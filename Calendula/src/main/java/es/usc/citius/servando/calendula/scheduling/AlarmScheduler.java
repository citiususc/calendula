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

    public static final int DEFAULT_ALARM_TIME_MARGIN = 2 * 60 * 1000; // 2 hours
    public static final int ALARM_AUTO_DELAY_MILLIS = 1 * 60 * 1000;  // 5 mins
    public static final int ALARM_USER_DELAY_MILLIS = 60 * 60 * 1000; // 1 hour

    private static final String TAG = AlarmScheduler.class.getName();

    /**
     * Set an alarm to an specific routine time using the
     * android AlarmManager service.
     *
     * @param routine The routine whose alarm will be set
     */
    private void setAlarm(Routine routine, Context ctx) {

        // set the routine alarm only if there are schedules associated
        if (ScheduleUtils.getRoutineScheduleItems(routine, false).size() > 0) {
            Log.d(TAG, "Updating routine alarm [" + routine.name() + "]");
            // get routine pending intent
            PendingIntent routinePendingIntent = alarmPendingIntent(ctx, routine);
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

    private void cancelAlarm(Routine routine, Context ctx) {
        // get routine pending intent
        PendingIntent routinePendingIntent = alarmPendingIntent(ctx, routine);
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(routinePendingIntent);
        }
    }


    /**
     * Set an alarm to an specific routine time using the
     * android AlarmManager service.
     *
     * @param routine The routine whose alarm will be set
     */
    private void delayAlarm(Routine routine, int millis, Context ctx) {

        // set the routine alarm only if there are schedules associated
        if (ScheduleUtils.getRoutineScheduleItems(routine, false).size() > 0) {
            Log.d(TAG, "Updating routine alarm [" + routine.name() + "]");
            // get delay routine pending intent
            PendingIntent routinePendingIntent = alarmDelayPendingIntent(ctx, routine);
            // Get the AlarmManager service
            AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            // set the routine alarm, with repetition every day
            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, DateTime.now().getMillis() + millis, routinePendingIntent);
                Log.d(TAG, "Alarm delayed " + millis + " millis");
            }
        }
    }

    private void cancelDelayedAlarm(Routine routine, Context ctx) {
        // get delay routine pending intent
        PendingIntent routinePendingIntent = alarmDelayPendingIntent(ctx, routine);
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
    private void setAlarmsIfNeeded(Schedule schedule, Context ctx) {
        Log.d(TAG, "Setting alarm for schedule [" + schedule.medicine().name() + "] with " + schedule.items().size() + " items");
        for (ScheduleItem scheduleItem : schedule.items()) {
            setAlarm(scheduleItem.routine(), ctx);
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
        if (!doses.isEmpty()) {
            final Intent intent = new Intent(ctx, StartActivity.class);
            intent.putExtra("action", StartActivity.ACTION_SHOW_REMINDERS);
            intent.putExtra("routine_id", routine.getId());
            ReminderNotification.notify(ctx, ctx.getResources().getString(R.string.meds_time), routine, doses, intent);

            // set auto delay for within 5 minutes
            delayAlarm(routine, ALARM_AUTO_DELAY_MILLIS, ctx);
        }
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

    // PUBLIC INTERFACE

    /**
     * Called by the when an previously established alarm is received.
     *
     * @param routineId the id of a routine
     */
    public void onAlarmReceived(Long routineId, Context ctx) {


        Routine routine = Routine.findById(routineId);
        if (routine != null) {
            Log.d(TAG, "onAlarmReceived: " + routine.getId() + ", " + routine.name());
            if (isWithinDefaultMargins(routine)) {
                onRoutineLost(routine);
            } else {
                onRoutineTime(routine, ctx);
            }
        } else {
            Log.d(TAG, "onAlarmReceived: " + routineId + ", null routine");
        }
    }


    /**
     * @param r
     */
    public void onCreateOrUpdateRoutine(Routine r, Context ctx) {
        Log.d(TAG, "onCreateOrUpdateRoutine: " + r.getId() + ", " + r.name());
        setAlarm(r, ctx);
    }

    /**
     *
     * @param s
     */
    public void onCreateOrUpdateSchedule(Schedule s, Context ctx) {
        Log.d(TAG, "onCreateOrUpdateSchedule: " + s.getId() + ", " + s.medicine().name());
        setAlarmsIfNeeded(s, ctx);
    }

    /**
     *
     * @param r
     */
    public void onDeleteRoutine(Routine r, Context ctx) {
        Log.d(TAG, "onDeleteRoutine: " + r.getId() + ", " + r.name());
        cancelAlarm(r, ctx);
    }

    /**
     *
     * @param r
     */
    public void onDelayRoutine(Routine r, Context ctx, int delay) {
        Log.d(TAG, "onDelayRoutine: " + r.getId() + ", " + r.name());
        // check this routine is not future
        if (isWithinDefaultMargins(r)) {
            delayAlarm(r, delay, ctx);
            ReminderNotification.cancel(ctx);
        }
    }

    /**
     *
     * @param rId
     */
    public void onDelayRoutine(long rId, Context ctx, int delay) {
        Routine r = Routine.findById(rId);
        if (r != null) {
            onDelayRoutine(r, ctx, delay);
        }
    }

    public void onCancelRoutineNotifications(Routine r, Context ctx) {
        Log.d(TAG, "onCancelRoutineNotifications: " + r.getId() + ", " + r.name());
        // canclel alarms related to delayed notifications
        cancelDelayedAlarm(r, ctx);
        // cancel notification
        ReminderNotification.cancel(ctx);
    }

    /**
     * Check if the current time is in the time interval specified by the routine time and
     * routine time + DEFAULT_ALARM_TIME_MARGIN
     *
     * @param r
     * @return
     */
    private boolean isWithinDefaultMargins(Routine r) {
        DateTime now = DateTime.now();
        DateTime routineTime = r.time().toDateTimeToday();
        return routineTime.isBefore(now) && routineTime.plusMillis(DEFAULT_ALARM_TIME_MARGIN).isAfter(now);
    }


    public static PendingIntent alarmPendingIntent(Context ctx, Routine routine) {
        // intent our receiver will receive
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        // indicate thar is for a routine
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_ROUTINE_TIME);
        // pass the routine id (hash code)
        Log.d(TAG, "Put extra " + CalendulaApp.INTENT_EXTRA_ROUTINE_ID + ": " + routine.getId());
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, routine.getId());
        // create pending intent
        int intent_id = routine.getId().hashCode();
        return PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent alarmDelayPendingIntent(Context ctx, Routine routine) {
        // intent our receiver will receive
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        // indicate thar is for a routine
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_ROUTINE_DELAYED_TIME);
        // pass the routine id (hash code)
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, routine.getId());
        // create pending intent
        int intent_id = routine.getId().hashCode() + 100;
        return PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
