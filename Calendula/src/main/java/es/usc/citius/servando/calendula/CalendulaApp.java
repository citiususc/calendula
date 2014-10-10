package es.usc.citius.servando.calendula;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.activeandroid.ActiveAndroid;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.scheduling.AlarmReceiver;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;

/**
 * Created by castrelo on 4/10/14.
 */
public class CalendulaApp extends Application {

    // PREFERENCES
    public static final String PREFERENCES_NAME = "CalendulaPreferences";
    public static final String PREF_ALARM_SETTLED = "alarm_settled";

    // INTENTS
    public static final String INTENT_EXTRA_ACTION = "action";
    public static final String INTENT_EXTRA_ROUTINE_ID = "routine_id";

    // ACTIONS
    public static final int ACTION_ROUTINE_TIME = 1;
    public static final int ACTION_DAILY_ALARM = 2;


    @Override
    public void onCreate() {
        super.onCreate();

        // initialize sqlite engine
        ActiveAndroid.initialize(this);
        // initialize daily agenda
        DailyAgenda.instance().setupForToday(this);
        // setup alarm for daily agenda update
        setupUpdateDailyAgendaAlarm();

    }

    public void setupUpdateDailyAgendaAlarm() {

//        SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
//        boolean alarmAlreadySettled = settings.getBoolean(PREF_ALARM_SETTLED,false);

        //if(!alarmAlreadySettled){

        // intent our receiver will receive
        Intent intent = new Intent(this, AlarmReceiver.class);
        // indicate thar is for a routine
        intent.putExtra(INTENT_EXTRA_ACTION, ACTION_DAILY_ALARM);
        // create pending intent
        int intent_id = 1234567890;
        PendingIntent routinePendingIntent = PendingIntent.getBroadcast(this, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // set the routine alarm, with repetition every day
        if (alarmManager != null) {
            // set a repeating alarm every day at 00:00
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new LocalTime(0, 0).toDateTimeToday().getMillis(), AlarmManager.INTERVAL_DAY, routinePendingIntent);
        }
        // Update preferences
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putBoolean(PREF_ALARM_SETTLED,true);
//            editor.commit();
        //}


    }

}
