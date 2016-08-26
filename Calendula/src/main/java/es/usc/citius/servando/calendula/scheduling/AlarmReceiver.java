package es.usc.citius.servando.calendula.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.persistence.Routine;

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

            long repeatMillisec = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_REPEAT_MILLISEC, -1);
            long routineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);

            if (repeatMillisec != -1 && routineId != -1) {

                PendingIntent routinePendingIntent = AlarmScheduler.alarmPendingIntent(context, Routine.findById(routineId), repeatMillisec);

                // Get the AlarmManager service
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                // set the routine alarm, with repetition every day
                if (alarmManager != null) {
                    // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, routine.time().toDateTimeToday().getMillis(), AlarmManager.INTERVAL_DAY, routinePendingIntent);
                    if (Build.VERSION.SDK_INT >= 23) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, repeatMillisec, routinePendingIntent);
                    } else if (Build.VERSION.SDK_INT >= 19) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, repeatMillisec, routinePendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, repeatMillisec, routinePendingIntent);
                    }

                    Log.d(TAG, "Alarm rescheduled to " + repeatMillisec + " millis");
                }

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