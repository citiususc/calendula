package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.persistence.Routine;

/**
 * This class receives our routine alarms
 */
public class NotificationEventReceiver extends BroadcastReceiver {

    public static final String TAG = NotificationEventReceiver.class.getName();


    @Override
    public void onReceive(Context context, Intent intent) {
        {
            long routineId;
            // get action type
            int action = intent.getIntExtra(CalendulaApp.INTENT_EXTRA_ACTION, -1);

            Log.d(TAG, "Notification event received - Action : " + action);

            switch (action) {

                case CalendulaApp.ACTION_DELAY_ROUTINE:
                    routineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
                    if (routineId != -1) {

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");
                        long delay = Long.parseLong(delayMinutesStr);
                        if (delay < 0) {
                            delay = 15;
                        }
                        AlarmScheduler.instance().onDelayRoutine(routineId, context, (int) delay);
                        Toast.makeText(context, "Routine delayed " + delay + " minutes", Toast.LENGTH_SHORT).show();

                    }
                    break;

                case CalendulaApp.ACTION_CANCEL_ROUTINE:
                    routineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
                    if (routineId != -1) {
                        AlarmScheduler.instance().onCancelRoutineNotifications(Routine.findById(routineId), context);
                        Toast.makeText(context, "Reminder cancelled", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

}