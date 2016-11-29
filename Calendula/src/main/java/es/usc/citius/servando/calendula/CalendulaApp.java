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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.mikepenz.iconics.Iconics;

import org.joda.time.LocalTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.PatientDao;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.jobs.CalendulaJobCreator;
import es.usc.citius.servando.calendula.jobs.PurgeCacheJob;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.scheduling.AlarmIntentParams;
import es.usc.citius.servando.calendula.scheduling.AlarmReceiver;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;
import es.usc.citius.servando.calendula.util.Settings;

/**
 * Created by castrelo on 4/10/14.
 */
public class CalendulaApp extends Application {

    public static boolean disableReceivers = false;

    private static boolean isOpen;

    public static final String PHARMACY_MODE_ENABLED = "PHARMACY_MODE_ENABLED";

    // PREFERENCES
    public static final String PREFERENCES_NAME = "CalendulaPreferences";
    public static final String PREF_ALARM_SETTLED = "alarm_settled";

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

    private final static String TAG="CalendulaApp";


    private static EventBus eventBus = EventBus.getDefault();

    public static boolean isOpen() {
        return isOpen;
    }

    public static void open(boolean isOpen) {
        CalendulaApp.isOpen = isOpen;
    }

    SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceUtils.init(getApplicationContext());
        // initialize SQLite engine
        initializeDatabase();
        if (!prefs.getBoolean("DEFAULT_DATA_INSERTED", false)) {
            DefaultDataGenerator.fillDBWithDummyData(getApplicationContext());
            prefs.edit().putBoolean("DEFAULT_DATA_INSERTED", true).commit();
        }

        // initialize daily agenda
        DailyAgenda.instance().setupForToday(this, false);
        // setup alarm for daily agenda update
        setupUpdateDailyAgendaAlarm();
        //exportDatabase(this, DB_NAME, new File(Environment.getExternalStorageDirectory() + File.separator + DB_NAME));
        //forceLocale(Locale.GERMAN);
        //only required if you add a custom or generic font on your own
        Iconics.init(getApplicationContext());
        //register custom fonts like this (or also provide a font definition file)
        Iconics.registerFont(new PresentationsTypeface());

        //load settings
        try {
            Settings.instance().load(getApplicationContext());
        } catch (Exception e) {
            Log.w(TAG, "onCreate: An exception happened when loading settings file");
        }
        updatePreferences();

        //initialize job engine
        JobManager.create(this).addJobCreator(new CalendulaJobCreator());
        PurgeCacheJob.scheduleJob();
    }

    private void updatePreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dbWasEnabled = prefs.getBoolean("enable_prescriptions_db", false);
        SharedPreferences.Editor editor = prefs.edit();

        // replace old "enable db preference" with the default db key (AEMPS)
        if (dbWasEnabled) {
            editor.putString("last_valid_database", DBRegistry.instance().defaultDBMgr().id())
                    .putString("prescriptions_database", DBRegistry.instance().defaultDBMgr().id());
        }
        editor.remove("enable_prescriptions_db");
        editor.commit();
    }

    public static boolean isPharmaModeEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(PHARMACY_MODE_ENABLED, false);
    }


    private void forceLocale(Locale l) {
        Locale locale = new Locale(l.getLanguage());
        Locale.setDefault(locale);
        Configuration config = getApplicationContext().getResources().getConfiguration();
        config.locale = locale;
        getApplicationContext().getResources().updateConfiguration(config, getApplicationContext().getResources().getDisplayMetrics());
    }

    public void initializeDatabase() {
        DB.init(this);
        DBRegistry.init(this);
        try {
            if (DB.patients().countOf() == 1) {
                Patient p = DB.patients().getDefault();
                prefs.edit().putLong(PatientDao.PREFERENCE_ACTIVE_PATIENT, p.id()).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTerminate() {
        DB.dispose();
        super.onTerminate();
    }

    public void setupUpdateDailyAgendaAlarm() {
        // intent our receiver will receive
        Intent intent = new Intent(this, AlarmReceiver.class);
        AlarmIntentParams params = AlarmIntentParams.forDailyUpdate();
        AlarmScheduler.setAlarmParams(intent, params);
        PendingIntent dailyAlarm = PendingIntent.getBroadcast(this, params.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new LocalTime(0, 0).toDateTimeToday().getMillis(), AlarmManager.INTERVAL_DAY, dailyAlarm);
        }
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

    public static EventBus eventBus() {
        return eventBus;
    }


    public static String activePatientAuth(Context ctx) {
        Long id = DB.patients().getActive(ctx).id();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("remote_token" + id, null);
    }
}
