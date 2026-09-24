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
import org.mockito.InOrder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27)
public class NetworkUtilsTest {

    private static final String BACKEND_URL =
            "https://www.aemps.gob.es/cima/dochtml/p/example";

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
                fixture.context, BACKEND_URL, activeProbe, resolver));

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
                fixture.context, BACKEND_URL, activeProbe, resolver));

        verify(activeProbe).isReachable(fixture.context);
        verifyNoMoreInteractions(resolver);
    }

    @Test
    public void backendDnsFailurePreventsGateFromOpening() throws Exception {
        NetworkFixture fixture = connectedWifi();
        NetworkUtils.ActiveInternetProbe activeProbe =
                mock(NetworkUtils.ActiveInternetProbe.class);
        NetworkUtils.BackendHostResolver resolver =
                mock(NetworkUtils.BackendHostResolver.class);

        when(activeProbe.isReachable(fixture.context)).thenReturn(true);
        when(resolver.resolves("www.aemps.gob.es", fixture.network)).thenReturn(false);

        assertFalse(NetworkUtils.isBackendAvailable(
                fixture.context, BACKEND_URL, activeProbe, resolver));

        verify(activeProbe).isReachable(fixture.context);
        verify(resolver).resolves("www.aemps.gob.es", fixture.network);
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
                context, BACKEND_URL, activeProbe, resolver));

        verify(activeProbe, never()).isReachable(context);
        verifyNoMoreInteractions(resolver);
    }

    @Test
    public void malformedAndNonHttpUrlsNeverStartProbes() throws Exception {
        NetworkFixture fixture = connectedWifi();
        NetworkUtils.ActiveInternetProbe activeProbe =
                mock(NetworkUtils.ActiveInternetProbe.class);
        NetworkUtils.BackendHostResolver resolver =
                mock(NetworkUtils.BackendHostResolver.class);

        assertFalse(NetworkUtils.isBackendAvailable(
                fixture.context, "not a URL", activeProbe, resolver));
        assertFalse(NetworkUtils.isBackendAvailable(
                fixture.context, "file:///tmp/offline.html", activeProbe, resolver));

        verifyNoMoreInteractions(activeProbe, resolver);
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
