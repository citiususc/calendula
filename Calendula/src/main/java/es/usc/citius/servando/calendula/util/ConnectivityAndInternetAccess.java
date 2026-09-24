/*
 * SPDX-License-Identifier: MIT
 *
 * Based on Connectivity.java by Emil Davtyan (emil2k), later modified by str4d.
 * Integrated into Calendula from Rodrigo Sambade Saa's ConnectivityAndInternetAccess gist:
 * https://gist.github.com/rodrigosambadesaa/729cca29a031fef4e2f15751863b655f
 * Further modernized for thread-safety, Android API compatibility,
 * extensible probe strategies, and captive-portal-aware reachability checks.
 */
package es.usc.citius.servando.calendula.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

@SuppressWarnings("deprecation")
public final class ConnectivityAndInternetAccess {

    // Value introduced in API 28. Kept numeric because Calendula compiles against SDK 27.
    private static final int NET_CAPABILITY_NOT_SUSPENDED_COMPAT = 21;

    public interface DnsProbeStrategy {
        boolean checkDns(String resolver, Network network);
    }

    public interface HttpProbeStrategy {
        boolean checkHttp(String url, Network network);
    }

    public interface TcpProbeStrategy {
        boolean checkTcp(String host, int port, Network network);
    }

    public interface NtpProbeStrategy {
        boolean checkNtp(String host, Network network);
    }

    public interface TlsProbeStrategy {
        boolean checkTls(String host, int port, Network network);
    }

    public interface InternetCallback {
        void onResult(InternetResult result);
    }

    /**
     * Receives the result of the optional ICMP diagnostic.
     *
     * <p>ICMP is deliberately independent from the normal DNS/HTTP reachability
     * result. A failed ICMP probe does not mean that Internet access is unavailable.
     */
    public interface IcmpCallback {
        void onResult(IcmpResult result);
    }

    public static final class InternetResult {
        private final boolean reachable;
        private final String reachedHost;
        private final List<String> attemptedHosts;
        private final long elapsedMilliseconds;

        private InternetResult(
                boolean reachable,
                String reachedHost,
                List<String> attemptedHosts,
                long elapsedMilliseconds) {
            this.reachable = reachable;
            this.reachedHost = reachedHost;
            this.attemptedHosts = Collections.unmodifiableList(new ArrayList<>(attemptedHosts));
            this.elapsedMilliseconds = elapsedMilliseconds;
        }

        public boolean isReachable() { return reachable; }
        public String getReachedHost() { return reachedHost; }
        public List<String> getAttemptedHosts() { return attemptedHosts; }
        public long getElapsedMilliseconds() { return elapsedMilliseconds; }
    }

    /**
     * Result of the optional ICMP diagnostic.
     *
     * <p>This result must not be used as the authoritative Internet-availability
     * signal. Networks commonly allow DNS/HTTPS while filtering ICMP.
     */
    public static final class IcmpResult {
        private final boolean reachable;
        private final String reachedAddress;
        private final List<String> attemptedAddresses;
        private final long elapsedMilliseconds;

        private IcmpResult(
                boolean reachable,
                String reachedAddress,
                List<String> attemptedAddresses,
                long elapsedMilliseconds) {
            this.reachable = reachable;
            this.reachedAddress = reachedAddress;
            this.attemptedAddresses = Collections.unmodifiableList(
                    new ArrayList<>(attemptedAddresses));
            this.elapsedMilliseconds = elapsedMilliseconds;
        }

        public boolean isReachable() { return reachable; }
        public String getReachedAddress() { return reachedAddress; }
        public List<String> getAttemptedAddresses() { return attemptedAddresses; }
        public long getElapsedMilliseconds() { return elapsedMilliseconds; }
    }

    public static final class Request {
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private volatile Future<?> future;

        private Request() {}

        public void cancel() {
            cancelled.set(true);
            Future<?> task = future;
            if (task != null) {
                task.cancel(true);
            }
        }

        public boolean isCancelled() {
            return cancelled.get();
        }

        private void attach(Future<?> task) {
            future = task;
            if (cancelled.get()) {
                task.cancel(true);
            }
        }
    }

    /** Receives passive changes to the application's default-network state. */
    public interface NetworkStateCallback {
        void onStateChanged(NetworkState state);
    }

    /**
     * Cheap, passive snapshot of the application's default network.
     *
     * <p>No DNS or HTTP traffic is generated to build this state. Validation and
     * captive-portal flags are only available from Android API 23 onward.
     */
    public static final class NetworkState {
        private final boolean connected;
        private final boolean internetValidated;
        private final boolean captivePortalDetected;
        private final long observedAtElapsedRealtime;

        private NetworkState(
                boolean connected,
                boolean internetValidated,
                boolean captivePortalDetected,
                long observedAtElapsedRealtime) {
            this.connected = connected;
            this.internetValidated = internetValidated;
            this.captivePortalDetected = captivePortalDetected;
            this.observedAtElapsedRealtime = observedAtElapsedRealtime;
        }

        public boolean isConnected() { return connected; }
        public boolean isInternetValidated() { return internetValidated; }
        public boolean isCaptivePortalDetected() { return captivePortalDetected; }
        public long getObservedAtElapsedRealtime() { return observedAtElapsedRealtime; }

        private boolean sameConnectivityState(NetworkState other) {
            return other != null
                    && connected == other.connected
                    && internetValidated == other.internetValidated
                    && captivePortalDetected == other.captivePortalDetected;
        }

        @Override
        public String toString() {
            return "NetworkState{"
                    + "connected=" + connected
                    + ", internetValidated=" + internetValidated
                    + ", captivePortalDetected=" + captivePortalDetected
                    + '}';
        }
    }

    /**
     * Lifecycle-friendly passive observer. API 24+ uses the application's default
     * {@link ConnectivityManager.NetworkCallback}. API 16-23 uses a dynamically
     * registered {@link ConnectivityManager#CONNECTIVITY_ACTION} receiver because
     * {@code registerDefaultNetworkCallback()} did not exist before API 24.
     *
     * <p>The observer itself performs no DNS or HTTP probes. Call {@link #close()}
     * when the owning lifecycle no longer needs updates.
     */
    public static final class NetworkObserver implements Closeable {
        private final Context applicationContext;
        private final ConnectivityManager connectivityManager;
        private final NetworkStateCallback callback;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private volatile NetworkState latestState;
        private ConnectivityManager.NetworkCallback networkCallback;
        private BroadcastReceiver legacyReceiver;

        private NetworkObserver(Context context, NetworkStateCallback callback) {
            requireContext(context);
            if (callback == null) {
                throw new IllegalArgumentException("callback == null");
            }
            Context appContext = context.getApplicationContext();
            this.applicationContext = appContext != null ? appContext : context;
            this.connectivityManager = manager(this.applicationContext);
            this.callback = callback;
            this.latestState = snapshotNetworkState(this.applicationContext);

            // Deliver a useful initial value immediately through the same main-thread path.
            deliver(this.latestState);
            register();
        }

        public NetworkState getLatestState() {
            return latestState;
        }

        private void register() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                networkCallback = new ConnectivityManager.NetworkCallback() {
                    private volatile Network currentDefaultNetwork;

                    @Override
                    public void onAvailable(Network network) {
                        // Do not synchronously query NetworkCapabilities here. Android's
                        // documentation warns that this is race-prone; wait for
                        // onCapabilitiesChanged().
                        currentDefaultNetwork = network;
                    }

                    @Override
                    public void onCapabilitiesChanged(
                            Network network,
                            NetworkCapabilities capabilities) {
                        currentDefaultNetwork = network;
                        publish(networkStateFromCapabilities(
                                connectivityManager, capabilities));
                    }

                    @Override
                    public void onLost(Network network) {
                        if (network.equals(currentDefaultNetwork)) {
                            currentDefaultNetwork = null;
                            publish(disconnectedNetworkState());
                        }
                    }
                };
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
                return;
            }

            legacyReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context ignoredContext, Intent ignoredIntent) {
                    publish(snapshotNetworkState(applicationContext));
                }
            };
            applicationContext.registerReceiver(
                    legacyReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        private void publish(NetworkState state) {
            if (closed.get()) {
                return;
            }
            NetworkState previous = latestState;
            latestState = state;
            if (!state.sameConnectivityState(previous)) {
                deliver(state);
            }
        }

        private void deliver(NetworkState state) {
            MAIN_HANDLER.post(() -> {
                if (!closed.get()) {
                    callback.onStateChanged(state);
                }
            });
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }

            if (networkCallback != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                } catch (IllegalArgumentException ignored) {
                    // Already unregistered or registration failed during teardown.
                }
                networkCallback = null;
            }

            if (legacyReceiver != null) {
                try {
                    applicationContext.unregisterReceiver(legacyReceiver);
                } catch (IllegalArgumentException ignored) {
                    // Receiver was already unregistered.
                }
                legacyReceiver = null;
            }
        }
    }

    private static final int MINIMUM_FAST_KBPS = 3_072;
    private static final int CONNECT_TIMEOUT_MS = 3_000;
    private static final int READ_TIMEOUT_MS = 3_000;
    private static final int DNS_TIMEOUT_MS = 2_500;
    private static final long EFFECTIVE_DNS_STAGE_TIMEOUT_MS = 1_500L;
    private static final long DNS_STAGE_TIMEOUT_MS = 3_500L;
    private static final long TOTAL_PROBE_TIMEOUT_MS = 6_000L;
    private static final int MAX_PARALLEL_PROBES = 16;
    private static final long ICMP_ATTEMPT_TIMEOUT_MS = 800L;
    private static final long ICMP_TOTAL_TIMEOUT_MS = 1_500L;
    private static final long ICMP_POLL_INTERVAL_MS = 25L;
    private static final String PING_BINARY = "/system/bin/ping";
    private static final int DNS_PORT = 53;
    private static final int NTP_PORT = 123;
    private static final int HTTPS_PORT = 443;
    private static final long CONNECTION_ATTEMPT_TIMEOUT_MS = 30_000L;
    private static final String DNS_QUERY_NAME = "example.com";

    private static final List<String> DEFAULT_DNS_RESOLVERS = Collections.unmodifiableList(Arrays.asList(
            "1.1.1.1",
            "8.8.8.8",
            "9.9.9.9",
            "208.67.222.222",
            "[2606:4700:4700::1111]"
    ));

    private static final List<String> DEFAULT_HOSTS = Collections.unmodifiableList(Arrays.asList(
            "https://www.google.com/generate_204",
            "https://www.facebook.com/",
            "https://www.wolframalpha.com/",
            "https://www.apple.com/",
            "https://www.amazon.com/"
    ));

    /**
     * Numeric addresses are intentional: the built-in ICMP diagnostic should not
     * require forward DNS before it can test basic IP/ICMP reachability.
     */
    private static final List<String> DEFAULT_ICMP_TARGETS =
            Collections.unmodifiableList(Arrays.asList(
                    "1.1.1.1",
                    "8.8.8.8",
                    "[2606:4700:4700::1111]"
            ));

    private static final List<String> DEFAULT_TCP_TARGETS =
            Collections.unmodifiableList(Arrays.asList(
                    "1.1.1.1:53",
                    "8.8.8.8:443",
                    "[2606:4700:4700::1111]:53"
            ));

    private static final List<String> DEFAULT_NTP_TARGETS =
            Collections.unmodifiableList(Arrays.asList(
                    "time.google.com",
                    "pool.ntp.org"
            ));

    private static final List<String> DEFAULT_TLS_TARGETS =
            Collections.unmodifiableList(Arrays.asList(
                    "www.google.com:443",
                    "cloudflare.com:443"
            ));

    private static volatile List<String> globalHosts = DEFAULT_HOSTS;
    private static volatile List<String> globalResolvers = DEFAULT_DNS_RESOLVERS;
    private static volatile List<String> globalTcpTargets = DEFAULT_TCP_TARGETS;
    private static volatile List<String> globalNtpTargets = DEFAULT_NTP_TARGETS;
    private static volatile List<String> globalTlsTargets = DEFAULT_TLS_TARGETS;
    private static volatile DnsProbeStrategy globalDnsStrategy = new DefaultDnsProbe();
    private static volatile HttpProbeStrategy globalHttpStrategy = new DefaultHttpProbe();
    private static volatile TcpProbeStrategy globalTcpStrategy = new DefaultTcpProbe();
    private static volatile NtpProbeStrategy globalNtpStrategy = new DefaultNtpProbe();
    private static volatile TlsProbeStrategy globalTlsStrategy = new DefaultTlsProbe();

    private static final Object CONNECTION_ATTEMPT_LOCK = new Object();
    private static final Deque<ConnectionAttempt> CONNECTION_ATTEMPT_QUEUE = new ArrayDeque<>();
    private static final AtomicInteger CONNECTION_ATTEMPTS = new AtomicInteger(0);
    private static final AtomicBoolean CONNECTION_ATTEMPT_STALLED = new AtomicBoolean(false);
    private static long legacyConnectingSinceElapsedRealtime = -1L;
    private static final AtomicInteger DNS_TRANSACTION_ID = new AtomicInteger((int) System.nanoTime());
    private static final AtomicInteger PROBE_THREAD_NUMBER = new AtomicInteger(0);

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final SSLSocketFactory TLS_12_SOCKET_FACTORY = createTls12Factory();

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactory() {
        private int number;

        @Override
        public synchronized Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "connectivity-check-" + (++number));
            thread.setDaemon(true);
            return thread;
        }
    });

    private final List<String> instanceHosts;
    private final List<String> instanceResolvers;
    private final List<String> instanceTcpTargets;
    private final List<String> instanceNtpTargets;
    private final List<String> instanceTlsTargets;
    private final DnsProbeStrategy instanceDnsStrategy;
    private final HttpProbeStrategy instanceHttpStrategy;
    private final TcpProbeStrategy instanceTcpStrategy;
    private final NtpProbeStrategy instanceNtpStrategy;
    private final TlsProbeStrategy instanceTlsStrategy;
    private final List<String> instanceIcmpTargets;

    /**
     * Legacy constructor retained for compatibility. It also updates the global host list,
     * matching the historical mutable-global behavior. New code should prefer Builder.
     */
    public ConnectivityAndInternetAccess(ArrayList<String> hosts) {
        this.instanceHosts = normalizeHosts(hosts);
        this.instanceResolvers = DEFAULT_DNS_RESOLVERS;
        this.instanceTcpTargets = DEFAULT_TCP_TARGETS;
        this.instanceNtpTargets = DEFAULT_NTP_TARGETS;
        this.instanceTlsTargets = DEFAULT_TLS_TARGETS;
        this.instanceDnsStrategy = new DefaultDnsProbe();
        this.instanceHttpStrategy = new DefaultHttpProbe();
        this.instanceTcpStrategy = new DefaultTcpProbe();
        this.instanceNtpStrategy = new DefaultNtpProbe();
        this.instanceTlsStrategy = new DefaultTlsProbe();
        this.instanceIcmpTargets = DEFAULT_ICMP_TARGETS;
        globalHosts = this.instanceHosts;
    }

    private ConnectivityAndInternetAccess(Builder builder) {
        this.instanceHosts = normalizeHosts(builder.hosts);
        this.instanceResolvers = normalizeDnsResolvers(builder.dnsResolvers);
        this.instanceTcpTargets = normalizeEndpointTargets(
                builder.tcpTargets, HTTPS_PORT, "tcpTargets");
        this.instanceNtpTargets = normalizeNtpTargets(builder.ntpTargets);
        this.instanceTlsTargets = normalizeEndpointTargets(
                builder.tlsTargets, HTTPS_PORT, "tlsTargets");
        this.instanceDnsStrategy = builder.dnsStrategy != null
                ? builder.dnsStrategy
                : new DefaultDnsProbe();
        this.instanceHttpStrategy = builder.httpStrategy != null
                ? builder.httpStrategy
                : new DefaultHttpProbe();
        this.instanceTcpStrategy = builder.tcpStrategy != null
                ? builder.tcpStrategy
                : new DefaultTcpProbe();
        this.instanceNtpStrategy = builder.ntpStrategy != null
                ? builder.ntpStrategy
                : new DefaultNtpProbe();
        this.instanceTlsStrategy = builder.tlsStrategy != null
                ? builder.tlsStrategy
                : new DefaultTlsProbe();
        this.instanceIcmpTargets = normalizeIcmpTargets(builder.icmpTargets);
    }

    public static final class Builder {
        private List<String> hosts = DEFAULT_HOSTS;
        private List<String> dnsResolvers = DEFAULT_DNS_RESOLVERS;
        private List<String> tcpTargets = DEFAULT_TCP_TARGETS;
        private List<String> ntpTargets = DEFAULT_NTP_TARGETS;
        private List<String> tlsTargets = DEFAULT_TLS_TARGETS;
        private List<String> icmpTargets = DEFAULT_ICMP_TARGETS;
        private DnsProbeStrategy dnsStrategy;
        private HttpProbeStrategy httpStrategy;
        private TcpProbeStrategy tcpStrategy;
        private NtpProbeStrategy ntpStrategy;
        private TlsProbeStrategy tlsStrategy;

        public Builder setHosts(List<String> hosts) {
            this.hosts = hosts;
            return this;
        }

        public Builder setDnsResolvers(List<String> resolvers) {
            this.dnsResolvers = resolvers;
            return this;
        }

        public Builder setTcpTargets(List<String> targets) {
            this.tcpTargets = targets;
            return this;
        }

        public Builder setNtpTargets(List<String> targets) {
            this.ntpTargets = targets;
            return this;
        }

        public Builder setTlsTargets(List<String> targets) {
            this.tlsTargets = targets;
            return this;
        }

        /**
         * Configures targets used only by the explicit ICMP diagnostic.
         *
         * <p>The default targets are 1.1.1.1 and 8.8.8.8. They are tried
         * sequentially, never as part of the normal DNS/HTTP reachability check.
         */
        public Builder setIcmpTargets(List<String> targets) {
            this.icmpTargets = targets;
            return this;
        }

        public Builder setDnsProbeStrategy(DnsProbeStrategy strategy) {
            this.dnsStrategy = strategy;
            return this;
        }

        public Builder setHttpProbeStrategy(HttpProbeStrategy strategy) {
            this.httpStrategy = strategy;
            return this;
        }

        public Builder setTcpProbeStrategy(TcpProbeStrategy strategy) {
            this.tcpStrategy = strategy;
            return this;
        }

        public Builder setNtpProbeStrategy(NtpProbeStrategy strategy) {
            this.ntpStrategy = strategy;
            return this;
        }

        public Builder setTlsProbeStrategy(TlsProbeStrategy strategy) {
            this.tlsStrategy = strategy;
            return this;
        }

        public ConnectivityAndInternetAccess build() {
            return new ConnectivityAndInternetAccess(this);
        }
    }

    public static Builder strictCaptivePortalBuilder() {
        return new Builder()
                .setDnsResolvers(Collections.emptyList())
                .setTcpTargets(Collections.emptyList())
                .setNtpTargets(Collections.emptyList())
                .setTlsTargets(Collections.emptyList())
                .setHosts(Collections.singletonList(
                        "https://connectivitycheck.gstatic.com/generate_204"))
                .setHttpProbeStrategy(new StrictHttpProbe());
    }

    // Instance API: preferred for new code.

    public Request checkInternetAsync(Context context, InternetCallback callback) {
        return executeAsync(
                context,
                instanceResolvers,
                instanceHosts,
                instanceTcpTargets,
                instanceNtpTargets,
                instanceTlsTargets,
                instanceDnsStrategy,
                instanceHttpStrategy,
                instanceTcpStrategy,
                instanceNtpStrategy,
                instanceTlsStrategy,
                callback);
    }

    public InternetResult checkInternetBlocking(Context context) {
        return executeBlocking(
                context,
                instanceResolvers,
                instanceHosts,
                instanceTcpTargets,
                instanceNtpTargets,
                instanceTlsTargets,
                instanceDnsStrategy,
                instanceHttpStrategy,
                instanceTcpStrategy,
                instanceNtpStrategy,
                instanceTlsStrategy);
    }

    /**
     * Runs an optional ICMP diagnostic off the caller thread.
     *
     * <p>This does not participate in {@link #checkInternetAsync(Context, InternetCallback)}
     * and a false result must not be interpreted as "offline". The spawned ping
     * process follows the OS routing decision and cannot be bound to a specific
     * Android {@link Network} like the DNS/HTTP probes can.
     */
    public Request checkIcmpReachabilityAsync(IcmpCallback callback) {
        return executeIcmpAsync(instanceIcmpTargets, callback);
    }

    /**
     * Blocking counterpart of {@link #checkIcmpReachabilityAsync(IcmpCallback)}.
     * Do not call this from the main thread.
     */
    public IcmpResult checkIcmpReachabilityBlocking() {
        return executeIcmpBlocking(instanceIcmpTargets);
    }

    // Connectivity API.

    public static boolean isActiveNetworkConnected(Context context) {
        return isConnected(context);
    }

    public static boolean isConnected(Context context, Network network) {
        requireContext(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return isConnected(context);
        }
        if (network == null) {
            return false;
        }
        ConnectivityManager connectivityManager = manager(context);
        return isEffectivelyUsable(connectivityManager,
                connectivityManager.getNetworkCapabilities(network));
    }

    public static boolean isConnecting(Context context) {
        requireContext(context);

        if (isConnected(context)) {
            return false;
        }

        if (Build.VERSION.SDK_INT <= 28 /* P; compileSdk 27 compatibility */) {
            boolean legacyConnecting = isLegacyConnecting(manager(context));
            updateLegacyConnectingStallState(legacyConnecting);
            if (legacyConnecting) {
                return true;
            }
        }

        expireTimedOutConnectionAttempts();
        return CONNECTION_ATTEMPTS.get() > 0;
    }

    /**
     * Returns whether a connection attempt has remained unresolved for at least
     * {@link #CONNECTION_ATTEMPT_TIMEOUT_MS}.
     *
     * <p>On API 29+ this is based on attempts registered through
     * {@link #beginConnectionAttempt(Context)}. On API 16-28 the legacy
     * {@link NetworkInfo.State#CONNECTING} signal is also timed from the first
     * observation made by this helper or {@link #isConnecting(Context)}.
     *
     * <p>An explicit-attempt timeout remains observable until a successful
     * connection, a new attempt cycle, or {@link #clearConnectionAttemptStall()}.
     */
    public static boolean isConnectionAttemptStalled(Context context) {
        requireContext(context);

        if (isConnected(context)) {
            return false;
        }

        expireTimedOutConnectionAttempts();
        if (CONNECTION_ATTEMPT_STALLED.get()) {
            return true;
        }

        if (Build.VERSION.SDK_INT <= 28 /* P; compileSdk 27 compatibility */) {
            return updateLegacyConnectingStallState(
                    isLegacyConnecting(manager(context)));
        }

        return false;
    }

    /** Clears a previously observed connection-attempt stall/timeout. */
    public static void clearConnectionAttemptStall() {
        synchronized (CONNECTION_ATTEMPT_LOCK) {
            CONNECTION_ATTEMPT_STALLED.set(false);
            legacyConnectingSinceElapsedRealtime = -1L;
        }
    }

    public static void beginConnectionAttempt(Context context) {
        requireContext(context);
        Context applicationContext = context.getApplicationContext();
        final Context safeContext = applicationContext != null ? applicationContext : context;
        final ConnectionAttempt attempt;

        synchronized (CONNECTION_ATTEMPT_LOCK) {
            if (CONNECTION_ATTEMPTS.get() == 0) {
                CONNECTION_ATTEMPT_STALLED.set(false);
                legacyConnectingSinceElapsedRealtime = -1L;
            }
            // Timestamp at enqueue time so queue order and timeout order cannot diverge
            // when several callers begin attempts concurrently.
            attempt = new ConnectionAttempt(SystemClock.elapsedRealtime());
            CONNECTION_ATTEMPT_QUEUE.addLast(attempt);
            CONNECTION_ATTEMPTS.incrementAndGet();
        }

        MAIN_HANDLER.postDelayed(
                () -> {
                    if (!isConnected(safeContext)) {
                        timeoutConnectionAttempt(attempt);
                    }
                },
                CONNECTION_ATTEMPT_TIMEOUT_MS);
    }

    public static void endConnectionAttempt() {
        synchronized (CONNECTION_ATTEMPT_LOCK) {
            while (!CONNECTION_ATTEMPT_QUEUE.isEmpty()) {
                ConnectionAttempt attempt = CONNECTION_ATTEMPT_QUEUE.removeFirst();
                if (!attempt.closed) {
                    attempt.closed = true;
                    if (CONNECTION_ATTEMPTS.get() > 0) {
                CONNECTION_ATTEMPTS.decrementAndGet();
            }
                    return;
                }
            }
        }
    }

    public static boolean isConnectedOrConnecting(Context context) {
        requireContext(context);

        if (isConnected(context)) {
            return true;
        }

        if (Build.VERSION.SDK_INT <= 28 /* P; compileSdk 27 compatibility */) {
            for (NetworkInfo info : legacyNetworks(manager(context))) {
                if (info != null
                        && info.isAvailable()
                        && info.isConnectedOrConnecting()) {
                    return true;
                }
            }
        }

        return CONNECTION_ATTEMPTS.get() > 0;
    }

    public static boolean isConnected(Context context) {
        requireContext(context);
        ConnectivityManager connectivityManager = manager(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network active = connectivityManager.getActiveNetwork();
            if (active != null
                    && isEffectivelyUsable(connectivityManager,
                            connectivityManager.getNetworkCapabilities(active))) {
                clearConnectionAttempts();
                return true;
            }
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Network network : connectivityManager.getAllNetworks()) {
                if (isEffectivelyUsable(connectivityManager,
                        connectivityManager.getNetworkCapabilities(network))) {
                    clearConnectionAttempts();
                    return true;
                }
            }
            return false;
        }

        boolean connected = isConnectedLegacy(connectivityManager.getActiveNetworkInfo());
        if (connected) {
            clearConnectionAttempts();
        }
        return connected;
    }

    /** Returns whether a usable non-VPN network exists beneath the active path. */
    public static boolean hasUnderlyingNetwork(Context context) {
        requireContext(context);
        return hasUsableNonVpnNetwork(manager(context));
    }

    /** Compatibility alias for {@link #hasUnderlyingNetwork(Context)}. */
    public static boolean hasPhysicalNetwork(Context context) {
        return hasUnderlyingNetwork(context);
    }

    /** Returns a cheap point-in-time snapshot of the application's default network. */
    public static NetworkState snapshotNetworkState(Context context) {
        requireContext(context);
        ConnectivityManager connectivityManager = manager(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network active = connectivityManager.getActiveNetwork();
            if (active == null) {
                return disconnectedNetworkState();
            }
            return networkStateFromCapabilities(
                    connectivityManager,
                    connectivityManager.getNetworkCapabilities(active));
        }

        // Before API 23 there is no default-Network object and no VALIDATED or
        // CAPTIVE_PORTAL capability. activeNetworkInfo represents the legacy default.
        boolean connected = isConnectedLegacy(connectivityManager.getActiveNetworkInfo());
        return new NetworkState(
                connected,
                false,
                false,
                SystemClock.elapsedRealtime());
    }

    /**
     * Starts passive observation of the application's default network and immediately
     * delivers the current state on the main thread. Close the returned observer when
     * it is no longer needed.
     */
    public static NetworkObserver observeNetwork(
            Context context,
            NetworkStateCallback callback) {
        return new NetworkObserver(context, callback);
    }

    /**
     * Returns whether Android most recently validated general Internet access on the
     * application's current default network. This is a system snapshot, not a fresh
     * reachability probe. API levels below 23 do not expose VALIDATED and return false.
     */
    public static boolean isInternetValidated(Context context) {
        requireContext(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        ConnectivityManager connectivityManager = manager(context);
        Network active = connectivityManager.getActiveNetwork();
        return active != null && isInternetValidated(context, active);
    }

    /**
     * Network-specific variant of {@link #isInternetValidated(Context)}.
     */
    public static boolean isInternetValidated(Context context, Network network) {
        requireContext(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                manager(context).getNetworkCapabilities(network);
        return isEffectivelyUsable(manager(context), capabilities)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * Returns whether Android detected a captive portal on the application's current
     * default network the last time that network was probed. API levels below 23 do not
     * expose CAPTIVE_PORTAL and return false.
     */
    public static boolean isCaptivePortalDetected(Context context) {
        requireContext(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        ConnectivityManager connectivityManager = manager(context);
        Network active = connectivityManager.getActiveNetwork();
        return active != null && isCaptivePortalDetected(context, active);
    }

    /**
     * Network-specific variant of {@link #isCaptivePortalDetected(Context)}.
     */
    public static boolean isCaptivePortalDetected(Context context, Network network) {
        requireContext(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                manager(context).getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL);
    }

    public static boolean isConnectedWifi(Context context) {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_WIFI);
    }

    public static boolean isConnectedWifi(Context context, Network network) {
        return hasTransport(context, network, NetworkCapabilities.TRANSPORT_WIFI);
    }

    public static boolean isConnectedWifiOverAirplaneMode(Context context) {
        return isAirplaneModeOn(context) && isConnectedWifi(context);
    }

    public static boolean isConnectedWifiOverAirplaneMode(Context context, Network network) {
        return isAirplaneModeOn(context) && isConnectedWifi(context, network);
    }

    public static boolean isConnectedMobileTelephonyManager(Context context) {
        requireContext(context);
        if (Build.VERSION.SDK_INT >= 31 /* S; compileSdk 27 compatibility */) {
            return isConnectedMobile(context);
        }

        try {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager != null
                    && telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED;
        } catch (SecurityException ignored) {
            return isConnectedMobile(context);
        }
    }

    public static boolean isConnectedMobile(Context context, Network network) {
        return hasTransport(context, network, NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    public static boolean isConnectedMobile(Context context) {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    public static boolean isConnectedEthernet(Context context) {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    public static boolean isConnectedEthernet(Context context, Network network) {
        return hasTransport(context, network, NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    public static boolean isConnectedFast(Context context) {
        requireContext(context);
        ConnectivityManager connectivityManager = manager(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Network network : connectivityManager.getAllNetworks()) {
                if (isFast(connectivityManager.getNetworkCapabilities(network))) {
                    return true;
                }
            }
            return false;
        }

        for (NetworkInfo info : legacyNetworks(connectivityManager)) {
            if (isConnectedLegacy(info)
                    && isConnectionFast(info.getType(), info.getSubtype())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnectedFast(Context context, Network network) {
        requireContext(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            NetworkInfo info = manager(context).getActiveNetworkInfo();
            return isConnectedLegacy(info)
                    && isConnectionFast(info.getType(), info.getSubtype());
        }

        if (network == null) {
            return false;
        }

        return isFast(manager(context).getNetworkCapabilities(network));
    }

    public static boolean isAirplaneModeOn(Context context) {
        requireContext(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON,
                    0) != 0;
        }

        return Settings.Global.getInt(
                context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON,
                0) != 0;
    }

    public static boolean vpnActive(Context context) {
        requireContext(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }

        ConnectivityManager connectivityManager = manager(context);
        for (Network network : connectivityManager.getAllNetworks()) {
            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(network);
            if (capabilities != null
                    && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                return true;
            }
        }
        return false;
    }

    // Static compatibility helpers. Names differ from the instance methods so
    // Java and Kotlin/JVM do not generate duplicate method signatures.

    public static boolean isInternetReachable(Context context) {
        return executeBlocking(
                context,
                globalResolvers,
                globalHosts,
                globalDnsStrategy,
                globalHttpStrategy).isReachable();
    }

    public static boolean isInternetReachable(Context context, ArrayList<String> hosts) {
        return executeBlocking(
                context,
                globalResolvers,
                normalizeHosts(hosts),
                globalDnsStrategy,
                globalHttpStrategy).isReachable();
    }

    public static Request checkInternetAsyncDefault(
            Context context,
            InternetCallback callback) {
        return executeAsync(
                context,
                globalResolvers,
                globalHosts,
                globalDnsStrategy,
                globalHttpStrategy,
                callback);
    }

    public static Request checkInternetAsyncDefault(
            Context context,
            List<String> hosts,
            InternetCallback callback) {
        return executeAsync(
                context,
                globalResolvers,
                hosts,
                globalDnsStrategy,
                globalHttpStrategy,
                callback);
    }

    public static Request checkInternetAsyncDefault(
            Context context,
            List<String> dnsResolvers,
            List<String> hosts,
            InternetCallback callback) {
        return executeAsync(
                context,
                dnsResolvers,
                hosts,
                globalDnsStrategy,
                globalHttpStrategy,
                callback);
    }

    public static InternetResult checkInternetBlockingDefault(Context context) {
        return executeBlocking(
                context,
                globalResolvers,
                globalHosts,
                globalDnsStrategy,
                globalHttpStrategy);
    }

    public static InternetResult checkInternetBlockingDefault(
            Context context,
            List<String> hosts) {
        return executeBlocking(
                context,
                globalResolvers,
                hosts,
                globalDnsStrategy,
                globalHttpStrategy);
    }

    public static InternetResult checkInternetBlockingDefault(
            Context context,
            List<String> dnsResolvers,
            List<String> hosts) {
        return executeBlocking(
                context,
                dnsResolvers,
                hosts,
                globalDnsStrategy,
                globalHttpStrategy);
    }

    public static Request checkIcmpReachabilityAsyncDefault(IcmpCallback callback) {
        return executeIcmpAsync(DEFAULT_ICMP_TARGETS, callback);
    }

    public static IcmpResult checkIcmpReachabilityBlockingDefault() {
        return executeIcmpBlocking(DEFAULT_ICMP_TARGETS);
    }

    public static List<String> defaultHosts() {
        return DEFAULT_HOSTS;
    }

    public static List<String> defaultDnsResolvers() {
        return DEFAULT_DNS_RESOLVERS;
    }

    public static List<String> defaultTcpTargets() {
        return DEFAULT_TCP_TARGETS;
    }

    public static List<String> defaultNtpTargets() {
        return DEFAULT_NTP_TARGETS;
    }

    public static List<String> defaultTlsTargets() {
        return DEFAULT_TLS_TARGETS;
    }

    public static List<String> defaultIcmpTargets() {
        return DEFAULT_ICMP_TARGETS;
    }

    private static Request executeIcmpAsync(
            List<String> targets,
            IcmpCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        }

        final List<String> normalizedTargets = normalizeIcmpTargets(targets);
        final Request request = new Request();

        Future<?> future = EXECUTOR.submit(() -> {
            IcmpResult result = executeIcmpBlocking(normalizedTargets);

            if (!request.isCancelled()) {
                MAIN_HANDLER.post(() -> {
                    if (!request.isCancelled()) {
                        callback.onResult(result);
                    }
                });
            }
        });

        request.attach(future);
        return request;
    }

    private static IcmpResult executeIcmpBlocking(List<String> targets) {
        long started = SystemClock.elapsedRealtime();
        long deadline = started + ICMP_TOTAL_TIMEOUT_MS;
        List<String> attempted = new ArrayList<>();

        for (String target : normalizeIcmpTargets(targets)) {
            if (Thread.currentThread().isInterrupted()
                    || SystemClock.elapsedRealtime() >= deadline) {
                break;
            }

            attempted.add(target);
            long attemptDeadline = Math.min(
                    deadline,
                    SystemClock.elapsedRealtime() + ICMP_ATTEMPT_TIMEOUT_MS);

            if (checkIcmpTarget(target, attemptDeadline)) {
                return new IcmpResult(
                        true,
                        target,
                        attempted,
                        SystemClock.elapsedRealtime() - started);
            }
        }

        return new IcmpResult(
                false,
                null,
                attempted,
                SystemClock.elapsedRealtime() - started);
    }

    /**
     * Executes ping without a shell, so a configured target is passed as one
     * process argument rather than interpreted as command text.
     */
    private static boolean checkIcmpTarget(String target, long deadline) {
        Process process = null;

        try {
            process = startPingProcess(stripAddressBrackets(target));

            // ping never needs stdin.
            closeQuietly(process.getOutputStream());

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    return process.exitValue() == 0;
                } catch (IllegalThreadStateException stillRunning) {
                    // Process is still alive; enforce our own API-16-safe deadline.
                }

                long remaining = deadline - SystemClock.elapsedRealtime();
                if (remaining <= 0) {
                    return false;
                }

                try {
                    Thread.sleep(Math.min(ICMP_POLL_INTERVAL_MS, remaining));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            return false;
        } catch (IOException | RuntimeException ignored) {
            // Missing/restricted ping binaries and filtered ICMP are diagnostic
            // failures only; they never alter the normal Internet result.
            return false;
        } finally {
            if (process != null) {
                try {
                    process.destroy();
                } catch (RuntimeException ignored) {
                    // Best-effort teardown on unusual OEM Process implementations.
                }

                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                closeQuietly(process.getOutputStream());
            }
        }
    }

    private static Process startPingProcess(String target) throws IOException {
        try {
            return new ProcessBuilder(PING_BINARY, "-c", "1", target)
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException primaryFailure) {
            // Some OEM builds expose ping through PATH rather than this exact path.
            try {
                return new ProcessBuilder("ping", "-c", "1", target)
                        .redirectErrorStream(true)
                        .start();
            } catch (IOException fallbackFailure) {
                throw fallbackFailure;
            }
        }
    }

    private static Request executeAsync(
            Context context,
            List<String> dnsResolvers,
            List<String> hosts,
            List<String> tcpTargets,
            List<String> ntpTargets,
            List<String> tlsTargets,
            DnsProbeStrategy dnsStrategy,
            HttpProbeStrategy httpStrategy,
            TcpProbeStrategy tcpStrategy,
            NtpProbeStrategy ntpStrategy,
            TlsProbeStrategy tlsStrategy,
            InternetCallback callback) {
        requireContext(context);
        if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        }

        Context applicationContext = context.getApplicationContext();
        final Context appContext = applicationContext != null ? applicationContext : context;
        final List<String> normalizedResolvers = normalizeDnsResolvers(dnsResolvers);
        final List<String> normalizedHosts = normalizeHosts(hosts);
        final List<String> normalizedTcpTargets =
                normalizeEndpointTargets(tcpTargets, HTTPS_PORT, "tcpTargets");
        final List<String> normalizedNtpTargets = normalizeNtpTargets(ntpTargets);
        final List<String> normalizedTlsTargets =
                normalizeEndpointTargets(tlsTargets, HTTPS_PORT, "tlsTargets");
        final DnsProbeStrategy effectiveDnsStrategy =
                dnsStrategy != null ? dnsStrategy : new DefaultDnsProbe();
        final HttpProbeStrategy effectiveHttpStrategy =
                httpStrategy != null ? httpStrategy : new DefaultHttpProbe();
        final TcpProbeStrategy effectiveTcpStrategy =
                tcpStrategy != null ? tcpStrategy : new DefaultTcpProbe();
        final NtpProbeStrategy effectiveNtpStrategy =
                ntpStrategy != null ? ntpStrategy : new DefaultNtpProbe();
        final TlsProbeStrategy effectiveTlsStrategy =
                tlsStrategy != null ? tlsStrategy : new DefaultTlsProbe();
        final Request request = new Request();

        Future<?> future = EXECUTOR.submit(() -> {
            InternetResult result = executeBlocking(
                    appContext,
                    normalizedResolvers,
                    normalizedHosts,
                    normalizedTcpTargets,
                    normalizedNtpTargets,
                    normalizedTlsTargets,
                    effectiveDnsStrategy,
                    effectiveHttpStrategy,
                    effectiveTcpStrategy,
                    effectiveNtpStrategy,
                    effectiveTlsStrategy);

            if (!request.isCancelled()) {
                MAIN_HANDLER.post(() -> {
                    if (!request.isCancelled()) {
                        callback.onResult(result);
                    }
                });
            }
        });

        request.attach(future);
        return request;
    }

    private static Request executeAsync(
            Context context,
            List<String> dnsResolvers,
            List<String> hosts,
            DnsProbeStrategy dnsStrategy,
            HttpProbeStrategy httpStrategy,
            InternetCallback callback) {
        requireContext(context);
        if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        }

        Context applicationContext = context.getApplicationContext();
        final Context appContext = applicationContext != null ? applicationContext : context;
        final List<String> normalizedResolvers = normalizeDnsResolvers(dnsResolvers);
        final List<String> normalizedHosts = normalizeHosts(hosts);
        final DnsProbeStrategy effectiveDnsStrategy =
                dnsStrategy != null ? dnsStrategy : new DefaultDnsProbe();
        final HttpProbeStrategy effectiveHttpStrategy =
                httpStrategy != null ? httpStrategy : new DefaultHttpProbe();
        final Request request = new Request();

        Future<?> future = EXECUTOR.submit(() -> {
            InternetResult result = executeBlocking(
                    appContext,
                    normalizedResolvers,
                    normalizedHosts,
                    globalTcpTargets,
                    globalNtpTargets,
                    globalTlsTargets,
                    effectiveDnsStrategy,
                    effectiveHttpStrategy,
                    globalTcpStrategy,
                    globalNtpStrategy,
                    globalTlsStrategy);

            if (!request.isCancelled()) {
                MAIN_HANDLER.post(() -> {
                    if (!request.isCancelled()) {
                        callback.onResult(result);
                    }
                });
            }
        });

        request.attach(future);
        return request;
    }

    private static InternetResult executeBlocking(
            Context context,
            List<String> dnsResolvers,
            List<String> hosts,
            DnsProbeStrategy dnsStrategy,
            HttpProbeStrategy httpStrategy) {
        return executeBlocking(
                context,
                dnsResolvers,
                hosts,
                globalTcpTargets,
                globalNtpTargets,
                globalTlsTargets,
                dnsStrategy,
                httpStrategy,
                globalTcpStrategy,
                globalNtpStrategy,
                globalTlsStrategy);
    }

    private static InternetResult executeBlocking(
            Context context,
            List<String> dnsResolvers,
            List<String> hosts,
            List<String> tcpTargets,
            List<String> ntpTargets,
            List<String> tlsTargets,
            DnsProbeStrategy dnsStrategy,
            HttpProbeStrategy httpStrategy,
            TcpProbeStrategy tcpStrategy,
            NtpProbeStrategy ntpStrategy,
            TlsProbeStrategy tlsStrategy) {
        requireContext(context);

        long started = SystemClock.elapsedRealtime();
        long deadline = started + TOTAL_PROBE_TIMEOUT_MS;
        List<String> attempted = new CopyOnWriteArrayList<>();
        ConnectivityManager connectivityManager = manager(context);
        Network network = selectProbeNetwork(connectivityManager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (network == null) {
                return new InternetResult(
                        false,
                        null,
                        attempted,
                        SystemClock.elapsedRealtime() - started);
            }
        } else if (!isConnected(context)) {
            return new InternetResult(
                    false,
                    null,
                    attempted,
                    SystemClock.elapsedRealtime() - started);
        }

        ExecutorService probeExecutor = newProbeExecutor();
        try {
            List<String> normalizedResolvers = normalizeDnsResolvers(dnsResolvers);
            List<String> normalizedTcpTargets =
                    normalizeEndpointTargets(tcpTargets, HTTPS_PORT, "tcpTargets");
            List<String> normalizedNtpTargets = normalizeNtpTargets(ntpTargets);
            List<String> normalizedTlsTargets =
                    normalizeEndpointTargets(tlsTargets, HTTPS_PORT, "tlsTargets");
            String reached = null;

            /*
             * Prefer the DNS configuration of the selected Android Network before
             * contacting public resolvers directly. This respects the effective
             * network path (including VPN and Private DNS on modern Android).
             *
             * An empty resolver list still disables the entire DNS stage, preserving
             * the historical Builder semantics and strict captive-portal mode.
             * A custom DnsProbeStrategy also owns the DNS stage completely, so the
             * built-in effective-DNS preflight is only used with DefaultDnsProbe.
             */
            if (!normalizedResolvers.isEmpty()
                    && dnsStrategy instanceof DefaultDnsProbe) {
                List<ProbeAttempt> effectiveDnsAttempts =
                        Collections.singletonList(new ProbeAttempt(
                                effectiveDnsLabel(),
                                () -> checkEffectiveDns(network)));

                reached = raceProbes(
                        effectiveDnsAttempts,
                        attempted,
                        Math.min(deadline, started + EFFECTIVE_DNS_STAGE_TIMEOUT_MS),
                        probeExecutor);

                if (reached != null) {
                    return new InternetResult(
                            true,
                            reached,
                            attempted,
                            SystemClock.elapsedRealtime() - started);
                }
            }

            List<ProbeAttempt> transportAttempts = new ArrayList<>();
            for (String resolver : normalizedResolvers) {
                transportAttempts.add(new ProbeAttempt(
                        dnsEndpointLabel(resolver),
                        () -> dnsStrategy.checkDns(resolver, network)));
            }
            for (String target : normalizedTcpTargets) {
                Endpoint endpoint = parseEndpoint(target, HTTPS_PORT);
                transportAttempts.add(new ProbeAttempt(
                        endpointLabel("tcp", endpoint),
                        () -> tcpStrategy.checkTcp(endpoint.host, endpoint.port, network)));
            }
            for (String host : normalizedNtpTargets) {
                transportAttempts.add(new ProbeAttempt(
                        endpointLabel("ntp", new Endpoint(host, NTP_PORT)),
                        () -> ntpStrategy.checkNtp(host, network)));
            }

            reached = raceProbes(
                    transportAttempts,
                    attempted,
                    Math.min(deadline, started + DNS_STAGE_TIMEOUT_MS),
                    probeExecutor);

            if (reached != null) {
                return new InternetResult(
                        true,
                        reached,
                        attempted,
                        SystemClock.elapsedRealtime() - started);
            }

            List<ProbeAttempt> applicationAttempts = new ArrayList<>();
            for (String host : normalizeHosts(hosts)) {
                applicationAttempts.add(new ProbeAttempt(
                        host,
                        () -> httpStrategy.checkHttp(host, network)));
            }
            for (String target : normalizedTlsTargets) {
                Endpoint endpoint = parseEndpoint(target, HTTPS_PORT);
                applicationAttempts.add(new ProbeAttempt(
                        endpointLabel("tls", endpoint),
                        () -> tlsStrategy.checkTls(endpoint.host, endpoint.port, network)));
            }

            reached = raceProbes(
                    applicationAttempts,
                    attempted,
                    deadline,
                    probeExecutor);

            if (reached != null) {
                return new InternetResult(
                        true,
                        reached,
                        attempted,
                        SystemClock.elapsedRealtime() - started);
            }

            return new InternetResult(
                    false,
                    null,
                    attempted,
                    SystemClock.elapsedRealtime() - started);
        } finally {
            probeExecutor.shutdownNow();
        }
    }

    private static String raceProbes(
            List<ProbeAttempt> probes,
            List<String> attempted,
            long deadline,
            ExecutorService probeExecutor) {
        if (probes.isEmpty() || Thread.currentThread().isInterrupted()) {
            return null;
        }

        CompletionService<String> completion =
                new ExecutorCompletionService<>(probeExecutor);
        List<Future<String>> futures = new ArrayList<>();

        for (ProbeAttempt probe : probes) {
            futures.add(completion.submit(() -> {
                attempted.add(probe.label);
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                return probe.operation.run() ? probe.label : null;
            }));
        }

        int remaining = futures.size();
        try {
            while (remaining-- > 0) {
                long wait = deadline - SystemClock.elapsedRealtime();
                if (wait <= 0) {
                    return null;
                }

                Future<String> completed = completion.poll(wait, TimeUnit.MILLISECONDS);
                if (completed == null) {
                    return null;
                }

                try {
                    String reached = completed.get();
                    if (reached != null) {
                        return reached;
                    }
                } catch (CancellationException | ExecutionException ignored) {
                    // A failed probe does not fail the whole stage.
                }
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } finally {
            for (Future<String> future : futures) {
                future.cancel(true);
            }
        }

        return null;
    }

    private static boolean checkEffectiveDns(Network network) {
        try {
            InetAddress[] addresses;
            if (network != null
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                addresses = network.getAllByName(DNS_QUERY_NAME);
            } else {
                addresses = InetAddress.getAllByName(DNS_QUERY_NAME);
            }
            return addresses != null && addresses.length > 0;
        } catch (IOException | RuntimeException ignored) {
            return false;
        }
    }

    private static String effectiveDnsLabel() {
        return "dns://system/" + DNS_QUERY_NAME;
    }

    public static final class DefaultTcpProbe implements TcpProbeStrategy {
        @Override
        public boolean checkTcp(String host, int port, Network network) {
            Socket socket = null;

            try {
                socket = new Socket();
                if (network != null
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    network.bindSocket(socket);
                }

                InetAddress address = resolveAddress(host, network);
                socket.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT_MS);
                return socket.isConnected();
            } catch (IOException | RuntimeException ignored) {
                return false;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                        // Best-effort socket cleanup.
                    }
                }
            }
        }
    }

    public static final class DefaultNtpProbe implements NtpProbeStrategy {
        @Override
        public boolean checkNtp(String host, Network network) {
            DatagramSocket socket = null;

            try {
                socket = new DatagramSocket();
                if (network != null
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    network.bindSocket(socket);
                }

                socket.setSoTimeout(DNS_TIMEOUT_MS);
                InetAddress address = resolveAddress(host, network);
                byte[] request = new byte[48];
                request[0] = 0x1B;
                socket.send(new DatagramPacket(
                        request,
                        request.length,
                        address,
                        NTP_PORT));

                byte[] buffer = new byte[48];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);
                return response.getLength() >= 48;
            } catch (IOException | RuntimeException ignored) {
                return false;
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
    }

    public static final class DefaultTlsProbe implements TlsProbeStrategy {
        @Override
        public boolean checkTls(String host, int port, Network network) {
            Socket socket = null;
            SSLSocket sslSocket = null;

            try {
                socket = new Socket();
                if (network != null
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    network.bindSocket(socket);
                }

                InetAddress address = resolveAddress(host, network);
                socket.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT_MS);
                socket.setSoTimeout(READ_TIMEOUT_MS);

                SSLSocketFactory factory = TLS_12_SOCKET_FACTORY != null
                        ? TLS_12_SOCKET_FACTORY
                        : (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) factory.createSocket(socket, host, port, true);
                sslSocket.setSoTimeout(READ_TIMEOUT_MS);
                sslSocket.startHandshake();
                return true;
            } catch (IOException | RuntimeException ignored) {
                return false;
            } finally {
                if (sslSocket != null) {
                    try {
                        sslSocket.close();
                    } catch (IOException ignored) {
                        // Best-effort TLS socket cleanup.
                    }
                } else if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                        // Best-effort socket cleanup.
                    }
                }
            }
        }
    }

    public static final class DefaultDnsProbe implements DnsProbeStrategy {
        @Override
        public boolean checkDns(String resolver, Network network) {
            Endpoint endpoint = parseEndpoint(resolver, DNS_PORT);
            DatagramSocket socket = null;

            try {
                int transactionId = DNS_TRANSACTION_ID.incrementAndGet() & 0xffff;
                byte[] query = createDnsQuery(transactionId);

                socket = new DatagramSocket();
                if (network != null
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    network.bindSocket(socket);
                }

                socket.setSoTimeout(DNS_TIMEOUT_MS);
                socket.connect(new InetSocketAddress(
                        resolveAddress(endpoint.host, network),
                        endpoint.port));
                socket.send(new DatagramPacket(query, query.length));

                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);

                return isValidDnsResponse(
                        transactionId,
                        response.getData(),
                        response.getLength());
            } catch (IOException | RuntimeException ignored) {
                return false;
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }

        private byte[] createDnsQuery(int transactionId) {
            String[] labels = DNS_QUERY_NAME.split("\\.");
            int length = 12 + 1 + 4;
            for (String label : labels) {
                length += 1 + label.length();
            }

            byte[] query = new byte[length];
            query[0] = (byte) (transactionId >>> 8);
            query[1] = (byte) transactionId;
            query[2] = 0x01;
            query[5] = 0x01;

            int offset = 12;
            for (String label : labels) {
                query[offset++] = (byte) label.length();
                for (int i = 0; i < label.length(); i++) {
                    query[offset++] = (byte) label.charAt(i);
                }
            }

            query[offset++] = 0x00;
            query[offset++] = 0x00;
            query[offset++] = 0x01;
            query[offset++] = 0x00;
            query[offset] = 0x01;
            return query;
        }

        private boolean isValidDnsResponse(
                int transactionId,
                byte[] response,
                int length) {
            if (response == null || length < 12) {
                return false;
            }

            int responseId = ((response[0] & 0xff) << 8) | (response[1] & 0xff);
            int flags = ((response[2] & 0xff) << 8) | (response[3] & 0xff);
            int questionCount = ((response[4] & 0xff) << 8) | (response[5] & 0xff);
            int responseCode = flags & 0x000f;

            return responseId == transactionId
                    && (flags & 0x8000) != 0
                    && (flags & 0x7800) == 0
                    && questionCount > 0
                    && responseCode <= 5;
        }
    }

    public static final class DefaultHttpProbe implements HttpProbeStrategy {
        @Override
        public boolean checkHttp(String address, Network network) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(address);
                URLConnection raw = network != null
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? network.openConnection(url)
                        : url.openConnection();

                connection = (HttpURLConnection) raw;
                configureTlsIfNecessary(connection);

                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(READ_TIMEOUT_MS);
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept", "*/*");
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.setRequestProperty("Connection", "close");
                connection.setRequestProperty(
                        "User-Agent",
                        "ConnectivityAndInternetAccess/5");

                int response = connection.getResponseCode();
                return response >= 100 && response <= 599;
            } catch (IOException | RuntimeException ignored) {
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    public static final class StrictHttpProbe implements HttpProbeStrategy {
        @Override
        public boolean checkHttp(String address, Network network) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(address);
                URLConnection raw = network != null
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? network.openConnection(url)
                        : url.openConnection();

                connection = (HttpURLConnection) raw;
                configureTlsIfNecessary(connection);

                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(READ_TIMEOUT_MS);
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept", "*/*");
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.setRequestProperty("Connection", "close");
                connection.setRequestProperty(
                        "User-Agent",
                        "ConnectivityAndInternetAccess/5");

                int response = connection.getResponseCode();
                if (address.contains("generate_204")) {
                    return response == HttpURLConnection.HTTP_NO_CONTENT;
                }
                return response == HttpURLConnection.HTTP_OK
                        || response == HttpURLConnection.HTTP_NO_CONTENT;
            } catch (IOException | RuntimeException ignored) {
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    private static void configureTlsIfNecessary(HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                && TLS_12_SOCKET_FACTORY != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(TLS_12_SOCKET_FACTORY);
        }
    }

    private static NetworkState networkStateFromCapabilities(
            ConnectivityManager connectivityManager,
            NetworkCapabilities capabilities) {
        boolean connected = isEffectivelyUsable(connectivityManager, capabilities);
        boolean validated = connected
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        boolean captivePortal = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && capabilities != null
                && capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL);
        return new NetworkState(
                connected,
                validated,
                captivePortal,
                SystemClock.elapsedRealtime());
    }

    private static NetworkState disconnectedNetworkState() {
        return new NetworkState(false, false, false, SystemClock.elapsedRealtime());
    }

    private static ConnectivityManager manager(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            throw new IllegalStateException("ConnectivityManager unavailable");
        }
        return connectivityManager;
    }

    // Low-level capability check only. Call isEffectivelyUsable() when the
    // result must represent application connectivity, including VPN handling.
    private static boolean isUsable(NetworkCapabilities capabilities) {
        if (capabilities == null
                || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return false;
        }

        return Build.VERSION.SDK_INT < 28 /* P; compileSdk 27 compatibility */
                || capabilities.hasCapability(
                        NET_CAPABILITY_NOT_SUSPENDED_COMPAT);
    }

    private static boolean isEffectivelyUsable(
            ConnectivityManager connectivityManager,
            NetworkCapabilities capabilities) {
        if (!isUsable(capabilities)) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                || !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            return true;
        }
        // A VPN may retain INTERNET after its real underlying path disappeared.
        return hasUsableNonVpnNetwork(connectivityManager);
    }

    private static boolean hasUsableNonVpnNetwork(
            ConnectivityManager connectivityManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return isConnectedLegacy(connectivityManager.getActiveNetworkInfo());
        }
        for (Network network : connectivityManager.getAllNetworks()) {
            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(network);
            if (isUsable(capabilities)
                    && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    ? capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                    : !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTransport(Context context, int transport) {
        requireContext(context);
        ConnectivityManager connectivityManager = manager(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkCapabilities capabilities =
                        connectivityManager.getNetworkCapabilities(network);
                if (isUsable(capabilities) && capabilities.hasTransport(transport)) {
                    return true;
                }
            }
            return false;
        }

        for (NetworkInfo info : legacyNetworks(connectivityManager)) {
            if (isConnectedLegacy(info)
                    && legacyTypeMatches(info.getType(), transport)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTransport(
            Context context,
            Network network,
            int transport) {
        requireContext(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return hasTransport(context, transport);
        }

        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                manager(context).getNetworkCapabilities(network);
        return isUsable(capabilities) && capabilities.hasTransport(transport);
    }

    private static boolean legacyTypeMatches(int type, int transport) {
        if (transport == NetworkCapabilities.TRANSPORT_WIFI) {
            return type == ConnectivityManager.TYPE_WIFI;
        }
        if (transport == NetworkCapabilities.TRANSPORT_CELLULAR) {
            return type == ConnectivityManager.TYPE_MOBILE;
        }
        if (transport == NetworkCapabilities.TRANSPORT_ETHERNET) {
            return type == ConnectivityManager.TYPE_ETHERNET;
        }
        return false;
    }

    private static boolean isFast(NetworkCapabilities capabilities) {
        return isUsable(capabilities)
                && capabilities.getLinkDownstreamBandwidthKbps() >= MINIMUM_FAST_KBPS
                && capabilities.getLinkUpstreamBandwidthKbps() >= MINIMUM_FAST_KBPS;
    }

    private static NetworkInfo[] legacyNetworks(ConnectivityManager connectivityManager) {
        NetworkInfo[] networks = connectivityManager.getAllNetworkInfo();
        return networks != null ? networks : new NetworkInfo[0];
    }

    private static boolean isConnectedLegacy(NetworkInfo info) {
        return info != null && info.isAvailable() && info.isConnected();
    }

    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI
                || type == ConnectivityManager.TYPE_ETHERNET) {
            return true;
        }

        if (type != ConnectivityManager.TYPE_MOBILE) {
            return false;
        }

        switch (subType) {
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
            default:
                return false;
        }
    }

    private static Network selectProbeNetwork(ConnectivityManager connectivityManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network active = connectivityManager.getActiveNetwork();
            if (active != null
                    && isEffectivelyUsable(connectivityManager,
                            connectivityManager.getNetworkCapabilities(active))) {
                return active;
            }
            return null;
        }

        for (Network network : connectivityManager.getAllNetworks()) {
            if (isEffectivelyUsable(connectivityManager,
                    connectivityManager.getNetworkCapabilities(network))) {
                return network;
            }
        }
        return null;
    }

    private static List<String> normalizeIcmpTargets(List<String> targets) {
        if (targets == null) {
            throw new IllegalArgumentException("icmpTargets == null");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : targets) {
            if (raw == null) {
                continue;
            }

            String value = raw.trim();
            if (value.isEmpty()) {
                continue;
            }

            /*
             * ProcessBuilder already avoids shell injection. This validation also
             * rejects option-looking values and command/path punctuation while still
             * allowing IPv4, IPv6 zone identifiers, and ordinary host names.
             */
            String processTarget = stripAddressBrackets(value);
            if (value.startsWith("-")
                    || processTarget.startsWith("-")
                    || processTarget.isEmpty()
                    || !processTarget.matches("[A-Za-z0-9._:%-]+")) {
                throw new IllegalArgumentException(
                        "Invalid ICMP target: " + value);
            }

            normalized.add(value);
        }

        return Collections.unmodifiableList(new ArrayList<>(normalized));
    }

    private static List<String> normalizeHosts(List<String> hosts) {
        if (hosts == null) {
            throw new IllegalArgumentException("hosts == null");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : hosts) {
            if (raw == null) {
                continue;
            }

            String value = raw.trim();
            if (value.isEmpty()) {
                continue;
            }

            if (!value.regionMatches(true, 0, "https://", 0, 8)
                    && !value.regionMatches(true, 0, "http://", 0, 7)) {
                value = "https://" + value + "/";
            }

            if (!isValidURL(value)) {
                throw new IllegalArgumentException("Invalid HTTP(S) URL: " + value);
            }
            normalized.add(value);
        }

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("hosts cannot be empty");
        }

        return Collections.unmodifiableList(new ArrayList<>(normalized));
    }

    private static List<String> normalizeEndpointTargets(
            List<String> targets,
            int defaultPort,
            String argumentName) {
        if (targets == null) {
            throw new IllegalArgumentException(argumentName + " == null");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : targets) {
            if (raw == null) {
                continue;
            }

            String value = raw.trim();
            if (!value.isEmpty()) {
                parseEndpoint(value, defaultPort);
                normalized.add(value);
            }
        }

        return Collections.unmodifiableList(new ArrayList<>(normalized));
    }

    private static List<String> normalizeNtpTargets(List<String> targets) {
        if (targets == null) {
            throw new IllegalArgumentException("ntpTargets == null");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : targets) {
            if (raw == null) {
                continue;
            }

            String value = raw.trim();
            if (value.isEmpty()) {
                continue;
            }

            Endpoint endpoint = parseEndpoint(value, NTP_PORT);
            if (endpoint.port != NTP_PORT) {
                throw new IllegalArgumentException("NTP target must use port 123: " + value);
            }
            normalized.add(endpoint.host);
        }

        return Collections.unmodifiableList(new ArrayList<>(normalized));
    }

    private static List<String> normalizeDnsResolvers(List<String> resolvers) {
        if (resolvers == null) {
            throw new IllegalArgumentException("dnsResolvers == null");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : resolvers) {
            if (raw == null) {
                continue;
            }

            String value = raw.trim();
            if (!value.isEmpty()) {
                parseEndpoint(value, DNS_PORT);
                normalized.add(value);
            }
        }

        return Collections.unmodifiableList(new ArrayList<>(normalized));
    }

    private static boolean isValidURL(String address) {
        if (address == null) {
            throw new IllegalArgumentException("url == null");
        }

        try {
            URL url = new URL(address);
            url.toURI();
            String protocol = url.getProtocol();
            return ("http".equalsIgnoreCase(protocol)
                    || "https".equalsIgnoreCase(protocol))
                    && url.getHost() != null
                    && !url.getHost().isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String dnsEndpointLabel(String resolver) {
        return endpointLabel("dns", parseEndpoint(resolver, DNS_PORT));
    }

    private static String endpointLabel(String scheme, Endpoint endpoint) {
        String host = endpoint.host.indexOf(':') >= 0
                ? "[" + endpoint.host + "]"
                : endpoint.host;
        return scheme + "://" + host + ":" + endpoint.port;
    }

    private static Endpoint parseEndpoint(String target, int defaultPort) {
        if (target == null) {
            throw new IllegalArgumentException("target == null");
        }
        if (defaultPort <= 0 || defaultPort > 65_535) {
            throw new IllegalArgumentException("Invalid default port: " + defaultPort);
        }

        String value = target.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Invalid endpoint");
        }

        String host;
        int port = defaultPort;

        if (value.startsWith("[")) {
            int closingBracket = value.indexOf(']');
            if (closingBracket <= 1 || value.indexOf('[', 1) >= 0) {
                throw new IllegalArgumentException("Invalid endpoint: " + target);
            }

            host = value.substring(1, closingBracket).trim();
            String remainder = value.substring(closingBracket + 1).trim();
            if (!remainder.isEmpty()) {
                if (!remainder.startsWith(":") || remainder.indexOf(':', 1) >= 0) {
                    throw new IllegalArgumentException("Invalid endpoint: " + target);
                }
                port = parsePort(remainder.substring(1));
            }
        } else {
            if (value.indexOf('[') >= 0 || value.indexOf(']') >= 0) {
                throw new IllegalArgumentException("Invalid endpoint: " + target);
            }

            int firstColon = value.indexOf(':');
            int lastColon = value.lastIndexOf(':');
            if (firstColon >= 0 && firstColon == lastColon) {
                host = value.substring(0, firstColon).trim();
                port = parsePort(value.substring(firstColon + 1));
            } else {
                // Multiple colons without brackets are a bare IPv6 literal.
                host = value;
            }
        }

        if (host.isEmpty()) {
            throw new IllegalArgumentException("Invalid endpoint: " + target);
        }

        return new Endpoint(host, port);
    }

    private static String stripAddressBrackets(String target) {
        if (target != null
                && target.length() > 2
                && target.charAt(0) == '['
                && target.charAt(target.length() - 1) == ']') {
            return target.substring(1, target.length() - 1);
        }
        return target == null ? "" : target;
    }

    private static InetAddress resolveAddress(String host, Network network) throws IOException {
        InetAddress[] addresses;
        if (network != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addresses = network.getAllByName(host);
        } else {
            addresses = InetAddress.getAllByName(host);
        }

        if (addresses == null || addresses.length == 0) {
            throw new IOException("No addresses for " + host);
        }
        return addresses[0];
    }

    private static int parsePort(String rawPort) {
        try {
            int port = Integer.parseInt(rawPort.trim());
            if (port < 1 || port > 65_535) {
                throw new IllegalArgumentException("Invalid endpoint port");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid endpoint port", exception);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException ignored) {
            // Best-effort process-stream cleanup.
        }
    }

    private static ExecutorService newProbeExecutor() {
        return Executors.newFixedThreadPool(MAX_PARALLEL_PROBES, runnable -> {
            Thread thread = new Thread(
                    runnable,
                    "connectivity-probe-" + PROBE_THREAD_NUMBER.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    private static boolean timeoutConnectionAttempt(ConnectionAttempt attempt) {
        synchronized (CONNECTION_ATTEMPT_LOCK) {
            if (attempt.closed) {
                return false;
            }

            attempt.closed = true;
            CONNECTION_ATTEMPT_QUEUE.remove(attempt);
            if (CONNECTION_ATTEMPTS.get() > 0) {
                CONNECTION_ATTEMPTS.decrementAndGet();
            }
            CONNECTION_ATTEMPT_STALLED.set(true);
            return true;
        }
    }

    private static void expireTimedOutConnectionAttempts() {
        long now = SystemClock.elapsedRealtime();

        synchronized (CONNECTION_ATTEMPT_LOCK) {
            while (!CONNECTION_ATTEMPT_QUEUE.isEmpty()) {
                ConnectionAttempt attempt = CONNECTION_ATTEMPT_QUEUE.peekFirst();
                if (attempt == null) {
                    break;
                }
                if (attempt.closed) {
                    CONNECTION_ATTEMPT_QUEUE.removeFirst();
                    continue;
                }
                if (now - attempt.startedAtElapsedRealtime < CONNECTION_ATTEMPT_TIMEOUT_MS) {
                    break;
                }

                attempt.closed = true;
                CONNECTION_ATTEMPT_QUEUE.removeFirst();
                if (CONNECTION_ATTEMPTS.get() > 0) {
                CONNECTION_ATTEMPTS.decrementAndGet();
            }
                CONNECTION_ATTEMPT_STALLED.set(true);
            }
        }
    }

    private static boolean isLegacyConnecting(ConnectivityManager connectivityManager) {
        for (NetworkInfo info : legacyNetworks(connectivityManager)) {
            if (info != null
                    && info.isAvailable()
                    && info.getState() == NetworkInfo.State.CONNECTING) {
                return true;
            }
        }
        return false;
    }

    private static boolean updateLegacyConnectingStallState(boolean connecting) {
        synchronized (CONNECTION_ATTEMPT_LOCK) {
            if (!connecting) {
                legacyConnectingSinceElapsedRealtime = -1L;
                return false;
            }

            long now = SystemClock.elapsedRealtime();
            if (legacyConnectingSinceElapsedRealtime < 0L) {
                legacyConnectingSinceElapsedRealtime = now;
                return false;
            }

            return now - legacyConnectingSinceElapsedRealtime
                    >= CONNECTION_ATTEMPT_TIMEOUT_MS;
        }
    }

    private static void clearConnectionAttempts() {
        synchronized (CONNECTION_ATTEMPT_LOCK) {
            for (ConnectionAttempt attempt : CONNECTION_ATTEMPT_QUEUE) {
                attempt.closed = true;
            }
            CONNECTION_ATTEMPT_QUEUE.clear();
            CONNECTION_ATTEMPTS.set(0);
            CONNECTION_ATTEMPT_STALLED.set(false);
            legacyConnectingSinceElapsedRealtime = -1L;
        }
    }

    private interface ProbeOperation {
        boolean run();
    }

    private static final class ProbeAttempt {
        private final String label;
        private final ProbeOperation operation;

        private ProbeAttempt(String label, ProbeOperation operation) {
            this.label = label;
            this.operation = operation;
        }
    }

    private static final class Endpoint {
        private final String host;
        private final int port;

        private Endpoint(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    private static final class ConnectionAttempt {
        private final long startedAtElapsedRealtime;
        private boolean closed;

        private ConnectionAttempt(long startedAtElapsedRealtime) {
            this.startedAtElapsedRealtime = startedAtElapsedRealtime;
        }
    }

    private static SSLSocketFactory createTls12Factory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, null, null);
            return new Tls12SocketFactory(context.getSocketFactory());
        } catch (GeneralSecurityException ignored) {
            return null;
        }
    }

    private static void requireContext(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
    }

    private static final class Tls12SocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;

        private Tls12SocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException {
            return enable(delegate.createSocket(socket, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enable(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(
                String host,
                int port,
                InetAddress localHost,
                int localPort) throws IOException {
            return enable(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enable(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(
                InetAddress address,
                int port,
                InetAddress localAddress,
                int localPort) throws IOException {
            return enable(delegate.createSocket(address, port, localAddress, localPort));
        }

        private Socket enable(Socket socket) {
            if (socket instanceof SSLSocket) {
                SSLSocket sslSocket = (SSLSocket) socket;
                if (Arrays.asList(sslSocket.getSupportedProtocols()).contains("TLSv1.2")) {
                    sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
                }
            }
            return socket;
        }
    }
}
