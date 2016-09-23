package es.usc.citius.servando.calendula.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ConfirmActivity;
import es.usc.citius.servando.calendula.activities.ReminderNotification;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 *
 */
public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    public static final String EXTRA_PARAMS = "alarm_params";

    private static final AlarmScheduler instance = new AlarmScheduler();

    private AlarmScheduler() {

    }

    // static method to get the AlarmScheduler instance
    public static AlarmScheduler instance() {
        return instance;
    }

    public static PendingIntent pendingIntent(Context ctx, Routine routine, LocalDate date, boolean delayed){
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        AlarmIntentParams params = AlarmIntentParams.forRoutine(routine.getId(), date, delayed);
        intent.putExtra(EXTRA_PARAMS, params);
        return PendingIntent.getBroadcast(ctx, params.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent pendingIntent(Context ctx, Schedule schedule, LocalTime time, LocalDate date, boolean delayed){
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        AlarmIntentParams params = AlarmIntentParams.forSchedule(schedule.getId(), time, date, delayed);
        intent.putExtra(EXTRA_PARAMS, params);
        return PendingIntent.getBroadcast(ctx, params.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public Long getAlarmRepeatFreq(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");
        return Long.parseLong(delayMinutesStr);
    }

    /**
     * Set an alarm for a routine
     */
    private void setFirstAlarm(Routine routine, LocalDate date, Context ctx) {
        long timestamp = date.toDateTime(routine.time()).getMillis();
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, date, false);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timestamp, AlarmManager.INTERVAL_DAY, routinePendingIntent);
        }
    }

    /**
     * Set an alarm for a repeating schedule item
     */
    private void setFirstAlarm(Schedule schedule, LocalTime time, LocalDate date, Context ctx) {
        DateTime dateTime = date.toDateTime(time);
        PendingIntent routinePendingIntent = pendingIntent(ctx, schedule, time, date, false);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dateTime.getMillis(), AlarmManager.INTERVAL_DAY, routinePendingIntent);
        }
    }

    private void setRepeatAlarm(Routine routine, AlarmIntentParams firstParams, Context ctx, long delayMillis) {
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, firstParams.date(), true);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, DateTime.now().getMillis() + delayMillis, routinePendingIntent);
        }
    }

    private void setRepeatAlarm(Schedule schedule, AlarmIntentParams firstParams, Context ctx, long delayMillis) {
        PendingIntent routinePendingIntent = pendingIntent(ctx, schedule, firstParams.scheduleTime(), firstParams.date(), true);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, DateTime.now().getMillis() + delayMillis, routinePendingIntent);
        }
    }

    private void cancelAlarm(Routine routine, LocalDate date, Context ctx)
    {
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, date, false);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(routinePendingIntent);
        }
    }

    private void cancelDelayedAlarm(Routine routine, LocalDate date, Context ctx) {

        // when cancelling reminder, update time taken to now, but don't set as taken

        for (ScheduleItem scheduleItem : routine.scheduleItems()) {
            DailyScheduleItem ds = DailyScheduleItem.findByScheduleItem(scheduleItem);
            ds.setTimeTaken(LocalTime.now());
            ds.save();
            Log.d(TAG, "Set time taken to " + ds.scheduleItem().schedule().medicine().name());

        }

        // get delay routine pending intent
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, date, true);
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(routinePendingIntent);
        }
    }

    private void cancelHourlyDelayedAlarm(Schedule s, LocalTime t, LocalDate date, Context ctx) {

        DailyScheduleItem ds = DB.dailyScheduleItems().findByScheduleAndTime(s, t);
        if(ds!=null) {
            ds.setTimeTaken(LocalTime.now());
            ds.save();

            // get hourly delay pending intent
            PendingIntent pendingIntent = pendingIntent(ctx, s, t, date, true);
            // Get the AlarmManager service
            AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private void setAlarmsIfNeeded(Schedule schedule, LocalDate date, Context ctx) {
        if (!schedule.repeatsHourly()) {
            for (ScheduleItem scheduleItem : schedule.items()) {
                if (scheduleItem.routine() != null) {
                    setFirstAlarm(scheduleItem.routine(), date, ctx);
                }
            }
        } else {
            List<DateTime> times = schedule.hourlyItemsAt(date.toDateTimeAtStartOfDay());
            for (DateTime time : times) {
                setFirstAlarm(schedule, time.toLocalTime(), date, ctx);
            }
        }
    }

    private void onRoutineTime(Routine routine, AlarmIntentParams firstParams, Context ctx) {

        // get the schedule items for the current routine, excluding already taken
        List<ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine, false);

        Log.d(TAG, "OnRoutineTime - ScheduleItems: " + doses.size());
        if (!doses.isEmpty()) {

            boolean notify = false;
            // check if all items have timeTaken (cancelled notifications)            
            for (ScheduleItem scheduleItem : doses) {
                DailyScheduleItem ds = DailyScheduleItem.findByScheduleItem(scheduleItem);
                if (ds != null && ds.timeTaken() == null) {
                    Log.d(TAG, ds.scheduleItem().schedule().medicine().name() + " not checked or cancelled. Notify!");
                    notify = true;
                    break;
                }
            }

            if (notify) {
                final Intent intent = new Intent(ctx, ConfirmActivity.class);
                intent.putExtra("routine_id", routine.getId());
                intent.putExtra("date", firstParams.date);
                ReminderNotification.notify(ctx, ctx.getResources().getString(R.string.meds_time), routine, doses, intent);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                boolean repeatAlarms = prefs.getBoolean("alarm_repeat_enabled", false);
                if (repeatAlarms) {
                    setRepeatAlarm(routine, firstParams, ctx, getAlarmRepeatFreq(ctx)*60*1000);
                }
            }
        }
    }

    private void onHourlyScheduleTime(Schedule schedule, AlarmIntentParams firstParams, Context ctx) {

        boolean notify = false;
        // check if this item has timeTaken (cancelled notifications)

        DailyScheduleItem ds = DB.dailyScheduleItems().findBy(schedule, firstParams.date(), firstParams.scheduleTime());
        if (ds != null && ds.timeTaken() == null)
        {
            notify = true;
        }

        if (notify)
        {
            final Intent intent = new Intent(ctx, ConfirmActivity.class);
            intent.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
            intent.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, firstParams.scheduleTime);
            intent.putExtra("date", firstParams.date);

            String title = ctx.getResources().getString(R.string.meds_time);
            ReminderNotification.notify(ctx, title, schedule, firstParams.scheduleTime(), intent);

            // Handle delay if needed
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            boolean repeatAlarms = prefs.getBoolean("alarm_repeat_enabled", false);
            if (repeatAlarms)
            {
                setRepeatAlarm(schedule, firstParams, ctx, getAlarmRepeatFreq(ctx) * 60 * 1000);
            }
        }
    }

    private void onRoutineLost(Routine routine) {
        // get the schedule items for the current routine, excluding already taken
        List<ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine, false);
        Log.d(TAG, doses + " deses lost today!"); // TODO: handle this
    }

    public void onAlarmReceived(AlarmIntentParams params, Context ctx) {

        Routine routine = Routine.findById(params.routineId);
        if (routine != null) {
            Log.d(TAG, "onAlarmReceived: " + routine.getId() + ", " + routine.name());
            if (isWithinDefaultMargins(routine, params.date(), ctx)) {
                onRoutineTime(routine, params, ctx);
            } else {
                onRoutineLost(routine);
            }
        }
    }

    public void onHourlyAlarmReceived(AlarmIntentParams params, Context ctx) {
        Schedule schedule = Schedule.findById(params.scheduleId);
        if (schedule != null){
            DateTime time = params.dateTime();
            if (isWithinDefaultMargins(time, ctx)){
                onHourlyScheduleTime(schedule, params, ctx);
            }
        }
    }



    public void onDelayRoutine(Routine r, LocalDate date, Context ctx) {
        if (isWithinDefaultMargins(r,date,ctx)) {
            setRepeatAlarm(r, AlarmIntentParams.forRoutine(r.getId(), date, true), ctx, getAlarmRepeatFreq(ctx)* 60 * 1000);
            ReminderNotification.cancel(ctx,ReminderNotification.routineNotificationId(r.getId().intValue()));
        }
    }

    public void onDelayHourlySchedule(Schedule s, LocalTime t, LocalDate date, Context ctx) {
        if (isWithinDefaultMargins(date.toDateTime(t), ctx)) {
            setRepeatAlarm(s, AlarmIntentParams.forSchedule(s.getId(), t, date, true), ctx, getAlarmRepeatFreq(ctx)* 60 * 1000);
            ReminderNotification.cancel(ctx,ReminderNotification.scheduleNotificationId(s.getId().intValue()));
        }
    }

    public void onDelayHourlySchedule(Schedule s, LocalTime t, LocalDate date, Context ctx, long delayMinutes) {
        if (isWithinDefaultMargins(date.toDateTime(t), ctx)) {
            setRepeatAlarm(s, AlarmIntentParams.forSchedule(s.getId(), t, date, true), ctx, delayMinutes * 60 * 1000);
            ReminderNotification.cancel(ctx,ReminderNotification.scheduleNotificationId(s.getId().intValue()));
        }
    }

    public void onDelayRoutine(Routine r, LocalDate date, Context ctx, int delayMinutes) {
        if (isWithinDefaultMargins(r, date, ctx)) {
            setRepeatAlarm(r, AlarmIntentParams.forRoutine(r.getId(), date, true),ctx, delayMinutes*60*1000);
            ReminderNotification.cancel(ctx,ReminderNotification.routineNotificationId(r.getId().intValue()));
        }
    }

    public void updateAllAlarms(Context ctx) {
        for (Schedule schedule : Schedule.findAll()) {
            setAlarmsIfNeeded(schedule, LocalDate.now(), ctx);
        }
    }

    //
    // Methods to check if a intake is available according to the user preferences
    //

    public boolean isWithinDefaultMargins(Routine r, LocalDate date, Context cxt) {
        return isWithinDefaultMargins(date.toDateTime(r.time()), cxt);
    }

    public static boolean isWithinDefaultMargins(DateTime t, Context cxt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
        String delayMinutesStr = prefs.getString("alarm_reminder_window", "60");
        long window = Long.parseLong(delayMinutesStr);
        DateTime now = DateTime.now();
        return t.isBefore(now) && t.plusMillis((int) window * 60 * 1000).isAfter(now);
    }

    //
    // Methods to cancel status bar notifications and related alarms
    //

    public void cancelStatusBarNotification(Routine r, LocalDate date, Context ctx) {
        Log.d(TAG, "cancelStatusBarNotification: " + r.getId() + ", " + r.name());
        cancelDelayedAlarm(r, date, ctx);

        ReminderNotification.cancel(ctx,ReminderNotification.routineNotificationId(r.getId().intValue()) );
    }

    public void cancelStatusBarNotification(Schedule r, LocalTime t, LocalDate date, Context ctx) {
        Log.d(TAG, "cancelStatusBarNotification: " + r.getId());
        cancelHourlyDelayedAlarm(r, t, date, ctx);
        ReminderNotification.cancel(ctx, ReminderNotification.scheduleNotificationId(r.getId().intValue()));
    }

    //
    // Methods called when there are changes in database, to update the alarm status
    //

    public void onCreateOrUpdateRoutine(Routine r, Context ctx) {
        Log.d(TAG, "onCreateOrUpdateRoutine: " + r.getId() + ", " + r.name());
        setFirstAlarm(r, LocalDate.now(), ctx);
    }

    public void onCreateOrUpdateSchedule(Schedule s, Context ctx) {
        Log.d(TAG, "onCreateOrUpdateSchedule: " + s.getId() + ", " + s.medicine().name());
        setAlarmsIfNeeded(s, LocalDate.now(), ctx);
    }

    public void onDeleteRoutine(Routine r, Context ctx) {
        Log.d(TAG, "onDeleteRoutine: " + r.getId() + ", " + r.name());
        cancelAlarm(r,LocalDate.now(), ctx);
    }



}
