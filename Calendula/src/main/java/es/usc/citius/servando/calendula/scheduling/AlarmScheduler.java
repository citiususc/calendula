/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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
import android.os.Build;
import android.os.Bundle;

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
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

/**
 *
 */
public class AlarmScheduler {

    public static final String EXTRA_PARAMS = "alarm_params";
    private static final String TAG = "AlarmScheduler";
    private static final AlarmScheduler instance = new AlarmScheduler();

    private AlarmScheduler() {

    }

    // static method to get the AlarmScheduler instance
    public static AlarmScheduler instance() {
        return instance;
    }

    public static boolean isWithinDefaultMargins(DateTime t) {
        String delayMinutesStr = PreferenceUtils.getString(PreferenceKeys.SETTINGS_ALARM_REMINDER_WINDOW, "60");
        long window = Long.parseLong(delayMinutesStr);
        DateTime now = DateTime.now();
        return t.isBefore(now) && t.plusMillis((int) window * 60 * 1000).isAfter(now);
    }

    /*
     * Whether an alarm for a specific time can be scheduled or not based on
     * the alarm time and the alarm reminder window defined by the user. Alarm time plus
     * alarm window must be in the future to allow alarm scheduling
     */
    public static boolean canBeScheduled(DateTime t) {
        String delayMinutesStr = PreferenceUtils.getString(PreferenceKeys.SETTINGS_ALARM_REMINDER_WINDOW, "60");
        int window = (int) Long.parseLong(delayMinutesStr);
        return t.plusMinutes(window).isAfterNow();
    }

    public static void setAlarmParams(Intent intent, AlarmIntentParams parcelable) {
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_PARAMS, parcelable);
        intent.putExtra(EXTRA_PARAMS, b);
        intent.putExtra(CalendulaApp.INTENT_EXTRA_DATE, parcelable.date);
    }

    public static AlarmIntentParams getAlarmParams(Intent intent) {
        // try to get the bundle
        Bundle bundleExtra = intent.getBundleExtra(EXTRA_PARAMS);
        if (bundleExtra != null) {
            return bundleExtra.getParcelable(EXTRA_PARAMS);
        } else {
            // try to get the parcelable from the intent
            return intent.getParcelableExtra(EXTRA_PARAMS);
        }
    }

    private static PendingIntent pendingIntent(Context ctx, Routine routine, LocalDate date, boolean delayed, int actionType) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        AlarmIntentParams params = AlarmIntentParams.forRoutine(routine.getId(), date, delayed, actionType);
        setAlarmParams(intent, params);
        return PendingIntent.getBroadcast(ctx, params.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static PendingIntent pendingIntent(Context ctx, Schedule schedule, LocalTime time, LocalDate date, boolean delayed, int actionType) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        AlarmIntentParams params = AlarmIntentParams.forSchedule(schedule.getId(), time, date, delayed, actionType);
        setAlarmParams(intent, params);
        return PendingIntent.getBroadcast(ctx, params.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void onAlarmReceived(AlarmIntentParams params, Context ctx) {

        Routine routine = Routine.findById(params.routineId);
        if (routine != null) {
            LogUtil.d(TAG, "onAlarmReceived: " + routine.getId() + ", " + routine.getName());
            if (params.actionType == AlarmIntentParams.USER || isWithinDefaultMargins(routine, params.date())) {
                LogUtil.d(TAG, "Routine alarm received, is user action: " + (params.actionType == AlarmIntentParams.USER));
                onRoutineTime(routine, params, ctx);
            } else {
                LogUtil.d(TAG, "Routine lost");
                onRoutineLost(routine, params, ctx);
            }
        }
    }

    public void onHourlyAlarmReceived(AlarmIntentParams params, Context ctx) {
        Schedule schedule = Schedule.findById(params.scheduleId);
        if (schedule != null) {
            DateTime time = params.dateTime();
            if (params.actionType == AlarmIntentParams.USER || isWithinDefaultMargins(time)) {

                LogUtil.d(TAG, "Hourly alarm received, is user action: " + (params.actionType == AlarmIntentParams.USER));

                onHourlyScheduleTime(schedule, params, ctx);
            } else {
                LogUtil.d(TAG, "Schedule lest");
                onHourlyScheduleLost(schedule, params, ctx);
            }
        }
    }

    public void onDelayRoutine(Routine r, LocalDate date, Context ctx) {
        if (isWithinDefaultMargins(r, date)) {
            setRepeatAlarm(r, AlarmIntentParams.forRoutine(r.getId(), date, true), ctx, getAlarmRepeatFreq() * 60 * 1000);
        }
    }

    public void onDelayHourlySchedule(Schedule s, LocalTime t, LocalDate date, Context ctx) {
        if (isWithinDefaultMargins(date.toDateTime(t))) {
            setRepeatAlarm(s, AlarmIntentParams.forSchedule(s.getId(), t, date, true), ctx, getAlarmRepeatFreq() * 60 * 1000);
        }
    }

    public void onUserDelayHourlySchedule(Schedule s, LocalTime t, LocalDate date, Context ctx, long delayMinutes) {
        cancelHourlyDelayedAlarm(s, t, date, ctx, AlarmIntentParams.AUTO);
        setRepeatAlarm(s, AlarmIntentParams.forSchedule(s.getId(), t, date, true, AlarmIntentParams.USER), ctx, delayMinutes * 60 * 1000);
    }

    public void onUserDelayRoutine(Routine r, LocalDate date, Context ctx, int delayMinutes) {
        cancelDelayedAlarm(r, date, ctx, AlarmIntentParams.AUTO);
        setRepeatAlarm(r, AlarmIntentParams.forRoutine(r.getId(), date, true, AlarmIntentParams.USER), ctx, delayMinutes * 60 * 1000);
    }

    public void updateAllAlarms(Context ctx) {
        for (Schedule schedule : Schedule.findAll()) {
            setAlarmsIfNeeded(schedule, LocalDate.now(), ctx);
        }
    }

    public boolean isWithinDefaultMargins(Routine r, LocalDate date) {
        return isWithinDefaultMargins(date.toDateTime(r.getTime()));
    }

    public void onIntakeCancelled(Routine r, LocalDate date, Context ctx) {
        // set time taken
        cancelIntake(r, date);
        // cancel alarms
        onIntakeCompleted(r, date, ctx);
    }

    public void onIntakeCancelled(Schedule s, LocalTime t, LocalDate date, Context ctx) {
        // set time taken
        cancelIntake(s, t, date);
        // cancell all alarms
        onIntakeCompleted(s, t, date, ctx);
    }

    public void onIntakeConfirmAll(Routine r, LocalDate date, Context ctx) {
        // set time taken
        for (ScheduleItem scheduleItem : r.getScheduleItems()) {
            DailyScheduleItem ds = DB.dailyScheduleItems().findByScheduleItemAndDate(scheduleItem, date);
            if (ds != null) {
                LogUtil.d(TAG, "Confirming schedule item");
                ds.setTimeTaken(LocalTime.now());
                ds.setTakenToday(true);
                DB.dailyScheduleItems().saveAndUpdateStock(ds, true);
            }
        }
        // cancel alarms
        onIntakeCompleted(r, date, ctx);
    }

    public void onIntakeConfirmAll(Schedule s, LocalTime t, LocalDate date, Context ctx) {
        // set time taken
        DailyScheduleItem ds = DB.dailyScheduleItems().findBy(s, date, t);
        if (ds != null) {
            LogUtil.d(TAG, "Confirming schedule item");
            ds.setTakenToday(true);
            ds.setTimeTaken(LocalTime.now());
            DB.dailyScheduleItems().saveAndUpdateStock(ds, true);
        }
        // cancell all alarms
        onIntakeCompleted(s, t, date, ctx);
    }

    public void onIntakeCompleted(Routine r, LocalDate date, Context ctx) {
        // cancel notification
        ReminderNotification.cancel(ctx, ReminderNotification.routineNotificationId(r.getId().intValue()));
        // cancel all delay alarms
        cancelDelayedAlarm(r, date, ctx, AlarmIntentParams.USER);
        cancelDelayedAlarm(r, date, ctx, AlarmIntentParams.AUTO);
        // update the relevant screens
        CalendulaApp.eventBus().post(new PersistenceEvents.IntakeConfirmedEvent(r.getId() + date.hashCode(), true));
    }

    public void onIntakeCompleted(Schedule s, LocalTime t, LocalDate date, Context ctx) {
        // cancel notification
        ReminderNotification.cancel(ctx, ReminderNotification.scheduleNotificationId(s.getId().intValue()));
        // cancel all delay alarms
        cancelHourlyDelayedAlarm(s, t, date, ctx, AlarmIntentParams.USER);
        cancelHourlyDelayedAlarm(s, t, date, ctx, AlarmIntentParams.AUTO);
        // update the relevant screens
        CalendulaApp.eventBus().post(new PersistenceEvents.IntakeConfirmedEvent(s.getId() + date.hashCode(), true));
    }

    public void onCreateOrUpdateRoutine(Routine r, Context ctx) {
        LogUtil.d(TAG, "onCreateOrUpdateRoutine: " + r.getId() + ", " + r.getName());
        setFirstAlarm(r, LocalDate.now(), ctx);
    }

    public void onCreateOrUpdateSchedule(Schedule s, Context ctx) {
        LogUtil.d(TAG, "onCreateOrUpdateSchedule: " + s.getId() + ", " + s.medicine().getName());
        setAlarmsIfNeeded(s, LocalDate.now(), ctx);
    }

    public void onDeleteRoutine(Routine r, Context ctx) {
        LogUtil.d(TAG, "onDeleteRoutine: " + r.getId() + ", " + r.getName());
        cancelAlarm(r, LocalDate.now(), ctx);
    }

    private Long getAlarmRepeatFreq() {
        String delayMinutesStr = PreferenceUtils.getString(PreferenceKeys.SETTINGS_ALARM_REPEAT_FREQUENCY, "15");
        ;
        return Long.parseLong(delayMinutesStr);
    }

    /**
     * Set an alarm for a routine
     */
    private void setFirstAlarm(Routine routine, LocalDate date, Context ctx) {
        long timestamp = date.toDateTime(routine.getTime()).getMillis();
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, date, false, AlarmIntentParams.AUTO);
        setExactAlarm(ctx, timestamp, routinePendingIntent);
    }

    /**
     * Set an alarm for a repeating schedule item
     */
    private void setFirstAlarm(Schedule schedule, LocalTime time, LocalDate date, Context ctx) {
        DateTime dateTime = date.toDateTime(time);
        PendingIntent routinePendingIntent = pendingIntent(ctx, schedule, time, date, false, AlarmIntentParams.AUTO);
        setExactAlarm(ctx, dateTime.getMillis(), routinePendingIntent);
    }

    private void setRepeatAlarm(Routine routine, AlarmIntentParams firstParams, Context ctx, long delayMillis) {
        PendingIntent routinePendingIntent = pendingIntent(ctx, routine, firstParams.date(), true, firstParams.actionType);
        setExactAlarm(ctx, DateTime.now().getMillis() + delayMillis, routinePendingIntent);
    }

    private void setRepeatAlarm(Schedule schedule, AlarmIntentParams firstParams, Context ctx, long delayMillis) {
        PendingIntent schedulePendingIntent = pendingIntent(ctx, schedule, firstParams.scheduleTime(), firstParams.date(), true, firstParams.actionType);
        setExactAlarm(ctx, DateTime.now().getMillis() + delayMillis, schedulePendingIntent);
    }

    private void setExactAlarm(Context ctx, long millis, PendingIntent pendingIntent) {
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

    private void cancelAlarm(Routine routine, LocalDate date, Context ctx) {
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

        DailyScheduleItem ds = DB.dailyScheduleItems().findBy(s, date, t);
        if (ds != null) {
            // get hourly delay pending intent
            PendingIntent pendingIntent = pendingIntent(ctx, s, t, date, true, actionType);
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
                if (scheduleItem.getRoutine() != null && canBeScheduled(scheduleItem.getRoutine().getTime().toDateTimeToday())) {
                    setFirstAlarm(scheduleItem.getRoutine(), date, ctx);
                }
            }
        } else {
            List<DateTime> times = schedule.hourlyItemsAt(date.toDateTimeAtStartOfDay());
            for (DateTime time : times) {
                if (canBeScheduled(time)) {
                    setFirstAlarm(schedule, time.toLocalTime(), date, ctx);
                }
            }
        }
    }

    private void onRoutineTime(Routine routine, AlarmIntentParams firstParams, Context ctx) {

        List<ScheduleItem> doses = new ArrayList<>();
        List<ScheduleItem> rItems = routine.getScheduleItems();
        boolean notify = false;
        // check if all items have timeTaken (cancelled notifications)
        for (ScheduleItem scheduleItem : rItems) {
            LogUtil.d(TAG, "Routine schedule items: " + rItems.size());
            DailyScheduleItem ds = DB.dailyScheduleItems().findByScheduleItemAndDate(scheduleItem, firstParams.date());
            if (ds != null) {
                LogUtil.d(TAG, "DailySchedule Item: " + ds.toString());
                doses.add(scheduleItem);
                if (ds.getTimeTaken() == null) {
                    LogUtil.d(TAG, ds.getScheduleItem().getSchedule().medicine().getName() + " not checked or cancelled. Notify!");
                    notify = true;
                }
            }
        }

        if (notify) {
            final Intent intent = new Intent(ctx, ConfirmActivity.class);
            intent.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, routine.getId());
            intent.putExtra(CalendulaApp.INTENT_EXTRA_DATE, firstParams.date);
            ReminderNotification.notify(ctx, ctx.getResources().getString(R.string.meds_time), routine, doses, firstParams.date(), intent, false);
            LogUtil.d(TAG, "Show notification");
            boolean repeatAlarms = PreferenceUtils.getBoolean(PreferenceKeys.SETTINGS_ALARM_REPEAT_ENABLED, false);
            if (repeatAlarms) {
                firstParams.actionType = AlarmIntentParams.AUTO;
                setRepeatAlarm(routine, firstParams, ctx, getAlarmRepeatFreq() * 60 * 1000);
            }
        }
    }

    //
    // Methods called when there are changes in database, to update the alarm status
    //

    private void onHourlyScheduleTime(Schedule schedule, AlarmIntentParams firstParams, Context ctx) {

        boolean notify = false;
        // check if this item has timeTaken (cancelled notifications)

        DailyScheduleItem ds = DB.dailyScheduleItems().findBy(schedule, firstParams.date(), firstParams.scheduleTime());

        if (ds != null && ds.getTimeTaken() == null) {
            notify = true;
        }

        if (notify) {
            final Intent intent = new Intent(ctx, ConfirmActivity.class);
            intent.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
            intent.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, firstParams.scheduleTime);
            intent.putExtra(CalendulaApp.INTENT_EXTRA_DATE, firstParams.date);

            String title = ctx.getResources().getString(R.string.meds_time);
            ReminderNotification.notify(ctx, title, schedule, firstParams.date(), firstParams.scheduleTime(), intent, false);
            // Handle delay if needed
            boolean repeatAlarms = PreferenceUtils.getBoolean(PreferenceKeys.SETTINGS_ALARM_REPEAT_ENABLED, false);
            if (repeatAlarms) {
                firstParams.actionType = AlarmIntentParams.AUTO;
                setRepeatAlarm(schedule, firstParams, ctx, getAlarmRepeatFreq() * 60 * 1000);
            }
        }
    }

    private void onRoutineLost(Routine routine, AlarmIntentParams params, Context ctx) {
        // get the schedule items for the current routine, excluding already taken
        List<ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine, params.date());
        // cancel intake
        cancelIntake(routine, params.date());
        // cancel alarms
        onIntakeCompleted(routine, params.date(), ctx);

        // show routine lost notification
        final Intent intent = new Intent(ctx, ConfirmActivity.class);
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, routine.getId());
        intent.putExtra(CalendulaApp.INTENT_EXTRA_DATE, params.date);
        String title = ctx.getResources().getString(R.string.meds_time_lost);
        ReminderNotification.notify(ctx, title, routine, doses, params.date(), intent, true);
    }

    private void onHourlyScheduleLost(Schedule schedule, AlarmIntentParams params, Context ctx) {
        // cancel intake (set time taken)
        cancelIntake(schedule, params.scheduleTime(), params.date());
        // cancel alarms
        onIntakeCompleted(schedule, params.scheduleTime(), params.date(), ctx);

        // show schedule lost notification
        final Intent intent = new Intent(ctx, ConfirmActivity.class);
        intent.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
        intent.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, params.scheduleTime);
        intent.putExtra(CalendulaApp.INTENT_EXTRA_DATE, params.date);
        String title = ctx.getResources().getString(R.string.meds_time_lost);
        ReminderNotification.notify(ctx, title, schedule, params.date(), params.scheduleTime(), intent, true);
    }

    private void cancelIntake(Routine r, LocalDate date) {
        for (ScheduleItem scheduleItem : r.getScheduleItems()) {
            DailyScheduleItem ds = DB.dailyScheduleItems().findByScheduleItemAndDate(scheduleItem, date);
            if (ds.getTimeTaken() == null) {
                LogUtil.d(TAG, "Cancelling schedule item");
                ds.setTimeTaken(LocalTime.now());
                ds.save();
            }
        }
    }

    private void cancelIntake(Schedule s, LocalTime t, LocalDate date) {
        DailyScheduleItem ds = DB.dailyScheduleItems().findBy(s, date, t);
        if (ds != null && ds.getTimeTaken() == null) {
            LogUtil.d(TAG, "Cancelling schedule item");
            ds.setTimeTaken(LocalTime.now());
            ds.save();
        }
    }


}
