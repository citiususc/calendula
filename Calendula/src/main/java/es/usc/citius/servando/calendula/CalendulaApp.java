package es.usc.citius.servando.calendula;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.joda.time.LocalTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import de.greenrobot.event.EventBus;
import es.usc.citius.servando.calendula.scheduling.AlarmReceiver;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;

/**
 * Created by castrelo on 4/10/14.
 */
public class CalendulaApp extends Application {


    private static final String DB_NAME = "calendula.db";

    // PREFERENCES
    public static final String PREFERENCES_NAME = "CalendulaPreferences";
    public static final String PREF_ALARM_SETTLED = "alarm_settled";

    // INTENTS
    public static final String INTENT_EXTRA_ACTION = "action";
    public static final String INTENT_EXTRA_ROUTINE_ID = "routine_id";
    public static final String INTENT_EXTRA_MEDICINE_ID = "medicine_id";
    public static final String INTENT_EXTRA_SCHEDULE_ID = "schedule_id";
    public static final String INTENT_EXTRA_DELAY_ROUTINE_ID = "delay_routine_id";
    // ACTIONS
    public static final int ACTION_ROUTINE_TIME = 1;
    public static final int ACTION_DAILY_ALARM = 2;
    public static final int ACTION_ROUTINE_DELAYED_TIME = 3;
    public static final int ACTION_DELAY_ROUTINE = 4;
    public static final int ACTION_CANCEL_ROUTINE = 5;


    // REQUEST CODES
    public static final int RQ_SHOW_ROUTINE = 1;
    public static final int RQ_DELAY_ROUTINE = 2;


    private static EventBus eventBus = EventBus.getDefault();

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize sqlite engine
        ActiveAndroid.initialize(this, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DefaultDataGenerator.fillDBWithDummyData(getApplicationContext());
                // initialize daily agenda
                DailyAgenda.instance().setupForToday(CalendulaApp.this);
                // setup alarm for daily agenda update
                setupUpdateDailyAgendaAlarm();
            }
        }).start();

        // create app palette
        //Screen.createPalette(this, Screen.drawableToBitmap(getResources().getDrawable(R.drawable.home_bg_1)));
        // export database to db
        // exportDatabase(this,DB_NAME,new File(Environment.getExternalStorageDirectory()+File.separator+DB_NAME));

        Log.d("APP", Arrays.toString(PreferenceManager.getDefaultSharedPreferences(this).getAll().keySet().toArray()));
    }


    @Override
    public void onTerminate() {
        ActiveAndroid.dispose();
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
