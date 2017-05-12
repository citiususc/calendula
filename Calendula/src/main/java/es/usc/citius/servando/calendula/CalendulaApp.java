/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
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

package es.usc.citius.servando.calendula;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.modules.ModuleManager;
import es.usc.citius.servando.calendula.modules.modules.PharmacyModule;

/**
 * Created by castrelo on 4/10/14.
 */
public class CalendulaApp extends Application {

    // INTENTS
    public static final String INTENT_EXTRA_ACTION = "action";
    public static final String INTENT_EXTRA_ROUTINE_ID = "routine_id";
    public static final String INTENT_EXTRA_MEDICINE_ID = "medicine_id";
    public static final String INTENT_EXTRA_SCHEDULE_ID = "schedule_id";
    public static final String INTENT_EXTRA_SCHEDULE_TIME = "schedule_time";
    public static final String INTENT_EXTRA_DELAY_ROUTINE_ID = "delay_routine_id";
    public static final String INTENT_EXTRA_DELAY_SCHEDULE_ID = "delay_schedule_id";
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
    private final static String TAG = "CalendulaApp";
    public static boolean disableReceivers = false;
    private static boolean isOpen;
    private static EventBus eventBus = EventBus.getDefault();
    private static Context mContext;


    public static EventBus eventBus() {
        return eventBus;
    }

    public static boolean isOpen() {
        return isOpen;
    }

    public static boolean isPharmaModeEnabled() {
        return ModuleManager.isEnabled(PharmacyModule.ID);
    }

    public static void open(boolean isOpen) {
        CalendulaApp.isOpen = isOpen;
    }

    public static Context getContext() {
        return mContext;
    }

    public void exportDatabase(Context context, String databaseName, File out) {
        final File dbPath = context.getDatabasePath(databaseName);

        // If the database already exists, return
        if (!dbPath.exists()) {
            Log.d("APP", "Database not found");
            return;
        }

        // Try to copy database file
        try {
            final InputStream inputStream = new FileInputStream(dbPath);
            final OutputStream output = new FileOutputStream(out);

            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer, 0, 8192)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("APP", "Failed to export database", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(CalendulaApp.this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            return;
        }

        final Context applicationContext = getApplicationContext();
        mContext = applicationContext;

        //initialize LeakCanary
        LeakCanary.install(CalendulaApp.this);

        Log.d(TAG, "Application started");

        try {
            Log.d(TAG, "Application flavor is \"" + BuildConfig.FLAVOR + "\"");
            final String flavor = BuildConfig.FLAVOR.toUpperCase();
            ModuleManager.getInstance().runModules(flavor, applicationContext);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.e(TAG, "onCreate: Error loading module configuration", e);
            Log.w(TAG, "onCreate: Loading default module configuration instead");
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
