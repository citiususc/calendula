/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 */

package es.usc.citius.servando.calendula.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Application-facing network facade.
 *
 * <p>Every application-initiated backend request must pass the same preflight:
 * (1) a VPN-aware local route, (2) the full active multi-stage Internet probe
 * from {@link ConnectivityAndInternetAccess}, and (3) DNS resolution of the
 * actual backend host through the effective Android network. The real request
 * is started only after all three gates succeed.</p>
 */
public final class NetworkUtils {

    private static final String TAG = "NetworkUtils";
    private static final long BACKEND_DNS_TIMEOUT_MS = 1500L;

    private static final ConnectivityAndInternetAccess ACTIVE_CONNECTIVITY =
            new ConnectivityAndInternetAccess.Builder().build();
    private static final ConnectivityAndInternetAccess STRICT_CONNECTIVITY =
            ConnectivityAndInternetAccess.strictCaptivePortalBuilder().build();

    private static ConnectivityAndInternetAccess.NetworkObserver networkObserver;
    private static volatile ConnectivityAndInternetAccess.NetworkState latestState;

    interface ActiveInternetProbe {
        boolean isReachable(Context context);
    }

    interface BackendHostResolver {
        boolean resolves(String host, Network network) throws IOException;
    }

    private static final ActiveInternetProbe DEFAULT_ACTIVE_INTERNET_PROBE =
            new ActiveInternetProbe() {
                @Override
                public boolean isReachable(Context context) {
                    ConnectivityAndInternetAccess.InternetResult active =
                            ACTIVE_CONNECTIVITY.checkInternetBlocking(context);
                    LogUtil.d(
                            TAG,
                            "Active Internet preflight: reachable=" + active.isReachable()
                                    + ", winner=" + active.getReachedHost()
                                    + ", attempts=" + active.getAttemptedHosts().size()
                                    + ", elapsedMs=" + active.getElapsedMilliseconds());
                    if (!active.isReachable()) {
                        return false;
                    }

                    // A transport-level success alone can still occur behind a captive portal.
                    // Require the gist's strict HTTP 204 diagnostic before every real request.
                    ConnectivityAndInternetAccess.InternetResult strict =
                            STRICT_CONNECTIVITY.checkInternetBlocking(context);
                    LogUtil.d(
                            TAG,
                            "Strict captive-portal preflight: reachable=" + strict.isReachable()
                                    + ", winner=" + strict.getReachedHost()
                                    + ", attempts=" + strict.getAttemptedHosts().size()
                                    + ", elapsedMs=" + strict.getElapsedMilliseconds());
                    return strict.isReachable();
                }
            };

    private static final BackendHostResolver DEFAULT_BACKEND_HOST_RESOLVER =
            new BackendHostResolver() {
                @Override
                public boolean resolves(String host, Network network) throws IOException {
                    InetAddress[] addresses;
                    if (network != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        addresses = network.getAllByName(host);
                    } else {
                        addresses = InetAddress.getAllByName(host);
                    }
                    return addresses != null && addresses.length > 0;
                }
            };

    private NetworkUtils() {
    }

    public static boolean isNetworkAvailable(final Context ctx) {
        return ctx != null && ConnectivityAndInternetAccess.isConnected(ctx);
    }

    /**
     * Mandatory preflight used immediately before a real HTTP(S)/WebView/download request.
     */
    public static boolean isBackendAvailable(final Context ctx, final String url) {
        return isBackendAvailable(
                ctx,
                url,
                DEFAULT_ACTIVE_INTERNET_PROBE,
                DEFAULT_BACKEND_HOST_RESOLVER);
    }

    static boolean isBackendAvailable(
            final Context ctx,
            final String url,
            final ActiveInternetProbe activeInternetProbe,
            final BackendHostResolver backendHostResolver) {
        final String host = backendHost(url);
        if (ctx == null
                || host == null
                || activeInternetProbe == null
                || backendHostResolver == null) {
            return false;
        }

        // Gate 1: reject stale VPN-only connectivity (for example AdGuard without
        // Wi-Fi/mobile/Ethernet underneath) before spending time on active probes.
        if (!ConnectivityAndInternetAccess.isConnected(ctx)) {
            LogUtil.w(TAG, "Backend preflight rejected: no usable VPN-aware network");
            return false;
        }

        // Gate 2: the complete gist probe pipeline is mandatory before every real request.
        if (!activeInternetProbe.isReachable(ctx)) {
            LogUtil.w(TAG, "Backend preflight rejected: active Internet probes failed");
            return false;
        }

        // Gate 3: the actual destination must resolve on the route that will carry the request.
        final Network network = selectBackendNetwork(ctx);
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "calendula-backend-dns");
                thread.setDaemon(true);
                return thread;
            }
        });
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return backendHostResolver.resolves(host, network);
            }
        });

        try {
            boolean resolved = Boolean.TRUE.equals(
                    future.get(BACKEND_DNS_TIMEOUT_MS, TimeUnit.MILLISECONDS));
            if (!resolved) {
                LogUtil.w(TAG, "Backend preflight rejected: cannot resolve " + host);
            }
            return resolved;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException | TimeoutException e) {
            future.cancel(true);
            LogUtil.w(TAG, "Backend preflight rejected: DNS timeout/failure for " + host);
            return false;
        } finally {
            executor.shutdownNow();
        }
    }

    static String backendHost(String url) {
        if (url == null) {
            return null;
        }
        try {
            URL parsed = new URL(url);
            String protocol = parsed.getProtocol();
            if (!"http".equalsIgnoreCase(protocol)
                    && !"https".equalsIgnoreCase(protocol)) {
                return null;
            }
            String host = parsed.getHost();
            return host == null || host.trim().isEmpty() ? null : host;
        } catch (MalformedURLException ignored) {
            return null;
        }
    }

    private static Network selectBackendNetwork(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network active = manager.getActiveNetwork();
            return active != null && ConnectivityAndInternetAccess.isConnected(context, active)
                    ? active
                    : null;
        }

        Network[] networks = manager.getAllNetworks();
        if (networks != null) {
            for (Network network : networks) {
                if (ConnectivityAndInternetAccess.isConnected(context, network)) {
                    return network;
                }
            }
        }
        return null;
    }

    public static boolean isVpnActive(final Context ctx) {
        return ctx != null && ConnectivityAndInternetAccess.vpnActive(ctx);
    }

    public static boolean hasUnderlyingNetwork(final Context ctx) {
        return ctx != null && ConnectivityAndInternetAccess.hasUnderlyingNetwork(ctx);
    }

    public static ConnectivityAndInternetAccess.NetworkState latestState(final Context ctx) {
        ConnectivityAndInternetAccess.NetworkState state = latestState;
        return state != null
                ? state
                : ConnectivityAndInternetAccess.snapshotNetworkState(ctx);
    }

    public static synchronized void startNetworkObserver(final Context ctx) {
        if (networkObserver != null || ctx == null) {
            return;
        }

        final Context applicationContext =
                ctx.getApplicationContext() != null ? ctx.getApplicationContext() : ctx;
        networkObserver = ConnectivityAndInternetAccess.observeNetwork(
                applicationContext,
                new ConnectivityAndInternetAccess.NetworkStateCallback() {
                    @Override
                    public void onStateChanged(
                            ConnectivityAndInternetAccess.NetworkState state) {
                        latestState = state;
                        LogUtil.d(
                                TAG,
                                "Network state: connected=" + state.isConnected()
                                        + ", vpn="
                                        + ConnectivityAndInternetAccess.vpnActive(applicationContext)
                                        + ", validated=" + state.isInternetValidated()
                                        + ", captivePortal=" + state.isCaptivePortalDetected());
                    }
                });
        latestState = networkObserver.getLatestState();
    }

    public static synchronized void stopNetworkObserver() {
        if (networkObserver == null) {
            return;
        }
        networkObserver.close();
        networkObserver = null;
        latestState = null;
    }
}
