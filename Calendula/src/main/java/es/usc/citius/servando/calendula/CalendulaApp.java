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
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.scheduling.AlarmReceiver;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;

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


    // REQUEST CODES
    public static final int RQ_SHOW_ROUTINE = 1;
    public static final int RQ_DELAY_ROUTINE = 2;
    
    private static EventBus eventBus = EventBus.getDefault();

    public static boolean isOpen() {
        return isOpen;
    }

    public static void open(boolean isOpen) {
        CalendulaApp.isOpen = isOpen;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // initialize SQLite engine
        initializeDatabase();
        // create default patient
        createDefaultPatient(this);

        DefaultDataGenerator.fillDBWithDummyData(getApplicationContext());
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
    }

    private void createDefaultPatient(Context ctx) {
//        try {
//            if(DB.patients().countOf() <= 0){
//                // Create default
//                Patient p = new Patient();
//                p.setName("Usuario"); // R.string.default_patient_name
//                p.setDefault(true);
//                DB.patients().save(p);
//                SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(ctx);
//                prefs.edit().putLong(PatientDao.PREFERENCE_ACTIVE_PATIENT,p.id()).commit();
//            }
//
//            // For testing purposes
//            if(DB.patients().countOf() <= 1){
//
//                // Create default
//                Patient p = new Patient();
//                p.setName("Angel"); // R.string.default_patient_name
//                p.setAvatar(AvatarMgr.AVATAR_5);
//                DB.patients().save(p);
//
//                p = new Patient();
//                p.setName("Avó"); // R.string.default_patient_name
//                p.setAvatar(AvatarMgr.AVATAR_2);
//                DB.patients().save(p);
//
//                p = new Patient();
//                p.setName("Mamá"); // R.string.default_patient_name
//                p.setAvatar(AvatarMgr.AVATAR_7);
//                DB.patients().save(p);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    public static boolean isPharmaModeEnabled(Context ctx){
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(ctx);
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
        try{
            if(DB.patients().countOf() == 1) {
                SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
                Patient p = DB.patients().getDefault();
                prefs.edit().putLong(PatientDao.PREFERENCE_ACTIVE_PATIENT, p.id()).commit();
            }
        }catch (Exception e){
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
        // indicate thar is for a routine
        intent.putExtra(INTENT_EXTRA_ACTION, ACTION_DAILY_ALARM);
        // create pending intent
        int intent_id = 1234567890;
        PendingIntent routinePendingIntent = PendingIntent.getBroadcast(this, intent_id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // set the routine alarm, with repetition every day
        if (alarmManager != null) {
            // set a repeating alarm every day at 00:00
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new LocalTime(0, 0).toDateTimeToday().getMillis(), AlarmManager.INTERVAL_DAY, routinePendingIntent);
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


}
