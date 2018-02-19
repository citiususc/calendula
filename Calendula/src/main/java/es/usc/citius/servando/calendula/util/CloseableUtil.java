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

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by alvaro.brey.vilas on 19/02/18.
 */

public class CloseableUtil {

    private static final String TAG = "CloseableUtil";

    /**
     * Ensures closing of Closeables (for streams, mostly). Checks for null, tries to close and catches exceptions. Logs them and throws a runtime if specified.
     *
     * @param object       the closeable stream
     * @param throwRuntime if <code>true</code>, throw runtime exception if closing throws one. If <code>false</code> just log it.
     */
    public static void close(Closeable object, final boolean throwRuntime) {
        if (object != null) {
            try {
                object.close();
            } catch (IOException e) {
                if (throwRuntime) {
                    throw new RuntimeException(e);
                } else {
                    LogUtil.e(TAG, "close: ", e);
                }
            }
        }
    }


    /**
     * Closes the objects and logs any exceptions.
     *
     * @param objects the closeable objects
     * @see CloseableUtil#close(Closeable, boolean)
     */
    public static void closeQuietly(Closeable... objects) {
        for (Closeable object : objects) {
            close(object, false);
        }
    }

}
