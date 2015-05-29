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

    public static final String TAG = "AlarmReceiver.class";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        {
            Log.d(TAG, "onReceive");
            if (CalendulaApp.disableReceivers)
            {
                return;
            }

            // get action type
            int action = intent.getIntExtra(CalendulaApp.INTENT_EXTRA_ACTION, -1);
            Log.d(TAG, "Alarm received (Action : " + action + ")");

            switch (action)
            {
                case CalendulaApp.ACTION_ROUTINE_TIME:
                    Log.d(TAG, "Action ACTION_ROUTINE_TIME");
                    onRoutineAlarmReceived(context, intent);
                    break;
                case CalendulaApp.ACTION_ROUTINE_DELAYED_TIME:
                    Log.d(TAG, "Action ACTION_ROUTINE_DELAYED_TIME");
                    onRoutineAlarmReceived(context, intent);
                    break;
                case CalendulaApp.ACTION_HOURLY_SCHEDULE_TIME:
                    Log.d(TAG, "Action ACTION_HOURLY_SCHEDULE_TIME");
                    onHourlyScheduleAlarmReceived(context, intent);
                    break;
                case CalendulaApp.ACTION_HOURLY_SCHEDULE_DELAYED_TIME:
                    Log.d(TAG, "Action ACTION_HOURLY_SCHEDULE_DELAYED_TIME");
                    onHourlyScheduleAlarmReceived(context, intent);
                    break;
                case CalendulaApp.ACTION_DAILY_ALARM:
                    Log.d(TAG, "Action ACTION_DAILY_ALARM");
                    onDailyAgendaAlarmReceived(context, intent);
                    break;
                default:
                    Log.w(TAG, "Unknown action");
                    break;
            }
           /* // routine time or routine delayed reminder
            if ((action == CalendulaApp.ACTION_ROUTINE_TIME) || (action == CalendulaApp.ACTION_ROUTINE_DELAYED_TIME)) {
                onRoutineAlarmReceived(context,intent);
            } else if ((action == CalendulaApp.ACTION_HOURLY_SCHEDULE_TIME) || (action == CalendulaApp.ACTION_HOURLY_SCHEDULE_DELAYED_TIME)) {
                onHourlyScheduleAlarmReceived(context,intent);
            } else if (action == CalendulaApp.ACTION_DAILY_ALARM) {
                onDailyAgendaAlarmReceived();
            }*/
        }
    }

    public void onRoutineAlarmReceived(Context context, Intent intent)
    {
        // get the routine hash code from the intent
        Long routineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
        Log.d(TAG, "Routine id: " + routineId);
        AlarmScheduler.instance().onAlarmReceived(routineId, context.getApplicationContext());
    }

    public void onHourlyScheduleAlarmReceived(Context context, Intent intent)
    {
        // get the schedule id from the intent
        Long scheduleId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1);
        String scheduleTime = intent.getStringExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME);
        Log.d(TAG, "Hourly schedule id: " + scheduleId);
        AlarmScheduler.instance()
            .onHourlyAlarmReceived(scheduleId, scheduleTime, context.getApplicationContext());
    }

    public void onDailyAgendaAlarmReceived(Context context, Intent intent)
    {
        Log.d(TAG, "Received update daily agenda event");
        DailyAgenda.instance().setupForToday(context.getApplicationContext(), false);
    }
}