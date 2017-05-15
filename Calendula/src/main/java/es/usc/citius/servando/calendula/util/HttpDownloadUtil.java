/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2017 CITIUS - USC
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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by alvaro.brey.vilas on 04/01/17.
 */

public class HttpDownloadUtil {

    private final static String TAG = "HttpDownloadUtil";

    public static boolean downloadFile(final String fileUrl, final File file) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();
            return true;
        } catch (MalformedURLException e) {
            LogUtil.e(TAG, "downloadFile: ", e);
            return false;
        } catch (IOException e) {
            LogUtil.e(TAG, "downloadFile: ", e);
            return false;
        }
    }

    public static String downloadFileToText(final String fileUrl) {
        try {
            // Create a URL for the desired page
            URL url = new URL(fileUrl);

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            StringBuilder sb = new StringBuilder();
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
            in.close();
            return sb.toString();
        } catch (IOException e) {
            LogUtil.e(TAG, "downloadFileToText: ", e);
            return null;
        }
    }

}
