package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;

/**
 * This class receives our routine alarms
 */
public class NotificationEventReceiver extends BroadcastReceiver {

    public static final String TAG = NotificationEventReceiver.class.getName();


    @Override
    public void onReceive(Context context, Intent intent) {

        long routineId;
        long scheduleId;
        String scheduleTime;
        LocalDate date;

        int action = intent.getIntExtra(CalendulaApp.INTENT_EXTRA_ACTION, -1);

        Log.d(TAG, "Notification event received - Action : " + action);

        String dateStr = intent.getStringExtra("date");
        if (dateStr != null) {
            date = DateTimeFormat.forPattern(AlarmIntentParams.DATE_FORMAT).parseLocalDate(dateStr);
        } else {
            Log.w(TAG, "Date not supplied, assuming today.");
            date = LocalDate.now();
        }

        switch (action) {

            case CalendulaApp.ACTION_CANCEL_ROUTINE:
                routineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
                if (routineId != -1) {
                    AlarmScheduler.instance().cancelStatusBarNotification(Routine.findById(routineId), date, context);
                    Toast.makeText(context, context.getString(R.string.reminder_cancelled_message), Toast.LENGTH_SHORT).show();
                }
                break;

            case CalendulaApp.ACTION_CANCEL_HOURLY_SCHEDULE:
                scheduleId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1);
                scheduleTime = intent.getStringExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME);
                if (scheduleId != -1 && scheduleTime != null) {
                    LocalTime t = DateTimeFormat.forPattern("kk:mm").parseLocalTime(scheduleTime);
                    AlarmScheduler.instance().cancelStatusBarNotification(Schedule.findById(scheduleId), t, date, context);
                    Toast.makeText(context,context.getString(R.string.reminder_cancelled_message),Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Log.d(TAG, "Request not handled " + intent.toString());
                break;
        }

    }

}


//case CalendulaApp.ACTION_DELAY_HOURLY_SCHEDULE:
//        scheduleId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1);
//        scheduleTime = intent.getStringExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME);
//        if (scheduleId != -1 && scheduleTime != null)
//        {
//        SharedPreferences prefs =
//        PreferenceManager.getDefaultSharedPreferences(context);
//        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");
//        long delay = Long.parseLong(delayMinutesStr);
//        if (delay < 0)
//        {
//        delay = 15;
//        }
//        Schedule s = Schedule.findById(scheduleId);
//        LocalTime t =
//        DateTimeFormat.forPattern("kk:mm").parseLocalTime(scheduleTime);
//        AlarmScheduler.instance().onDelayHourlySchedule(s, t, date, context, (int) delay);
//        Toast.makeText(context,
//        context.getString(R.string.reminder_delayed_message),
//        Toast.LENGTH_SHORT).show();
//        }
//        break;