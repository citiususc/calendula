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

import android.util.Log;

import es.usc.citius.servando.calendula.BuildConfig;


public class LogUtil {

    private static final boolean ENABLE_LOGS = !BuildConfig.BUILD_TYPE.equalsIgnoreCase("release");


    private LogUtil() {
    }


    public static void d(final String tag, String message) {
        if (ENABLE_LOGS) {
                Log.d(tag, message);
        }
    }

    public static void d(final String tag, String message, Throwable cause) {
        if (ENABLE_LOGS) {
                Log.d(tag, message, cause);
        }
    }

    public static void v(final String tag, String message) {
        if (ENABLE_LOGS) {
                Log.v(tag, message);
        }
    }

    public static void v(final String tag, String message, Throwable cause) {
        if (ENABLE_LOGS) {
                Log.v(tag, message, cause);
        }
    }

    public static void i(final String tag, String message) {
        if (ENABLE_LOGS) {
            Log.i(tag, message);
        }
    }

    public static void i(final String tag, String message, Throwable cause) {
        if (ENABLE_LOGS) {
            Log.i(tag, message, cause);
        }
    }

    public static void w(final String tag, String message) {
        Log.w(tag, message);
    }

    public static void w(final String tag, String message, Throwable cause) {
        Log.w(tag, message, cause);
    }

    public static void wtf(final String tag, String message) {
        Log.wtf(tag, message);
    }

    public static void wtf(final String tag, String message, Throwable cause) {
        Log.wtf(tag, message, cause);
    }

    public static void e(final String tag, String message) {
        Log.e(tag, message);
    }

    public static void e(final String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);
    }
}