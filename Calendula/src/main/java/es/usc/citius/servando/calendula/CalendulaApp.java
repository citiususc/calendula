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

package es.usc.citius.servando.calendula;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Locale;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.modules.ModuleManager;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.debug.StethoHelper;

/**
 * Created by castrelo on 4/10/14.
 */
public class CalendulaApp extends MultiDexApplication {

    // INTENTS
    public static final String INTENT_EXTRA_ACTION = "action";
    public static final String INTENT_EXTRA_ROUTINE_ID = "routine_id";
    public static final String INTENT_EXTRA_MEDICINE_ID = "medicine_id";
    public static final String INTENT_EXTRA_SCHEDULE_ID = "schedule_id";
    public static final String INTENT_EXTRA_SCHEDULE_TIME = "schedule_time";
    public static final String INTENT_EXTRA_DELAY_ROUTINE_ID = "delay_routine_id";
    public static final String INTENT_EXTRA_DELAY_SCHEDULE_ID = "delay_schedule_id";
    public static final String INTENT_EXTRA_DATE = "date";
    public static final String INTENT_EXTRA_POSITION = "position";
    // ACTIONS
    public static final int ACTION_ROUTINE_TIME = 1;
    public static final int ACTION_DAILY_ALARM = 2;
    public static final int ACTION_ROUTINE_DELAYED_TIME = 3;
    public static final int ACTION_DELAY_ROUTINE = 4;
    public static final int ACTION_CANCEL_ROUTINE = 5;
    public static final int ACTION_HOURLY_SCHEDULE_TIME = 6;
    public static final int ACTION_HOURLY_SCHEDULE_DELAYED_TIME = 7;
    public static final int ACTION_DELAY_HOURLY_SCHEDULE = 8;
    public static final int ACTION_CANCEL_HOURLY_SCHEDULE = 9;
    public static final int ACTION_CHECK_PICKUPS_ALARM = 10;
    public static final int ACTION_CONFIRM_ALL_ROUTINE = 11;
    public static final int ACTION_CONFIRM_ALL_SCHEDULE = 12;
    // REQUEST CODES
    public static final int RQ_SHOW_ROUTINE = 1;
    public static final int RQ_DELAY_ROUTINE = 2;
    private static final String TAG = "CalendulaApp";
    public static boolean disableReceivers = false;

    private static WeakReference<EventBus> eventBusRef;
    private static Context mContext;


    public static EventBus eventBus() {
        if (eventBusRef == null || eventBusRef.get() == null) {
            eventBusRef = new WeakReference<>(EventBus.getDefault());
        }
        return eventBusRef.get();
    }

    public static Context getContext() {
        return mContext;
    }

    public void exportDatabase(Context context, String databaseName, File out) {
        final File dbPath = context.getDatabasePath(databaseName);

        // If the database already exists, return
        if (!dbPath.exists()) {
            LogUtil.d(TAG, "Database not found");
            return;
        }

        // Try to copy database file
        InputStream inputStream = null;
        OutputStream output = null;
        try {
            inputStream = new FileInputStream(dbPath);
            output = new FileOutputStream(out);
            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer, 0, 8192)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
        } catch (IOException e) {
            LogUtil.e(TAG, "Failed to export database", e);
        } finally {
            try {
                inputStream.close();
                output.close();
            } catch (Exception e) {
                LogUtil.e(TAG, "exportDatabase: ", e);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (!Build.FINGERPRINT.equals("robolectric")) {
            if (BuildConfig.DEBUG) {
                new StethoHelper().init(this);
            }

            if (LeakCanary.isInAnalyzerProcess(CalendulaApp.this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                return;
            }

            //initialize LeakCanary
            LeakCanary.install(CalendulaApp.this);
        }

        final Context applicationContext = getApplicationContext();
        mContext = applicationContext;

        LogUtil.d(TAG, "Application started");

        try {
            LogUtil.d(TAG, "Application flavor is \"" + BuildConfig.FLAVOR + "\"");
            final String flavor = BuildConfig.FLAVOR.toUpperCase();
            ModuleManager.getInstance().runModules(flavor, applicationContext);
        } catch (IllegalArgumentException | IllegalStateException e) {
            LogUtil.e(TAG, "onCreate: Error loading module configuration", e);
            LogUtil.w(TAG, "onCreate: Loading default module configuration instead");
            ModuleManager.getInstance().runDefaultModules(applicationContext);
        }

    }

    @Override
    public void onTerminate() {
        DB.dispose();
        super.onTerminate();
    }

    private void forceLocale(Locale l) {
        Locale locale = new Locale(l.getLanguage());
        Locale.setDefault(locale);
        Configuration config = getApplicationContext().getResources().getConfiguration();
        config.locale = locale;
        getApplicationContext().getResources().updateConfiguration(config, getApplicationContext().getResources().getDisplayMetrics());
    }
}
