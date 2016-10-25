/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
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

    private static PendingIntent pendingIntent(Context ctx, Routine routine, LocalDate date, boolean delayed, int actionType){
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        AlarmIntentParams params = AlarmIntentParams.forRoutine(routine.getId(), date, delayed, actionType);
        setAlarmParams(intent, params);
        return PendingIntent.getBroadcast(ctx, params.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static PendingIntent pendingIntent(Context ctx, Schedule schedule, LocalTime time, LocalDate date, boolean delayed, int actionType){
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        AlarmIntentParams params = AlarmIntentParams.forSchedule(schedule.getId(), time, date, delayed,actionType);
        setAlarmParams(intent, params);
        return PendingIntent.getBroadcast(ctx, params.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private Long getAlarmRepeatFreq(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");
        return Long.parseLong(delayMinutesStr);
    }

    /**
     * Set an alarm for a routine
     */
    private void setFirstAlarm(Routine routine, LocalDate date, Context ctx) {
        long timestamp = date.toDateTime(routine.time()).getMillis();
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, date, false, AlarmIntentParams.AUTO);
        setExactAlarm(ctx,timestamp,routinePendingIntent);
    }

    /**
     * Set an alarm for a repeating schedule item
     */
    private void setFirstAlarm(Schedule schedule, LocalTime time, LocalDate date, Context ctx) {
        DateTime dateTime = date.toDateTime(time);
        PendingIntent routinePendingIntent = pendingIntent(ctx, schedule, time, date, false,AlarmIntentParams.AUTO);
        setExactAlarm(ctx,dateTime.getMillis(),routinePendingIntent);
    }

    private void setRepeatAlarm(Routine routine, AlarmIntentParams firstParams, Context ctx, long delayMillis) {
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, firstParams.date(), true, firstParams.actionType);
        setExactAlarm(ctx,DateTime.now().getMillis() + delayMillis,routinePendingIntent);
    }

    private void setRepeatAlarm(Schedule schedule, AlarmIntentParams firstParams, Context ctx, long delayMillis) {
        PendingIntent schedulePendingIntent = pendingIntent(ctx, schedule, firstParams.scheduleTime(), firstParams.date(), true, firstParams.actionType);
        setExactAlarm(ctx,DateTime.now().getMillis() + delayMillis,schedulePendingIntent);
    }

    private void setExactAlarm(Context ctx, long millis, PendingIntent pendingIntent){
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
            }
        }
    }

    private void cancelAlarm(Routine routine, LocalDate date, Context ctx)
    {
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, date, false, AlarmIntentParams.AUTO);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(routinePendingIntent);
        }
    }

    private void cancelDelayedAlarm(Routine routine, LocalDate date, Context ctx, int actionType) {
        // cancel alarm
        // get delay routine pending intent
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, date, true, actionType);
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(routinePendingIntent);
        }
    }

    private void cancelHourlyDelayedAlarm(Schedule s, LocalTime t, LocalDate date, Context ctx, int actionType) {

        DailyScheduleItem ds = DB.dailyScheduleItems().findBy(s,date,t);
        if(ds!=null) {
            // get hourly delay pending intent
            PendingIntent pendingIntent = pendingIntent(ctx, s, t, date, true,actionType);
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
                if (scheduleItem.routine() != null && canBeScheduled(scheduleItem.routine().time().toDateTimeToday(),ctx)){
                    setFirstAlarm(scheduleItem.routine(), date, ctx);
                }
            }
        } else {
            List<DateTime> times = schedule.hourlyItemsAt(date.toDateTimeAtStartOfDay());
            for (DateTime time : times) {
                if(canBeScheduled(time,ctx)) {
                    setFirstAlarm(schedule, time.toLocalTime(), date, ctx);
                }
            }
        }
    }

    private void onRoutineTime(Routine routine, AlarmIntentParams firstParams, Context ctx) {

        List<ScheduleItem> doses = new ArrayList<>();
        List<ScheduleItem> rItems = routine.scheduleItems();
        boolean notify = false;
        // check if all items have timeTaken (cancelled notifications)
        for (ScheduleItem scheduleItem : rItems) {
            Log.d(TAG, "Routine schedule items: " + rItems.size());
            DailyScheduleItem ds = DB.dailyScheduleItems().findByScheduleItemAndDate(scheduleItem, firstParams.date());
            if (ds != null) {
                Log.d(TAG, "DailySchedule Item: " + ds.toString());
                doses.add(scheduleItem);
                if (ds.timeTaken() == null) {
                    Log.d(TAG, ds.scheduleItem().schedule().medicine().name() + " not checked or cancelled. Notify!");
                    notify = true;
                }
            }
        }

        if (notify) {
            final Intent intent = new Intent(ctx, ConfirmActivity.class);
            intent.putExtra("routine_id", routine.getId());
            intent.putExtra("date", firstParams.date);
            intent.putExtra("actionType", firstParams.actionType);
            ReminderNotification.notify(ctx, ctx.getResources().getString(R.string.meds_time), routine, doses, intent, false);
            Log.d(TAG, "Show notification");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            boolean repeatAlarms = prefs.getBoolean("alarm_repeat_enabled", false);
            if (repeatAlarms) {
                firstParams.actionType = AlarmIntentParams.AUTO;
                setRepeatAlarm(routine, firstParams, ctx, getAlarmRepeatFreq(ctx)*60*1000);
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
            intent.putExtra("actionType", firstParams.actionType);

            String title = ctx.getResources().getString(R.string.meds_time);
            ReminderNotification.notify(ctx, title, schedule, firstParams.scheduleTime(), intent, false);
            // Handle delay if needed
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            boolean repeatAlarms = prefs.getBoolean("alarm_repeat_enabled", false);
            if (repeatAlarms)
            {
                firstParams.actionType = AlarmIntentParams.AUTO;
                setRepeatAlarm(schedule, firstParams, ctx, getAlarmRepeatFreq(ctx) * 60 * 1000);
            }
        }
    }

    private void onRoutineLost(Routine routine, AlarmIntentParams params, Context ctx) {
        // get the schedule items for the current routine, excluding already taken
        List<ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine, params.date());
        // cancel notification
        ReminderNotification.cancel(ctx,ReminderNotification.routineNotificationId(routine.getId().intValue()));
        // cancel intake
        cancelIntake(routine, params.date(), ctx);
        // cancel alarms
        onIntakeCompleted(routine,params.date(),ctx);

        // show routine lost notification
        final Intent intent = new Intent(ctx, ConfirmActivity.class);
        intent.putExtra("routine_id", routine.getId());
        intent.putExtra("date", params.date);
        String title = ctx.getResources().getString(R.string.meds_time_lost);
        ReminderNotification.notify(ctx, title, routine, doses, intent, true);
    }

    private void onHourlyScheduleLost(Schedule schedule, AlarmIntentParams params, Context ctx) {
        // cancel notification
        ReminderNotification.cancel(ctx,ReminderNotification.scheduleNotificationId(schedule.getId().intValue()));
        // cancel intake (set time taken)
        cancelIntake(schedule,  params.scheduleTime(), params.date(), ctx);
        // cancel alarms
        onIntakeCompleted(schedule, params.scheduleTime(), params.date(), ctx);

        // show schedule lost notification
        final Intent intent = new Intent(ctx, ConfirmActivity.class);
        intent.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
        intent.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, params.scheduleTime);
        intent.putExtra("date", params.date);
        String title = ctx.getResources().getString(R.string.meds_time_lost);
        ReminderNotification.notify(ctx, title, schedule, params.scheduleTime(), intent, true);
    }

    public void onAlarmReceived(AlarmIntentParams params, Context ctx) {

        Routine routine = Routine.findById(params.routineId);
        if (routine != null) {
            Log.d(TAG, "onAlarmReceived: " + routine.getId() + ", " + routine.name());
            if (params.actionType == AlarmIntentParams.USER || isWithinDefaultMargins(routine, params.date(), ctx)) {
                Log.d(TAG, "Routine alarm received, is user action: " + (params.actionType == AlarmIntentParams.USER));
                onRoutineTime(routine, params, ctx);
            } else {
                Log.d(TAG, "Routine lost");
                onRoutineLost(routine,params,ctx);
            }
        }
    }

    public void onHourlyAlarmReceived(AlarmIntentParams params, Context ctx) {
        Schedule schedule = Schedule.findById(params.scheduleId);
        if (schedule != null){
            DateTime time = params.dateTime();
            if (params.actionType == AlarmIntentParams.USER || isWithinDefaultMargins(time, ctx)){

                Log.d(TAG, "Hourly alarm received, is user action: " + (params.actionType == AlarmIntentParams.USER));

                onHourlyScheduleTime(schedule, params, ctx);
            } else {
                Log.d(TAG, "Schedule lest");
                onHourlyScheduleLost(schedule, params, ctx);
            }
        }
    }



    public void onDelayRoutine(Routine r, LocalDate date, Context ctx) {
        if (isWithinDefaultMargins(r,date,ctx)) {
            setRepeatAlarm(r, AlarmIntentParams.forRoutine(r.getId(), date, true), ctx, getAlarmRepeatFreq(ctx)* 60 * 1000);
        }
    }

    public void onDelayHourlySchedule(Schedule s, LocalTime t, LocalDate date, Context ctx) {
        if (isWithinDefaultMargins(date.toDateTime(t), ctx)) {
            setRepeatAlarm(s, AlarmIntentParams.forSchedule(s.getId(), t, date, true), ctx, getAlarmRepeatFreq(ctx)* 60 * 1000);
        }
    }

    public void onUserDelayHourlySchedule(Schedule s, LocalTime t, LocalDate date, Context ctx, long delayMinutes) {
        cancelHourlyDelayedAlarm(s, t, date, ctx, AlarmIntentParams.AUTO);
        setRepeatAlarm(s, AlarmIntentParams.forSchedule(s.getId(), t, date, true, AlarmIntentParams.USER), ctx, delayMinutes * 60 * 1000);
    }

    public void onUserDelayRoutine(Routine r, LocalDate date, Context ctx, int delayMinutes) {
        cancelDelayedAlarm(r, date, ctx, AlarmIntentParams.AUTO);
        setRepeatAlarm(r, AlarmIntentParams.forRoutine(r.getId(), date, true, AlarmIntentParams.USER),ctx, delayMinutes*60*1000);
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

    /*
     * Whether an alarm for a specific time can be scheduled or not based on
     * the alarm time and the alarm reminder window defined by the user. Alarm time plus
     * alarm window must be in the future to allow alarm scheduling
     */
    public static boolean canBeScheduled(DateTime t, Context cxt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
        String delayMinutesStr = prefs.getString("alarm_reminder_window", "60");
        int window = (int)Long.parseLong(delayMinutesStr);
        return t.plusMinutes(window).isAfterNow();
    }

    public void onIntakeCancelled(Routine r, LocalDate date, Context ctx) {
        // cancel notification
        ReminderNotification.cancel(ctx,ReminderNotification.routineNotificationId(r.getId().intValue()));
        // set time taken
        cancelIntake(r,date,ctx);
        // cancel alarms
        onIntakeCompleted(r,date,ctx);
    }

    public void onIntakeCancelled(Schedule s, LocalTime t, LocalDate date, Context ctx) {
        // cancel notification
        ReminderNotification.cancel(ctx,ReminderNotification.routineNotificationId(s.getId().intValue()));
        // set time taken
        cancelIntake(s,t,date,ctx);
        // cancell all alarms
        onIntakeCompleted(s,t,date,ctx);
    }

    public void onIntakeCompleted(Routine r, LocalDate date, Context ctx){
        // cancel all delay alarms
        cancelDelayedAlarm(r, date, ctx, AlarmIntentParams.USER);
        cancelDelayedAlarm(r, date, ctx, AlarmIntentParams.AUTO);
    }

    public void onIntakeCompleted(Schedule r, LocalTime t, LocalDate date, Context ctx){
        // cancel all delay alarms
        cancelHourlyDelayedAlarm(r, t, date, ctx, AlarmIntentParams.USER);
        cancelHourlyDelayedAlarm(r, t, date, ctx, AlarmIntentParams.AUTO);
    }

    private void cancelIntake(Routine r, LocalDate date, Context ctx) {
        for (ScheduleItem scheduleItem : r.scheduleItems()) {
            DailyScheduleItem ds = DB.dailyScheduleItems().findByScheduleItemAndDate(scheduleItem,date);
            if(ds.timeTaken() == null) {
                Log.d(TAG, "Cancelling schedule item");
                ds.setTimeTaken(LocalTime.now());
                ds.save();
            }
        }
    }

    private void cancelIntake(Schedule s, LocalTime t, LocalDate date, Context ctx) {
        DailyScheduleItem ds = DB.dailyScheduleItems().findBy(s,date,t);
        if(ds!=null && ds.timeTaken()==null) {
            Log.d(TAG, "Cancelling schedule item");
            ds.setTimeTaken(LocalTime.now());
            ds.save();
        }
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


    public static void setAlarmParams(Intent intent, AlarmIntentParams parcelable){
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_PARAMS,parcelable);
        intent.putExtra(EXTRA_PARAMS,b);
        intent.putExtra("date",parcelable.date);
    }

    public static AlarmIntentParams getAlarmParams(Intent intent){
        // try to get the bundle
        Bundle bundleExtra = intent.getBundleExtra(EXTRA_PARAMS);
        if(bundleExtra!=null){
            return bundleExtra.getParcelable(EXTRA_PARAMS);
        }else{
            // try to get the parcelable from the intent
            return intent.getParcelableExtra(EXTRA_PARAMS);
        }
    }






}
