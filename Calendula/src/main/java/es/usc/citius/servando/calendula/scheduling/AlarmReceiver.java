package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import es.usc.citius.servando.calendula.CalendulaApp;

/**
 * This class receives our routine alarms
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = AlarmReceiver.class.getName();


    @Override
    public void onReceive(Context context, Intent intent) {
        {
            // get action type
            int action = intent.getIntExtra(CalendulaApp.INTENT_EXTRA_ACTION, -1);

            Log.d(TAG, "Alarm received - Action : " + action);

            // routine time
            if (action == CalendulaApp.ACTION_ROUTINE_TIME) {

                // get the routine hash code from the intent
                Long routineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
                Log.d(TAG, "Routine id: " + routineId);
                // and call scheduler
                AlarmScheduler.instance().onAlarmReceived(routineId, context);
            } else if (action == CalendulaApp.ACTION_DAILY_ALARM) {
                Log.d(TAG, "Received update daily agenda event");
                // update daily agenda
                DailyAgenda.instance().setupForToday(context);
            }
        }
    }

}