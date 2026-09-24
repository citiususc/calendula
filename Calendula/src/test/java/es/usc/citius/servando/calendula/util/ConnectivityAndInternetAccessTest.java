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
import android.net.NetworkCapabilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InOrder;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27)
public class ConnectivityAndInternetAccessTest {

    private static final String TEST_URL = "https://probe.example.test/generate_204";

    @Test
    public void plainInternetNetworkIsConnected() {
        NetworkFixture fixture = connectedWifi();

        assertTrue(ConnectivityAndInternetAccess.isConnected(fixture.context));
        assertTrue(ConnectivityAndInternetAccess.hasUnderlyingNetwork(fixture.context));
        assertTrue(ConnectivityAndInternetAccess.hasPhysicalNetwork(fixture.context));
    }

    @Test
    public void adGuardVpnWithoutUnderlyingNetworkIsDisconnected() {
        Context context = mock(Context.class);
        ConnectivityManager manager = mock(ConnectivityManager.class);
        Network vpn = mock(Network.class);
        NetworkCapabilities vpnCapabilities = mock(NetworkCapabilities.class);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetwork()).thenReturn(vpn);
        when(manager.getAllNetworks()).thenReturn(new Network[]{vpn});
        when(manager.getNetworkCapabilities(vpn)).thenReturn(vpnCapabilities);
        when(vpnCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);
        when(vpnCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_VPN)).thenReturn(true);
        when(vpnCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_NOT_VPN)).thenReturn(false);

        assertFalse(ConnectivityAndInternetAccess.isConnected(context));
        assertFalse(ConnectivityAndInternetAccess.hasUnderlyingNetwork(context));
        assertTrue(ConnectivityAndInternetAccess.vpnActive(context));
    }

    @Test
    public void vpnWithUnderlyingWifiIsConnected() {
        Context context = mock(Context.class);
        ConnectivityManager manager = mock(ConnectivityManager.class);
        Network vpn = mock(Network.class);
        Network wifi = mock(Network.class);
        NetworkCapabilities vpnCapabilities = mock(NetworkCapabilities.class);
        NetworkCapabilities wifiCapabilities = mock(NetworkCapabilities.class);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetwork()).thenReturn(vpn);
        when(manager.getAllNetworks()).thenReturn(new Network[]{vpn, wifi});
        when(manager.getNetworkCapabilities(vpn)).thenReturn(vpnCapabilities);
        when(manager.getNetworkCapabilities(wifi)).thenReturn(wifiCapabilities);

        when(vpnCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);
        when(vpnCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_VPN)).thenReturn(true);
        when(vpnCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_NOT_VPN)).thenReturn(false);

        when(wifiCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);
        when(wifiCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_VPN)).thenReturn(false);
        when(wifiCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_NOT_VPN)).thenReturn(true);

        assertTrue(ConnectivityAndInternetAccess.isConnected(context));
        assertTrue(ConnectivityAndInternetAccess.hasUnderlyingNetwork(context));
        assertTrue(ConnectivityAndInternetAccess.vpnActive(context));
    }

    @Test
    public void customDnsProbeCanEstablishReachability() {
        NetworkFixture fixture = connectedWifi();
        AtomicBoolean called = new AtomicBoolean(false);

        ConnectivityAndInternetAccess connectivity =
                baseBuilder()
                        .setDnsResolvers(Collections.singletonList("192.0.2.53"))
                        .setDnsProbeStrategy((resolver, network) -> {
                            called.set(true);
                            assertEquals("192.0.2.53", resolver);
                            assertEquals(fixture.network, network);
                            return true;
                        })
                        .build();

        ConnectivityAndInternetAccess.InternetResult result =
                connectivity.checkInternetBlocking(fixture.context);

        assertTrue(result.isReachable());
        assertTrue(called.get());
        assertEquals("dns://192.0.2.53:53", result.getReachedHost());
        assertTrue(result.getAttemptedHosts().contains("dns://192.0.2.53:53"));
    }

    @Test
    public void customTcpProbeCanEstablishReachability() {
        NetworkFixture fixture = connectedWifi();
        AtomicBoolean called = new AtomicBoolean(false);

        ConnectivityAndInternetAccess connectivity =
                baseBuilder()
                        .setTcpTargets(Collections.singletonList("192.0.2.10:443"))
                        .setTcpProbeStrategy((host, port, network) -> {
                            called.set(true);
                            assertEquals("192.0.2.10", host);
                            assertEquals(443, port);
                            assertEquals(fixture.network, network);
                            return true;
                        })
                        .build();

        ConnectivityAndInternetAccess.InternetResult result =
                connectivity.checkInternetBlocking(fixture.context);

        assertTrue(result.isReachable());
        assertTrue(called.get());
        assertEquals("tcp://192.0.2.10:443", result.getReachedHost());
    }

    @Test
    public void customNtpProbeCanEstablishReachability() {
        NetworkFixture fixture = connectedWifi();
        AtomicBoolean called = new AtomicBoolean(false);

        ConnectivityAndInternetAccess connectivity =
                baseBuilder()
                        .setNtpTargets(Collections.singletonList("time.example.test"))
                        .setNtpProbeStrategy((host, network) -> {
                            called.set(true);
                            assertEquals("time.example.test", host);
                            assertEquals(fixture.network, network);
                            return true;
                        })
                        .build();

        ConnectivityAndInternetAccess.InternetResult result =
                connectivity.checkInternetBlocking(fixture.context);

        assertTrue(result.isReachable());
        assertTrue(called.get());
        assertEquals("ntp://time.example.test:123", result.getReachedHost());
    }

    @Test
    public void customHttpProbeCanEstablishReachability() {
        NetworkFixture fixture = connectedWifi();
        AtomicBoolean called = new AtomicBoolean(false);

        ConnectivityAndInternetAccess connectivity =
                baseBuilder()
                        .setHosts(Collections.singletonList(TEST_URL))
                        .setHttpProbeStrategy((url, network) -> {
                            called.set(true);
                            assertEquals(TEST_URL, url);
                            assertEquals(fixture.network, network);
                            return true;
                        })
                        .build();

        ConnectivityAndInternetAccess.InternetResult result =
                connectivity.checkInternetBlocking(fixture.context);

        assertTrue(result.isReachable());
        assertTrue(called.get());
        assertEquals(TEST_URL, result.getReachedHost());
    }

    @Test
    public void customTlsProbeCanEstablishReachabilityAfterHttpFails() {
        NetworkFixture fixture = connectedWifi();
        AtomicBoolean tlsCalled = new AtomicBoolean(false);

        ConnectivityAndInternetAccess connectivity =
                baseBuilder()
                        .setHosts(Collections.singletonList(TEST_URL))
                        .setHttpProbeStrategy((url, network) -> false)
                        .setTlsTargets(Collections.singletonList("tls.example.test:443"))
                        .setTlsProbeStrategy((host, port, network) -> {
                            tlsCalled.set(true);
                            assertEquals("tls.example.test", host);
                            assertEquals(443, port);
                            assertEquals(fixture.network, network);
                            return true;
                        })
                        .build();

        ConnectivityAndInternetAccess.InternetResult result =
                connectivity.checkInternetBlocking(fixture.context);

        assertTrue(result.isReachable());
        assertTrue(tlsCalled.get());
        assertEquals("tls://tls.example.test:443", result.getReachedHost());
    }

    @Test
    public void allFailedInjectedProbesReportOffline() {
        NetworkFixture fixture = connectedWifi();

        ConnectivityAndInternetAccess connectivity =
                new ConnectivityAndInternetAccess.Builder()
                        .setHosts(Collections.singletonList(TEST_URL))
                        .setDnsResolvers(Collections.singletonList("192.0.2.53"))
                        .setTcpTargets(Collections.singletonList("192.0.2.10:443"))
                        .setNtpTargets(Collections.singletonList("time.example.test"))
                        .setTlsTargets(Collections.singletonList("tls.example.test:443"))
                        .setDnsProbeStrategy((resolver, network) -> false)
                        .setTcpProbeStrategy((host, port, network) -> false)
                        .setNtpProbeStrategy((host, network) -> false)
                        .setHttpProbeStrategy((url, network) -> false)
                        .setTlsProbeStrategy((host, port, network) -> false)
                        .build();

        ConnectivityAndInternetAccess.InternetResult result =
                connectivity.checkInternetBlocking(fixture.context);

        assertFalse(result.isReachable());
        assertEquals(null, result.getReachedHost());
        assertEquals(5, result.getAttemptedHosts().size());
    }

    @Test
    public void defaultTargetsCoverEveryProbeLayerAndDualStack() {
        assertFalse(ConnectivityAndInternetAccess.defaultHosts().isEmpty());
        assertFalse(ConnectivityAndInternetAccess.defaultDnsResolvers().isEmpty());
        assertFalse(ConnectivityAndInternetAccess.defaultTcpTargets().isEmpty());
        assertFalse(ConnectivityAndInternetAccess.defaultNtpTargets().isEmpty());
        assertFalse(ConnectivityAndInternetAccess.defaultTlsTargets().isEmpty());
        assertFalse(ConnectivityAndInternetAccess.defaultIcmpTargets().isEmpty());

        assertTrue(containsIpv6(ConnectivityAndInternetAccess.defaultDnsResolvers()));
        assertTrue(containsIpv6(ConnectivityAndInternetAccess.defaultTcpTargets()));
        assertTrue(containsIpv6(ConnectivityAndInternetAccess.defaultIcmpTargets()));
    }

    @Test
    public void strictCaptivePortalBuilderProducesRunnableConfiguration() {
        NetworkFixture fixture = connectedWifi();
        ConnectivityAndInternetAccess strict =
                ConnectivityAndInternetAccess.strictCaptivePortalBuilder()
                        .setHttpProbeStrategy((url, network) -> true)
                        .build();

        ConnectivityAndInternetAccess.InternetResult result =
                strict.checkInternetBlocking(fixture.context);

        assertNotNull(result);
        assertTrue(result.isReachable());
        assertEquals(
                "https://connectivitycheck.gstatic.com/generate_204",
                result.getReachedHost());
    }


    @Test
    public void mandatoryActiveProbeRunsBeforeActualBackendResolution() throws Exception {
        NetworkFixture fixture = connectedWifi();
        NetworkUtils.ActiveInternetProbe activeProbe =
                mock(NetworkUtils.ActiveInternetProbe.class);
        NetworkUtils.BackendHostResolver resolver =
                mock(NetworkUtils.BackendHostResolver.class);

        when(activeProbe.isReachable(fixture.context)).thenReturn(true);
        when(resolver.resolves("www.aemps.gob.es", fixture.network)).thenReturn(true);

        assertTrue(NetworkUtils.isBackendAvailable(
                fixture.context,
                "https://www.aemps.gob.es/cima/dochtml/p/example",
                activeProbe,
                resolver));

        InOrder order = inOrder(activeProbe, resolver);
        order.verify(activeProbe).isReachable(fixture.context);
        order.verify(resolver).resolves("www.aemps.gob.es", fixture.network);
    }

    @Test
    public void failedActiveProbePreventsBackendResolution() throws Exception {
        NetworkFixture fixture = connectedWifi();
        NetworkUtils.ActiveInternetProbe activeProbe =
                mock(NetworkUtils.ActiveInternetProbe.class);
        NetworkUtils.BackendHostResolver resolver =
                mock(NetworkUtils.BackendHostResolver.class);

        when(activeProbe.isReachable(fixture.context)).thenReturn(false);

        assertFalse(NetworkUtils.isBackendAvailable(
                fixture.context,
                "https://www.aemps.gob.es/cima/dochtml/p/example",
                activeProbe,
                resolver));

        verify(activeProbe).isReachable(fixture.context);
        verifyNoMoreInteractions(resolver);
    }

    @Test
    public void vpnWithoutUnderlyingNetworkStopsBeforeActiveProbe() throws Exception {
        Context context = mock(Context.class);
        ConnectivityManager manager = mock(ConnectivityManager.class);
        Network vpn = mock(Network.class);
        NetworkCapabilities vpnCapabilities = mock(NetworkCapabilities.class);
        NetworkUtils.ActiveInternetProbe activeProbe =
                mock(NetworkUtils.ActiveInternetProbe.class);
        NetworkUtils.BackendHostResolver resolver =
                mock(NetworkUtils.BackendHostResolver.class);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetwork()).thenReturn(vpn);
        when(manager.getAllNetworks()).thenReturn(new Network[]{vpn});
        when(manager.getNetworkCapabilities(vpn)).thenReturn(vpnCapabilities);
        when(vpnCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);
        when(vpnCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_VPN)).thenReturn(true);
        when(vpnCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_NOT_VPN)).thenReturn(false);

        assertFalse(NetworkUtils.isBackendAvailable(
                context,
                "https://www.aemps.gob.es/cima/dochtml/p/example",
                activeProbe,
                resolver));

        verify(activeProbe, never()).isReachable(context);
        verifyNoMoreInteractions(resolver);
    }

    @Test
    public void malformedBackendUrlNeverStartsMandatoryProbe() {
        NetworkFixture fixture = connectedWifi();
        NetworkUtils.ActiveInternetProbe activeProbe =
                mock(NetworkUtils.ActiveInternetProbe.class);
        NetworkUtils.BackendHostResolver resolver =
                mock(NetworkUtils.BackendHostResolver.class);

        assertFalse(NetworkUtils.isBackendAvailable(
                fixture.context,
                "not a URL",
                activeProbe,
                resolver));

        verifyNoMoreInteractions(activeProbe, resolver);
    }

    private static ConnectivityAndInternetAccess.Builder baseBuilder() {
        return new ConnectivityAndInternetAccess.Builder()
                .setHosts(Collections.singletonList(TEST_URL))
                .setDnsResolvers(Collections.<String>emptyList())
                .setTcpTargets(Collections.<String>emptyList())
                .setNtpTargets(Collections.<String>emptyList())
                .setTlsTargets(Collections.<String>emptyList());
    }

    private static boolean containsIpv6(Iterable<String> values) {
        for (String value : values) {
            if (value != null && value.contains(":") && value.contains("[")) {
                return true;
            }
        }
        return false;
    }

    private static NetworkFixture connectedWifi() {
        Context context = mock(Context.class);
        ConnectivityManager manager = mock(ConnectivityManager.class);
        Network wifi = mock(Network.class);
        NetworkCapabilities capabilities = mock(NetworkCapabilities.class);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetwork()).thenReturn(wifi);
        when(manager.getAllNetworks()).thenReturn(new Network[]{wifi});
        when(manager.getNetworkCapabilities(wifi)).thenReturn(capabilities);
        when(capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);
        when(capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_NOT_VPN)).thenReturn(true);
        when(capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_VPN)).thenReturn(false);
        when(capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true);

        return new NetworkFixture(context, wifi);
    }

    private static final class NetworkFixture {
        final Context context;
        final Network network;

        NetworkFixture(Context context, Network network) {
            this.context = context;
            this.network = network;
        }
    }
}
