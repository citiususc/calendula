package es.usc.citius.servando.calendula.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ReminderNotification;
import es.usc.citius.servando.calendula.activities.StartActivity;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
public class AlarmScheduler {

    private static final String TAG = AlarmScheduler.class.getName();

    // TODO: Handle routine time changes!

    /**
     * Set an alarm to an specific routine time using the
     * android AlarmManager service. This alarm will
     *
     * @param routine The routine whose alarm will be set
     */
    public void setAlarm(Routine routine, Context ctx) {

        // set the routine alarm only if there are schedules associated
        if (ScheduleUtils.getRoutineScheduleItems(routine, false).size() > 0) {
            Log.d(TAG, "Updating routine alarm [" + routine.name() + "]");
            // intent our receiver will receive
            Intent intent = new Intent(ctx, AlarmReceiver.class);
            // indicate thar is for a routine
            intent.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_ROUTINE_TIME);
            // pass the routine id (hash code)
            Log.d(TAG, "Put extra " + CalendulaApp.INTENT_EXTRA_ROUTINE_ID + ": " + routine.getId());
            intent.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, routine.getId());
            // create pending intent
            int intent_id = routine.getId().hashCode();
            PendingIntent routinePendingIntent = PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Get the AlarmManager service
            AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            // set the routine alarm, with repetition every day
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, routine.time().toDateTimeToday().getMillis(), AlarmManager.INTERVAL_DAY, routinePendingIntent);
                Duration timeToAlarm = new Duration(LocalTime.now().toDateTimeToday(), routine.time().toDateTimeToday());
                Log.d(TAG, "Alarm scheduled to " + timeToAlarm.getMillis() + " millis");
            }
        }
    }


    /**
     * Set an alarm to an specific routine time using the
     * android AlarmManager service. This alarm will
     *
     * @param routine The routine whose alarm will be set
     */
    public void delayAlarm(Routine routine, int millis, Context ctx) {

        // set the routine alarm only if there are schedules associated
        if (ScheduleUtils.getRoutineScheduleItems(routine, false).size() > 0) {
            Log.d(TAG, "Updating routine alarm [" + routine.name() + "]");
            // intent our receiver will receive
            Intent intent = new Intent(ctx, AlarmReceiver.class);
            // indicate thar is for a routine
            intent.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_ROUTINE_TIME);
            // pass the routine id (hash code)
            intent.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, routine.getId());
            // create pending intent
            int intent_id = routine.getId().hashCode();
            PendingIntent routinePendingIntent = PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Get the AlarmManager service
            AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            // set the routine alarm, with repetition every day
            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, DateTime.now().getMillis() + millis, routinePendingIntent);
                Log.d(TAG, "Alarm delayed " + millis + " millis");
            }
        }
    }


    public void cancelAlarm(Routine routine, Context ctx) {
        // intent we have sent to set the alarm
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        // indicate thar is for a routine
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_ROUTINE_TIME);
        // pass the routine id
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, routine.getId());
        // create pending intent
        int intent_id = routine.getId().hashCode();
        PendingIntent routinePendingIntent = PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(routinePendingIntent);
        }
    }


    /**
     * Set the alarms for the routines of an specific schedule
     * if they are not yet settled
     *
     * @param schedule The routine whose alarm will be set
     */
    public void setAlarmsIfNeeded(Schedule schedule, Context ctx) {
        Log.d(TAG, "Setting alarm for schedule [" + schedule.medicine().name() + "] with " + schedule.items().size() + " items");
        for (ScheduleItem scheduleItem : schedule.items()) {
            setAlarm(scheduleItem.routine(), ctx);
        }
    }

    /**
     * Set the alarms for the daily routines for the current
     * data. This will be called everyday at 00:00h
     */
    public void scheduleDailyAlarms(Context ctx) {
        for (Routine r : Routine.findAll()) {
            setAlarm(r, ctx);
        }
    }

    /**
     * Cancels the alarms of the daily routines
     */
    public void cancelDailyAlarms(Context ctx) {
        for (Routine r : Routine.findAll()) {
            cancelAlarm(r, ctx);
        }
    }

    /**
     * Called by the when an previously established alarm is received.
     *
     * @param routineId the id of a routine
     */
    public void onAlarmReceived(Long routineId, Context ctx) {

        Routine routine = Routine.findById(routineId);
        if (routine != null) {
            LocalTime now = LocalTime.now();
            LocalTime time = routine.time();
            if (time.isBefore(now.minusMinutes(60))) {
                // we are receiving an alarm for a routine that is in the past,
                // probably because the application has started just now.
                // We need to log and inform user
                onRoutineLost(routine);
            } else {
                onRoutineTime(routine, ctx);
            }


        }
    }

    /**
     * Called when this class receives an alarm from the AlarmReceiver,
     * and the routine time is after the current time
     *
     * @param routine
     */
    private void onRoutineTime(Routine routine, Context ctx) {

        // get the schedule items for the current routine, excluding already taken
        List<ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine, false);
        final Intent intent = new Intent(ctx, StartActivity.class);
        intent.putExtra("action", StartActivity.ACTION_SHOW_REMINDERS);
        intent.putExtra("routine_id", routine.getId());
        ReminderNotification.notify(ctx, ctx.getResources().getString(R.string.meds_time), routine, doses, intent);

    }

    /**
     * Called when this class receives an alarm from the AlarmReceiver,
     * and the routine time has passed
     *
     * @param routine
     */
    private void onRoutineLost(Routine routine) {
        // get the schedule items for the current routine, excluding already taken
        List<ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine, false);
        Log.d(TAG, doses + " deses lost today!"); // TODO: handle this
    }

    // SINGLETON

    private AlarmScheduler() {
    }

    // static instance
    private static final AlarmScheduler instance = new AlarmScheduler();

    // static method to get the AlarmScheduler instance
    public static AlarmScheduler instance() {
        return instance;
    }

}
