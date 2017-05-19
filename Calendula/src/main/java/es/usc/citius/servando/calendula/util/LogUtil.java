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