/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import es.usc.citius.servando.calendula.CalendulaApp;


public class HttpDownloadUtil {

    private static final String TAG = "HttpDownloadUtil";
    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 8000;

    /**
     * Backwards-compatible overload. New code should pass a Context explicitly so the request
     * cannot start without the VPN-aware network/backend preflight.
     */
    @Deprecated
    public static boolean downloadFile(final String fileUrl, final File file) {
        Context context = CalendulaApp.getContext();
        return context != null && downloadFile(context, fileUrl, file);
    }

    public static boolean downloadFile(
            final Context context,
            final String fileUrl,
            final File file) {
        FileOutputStream fileOutput = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
            connection = openBackendConnection(context, fileUrl);

            fileOutput = new FileOutputStream(file);
            inputStream = connection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            return true;
        } catch (IOException e) {
            LogUtil.e(TAG, "downloadFile: ", e);
            return false;
        } finally {
            CloseableUtil.closeQuietly(fileOutput, inputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Backwards-compatible overload. New code should pass a Context explicitly.
     */
    @Deprecated
    public static String downloadFileToText(final String fileUrl) {
        Context context = CalendulaApp.getContext();
        return context != null ? downloadFileToText(context, fileUrl) : null;
    }

    public static String downloadFileToText(
            final Context context,
            final String fileUrl) {
        BufferedReader in = null;
        HttpURLConnection connection = null;
        try {
            connection = openBackendConnection(context, fileUrl);
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String str;
            StringBuilder sb = new StringBuilder();
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            LogUtil.e(TAG, "downloadFileToText: ", e);
            return null;
        } finally {
            CloseableUtil.closeQuietly(in);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static HttpURLConnection openBackendConnection(
            Context context,
            String fileUrl) throws IOException {
        if (!NetworkUtils.isBackendAvailable(context, fileUrl)) {
            throw new IOException(
                    "No usable Internet route or backend DNS resolution failed for " + fileUrl);
        }

        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            connection.disconnect();
            throw new IOException(
                    "Backend returned HTTP " + responseCode + " for " + fileUrl);
        }
        return connection;
    }
}
