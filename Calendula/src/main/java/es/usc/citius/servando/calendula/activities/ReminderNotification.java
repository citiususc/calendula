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

package es.usc.citius.servando.calendula.activities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;
import android.text.SpannableStringBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;
import java.util.Random;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.notifications.LockScreenAlarmActivity;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmIntentParams;
import es.usc.citius.servando.calendula.scheduling.NotificationEventReceiver;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

/**
 * Helper class for showing and canceling intake notifications
 * notifications.
 */
public class ReminderNotification {

    private static final String TAG = "ReminderNotification";

    private static final String NOTIFICATION_ROUTINE_TAG = "Reminder";
    private static final String NOTIFICATION_SCHEDULE_TAG = "ScheduleReminder";

    private static Random random = new Random();

    public static int routineNotificationId(int routineId) {
        return ("routine_notification_" + routineId).hashCode();
    }

    public static int scheduleNotificationId(int schedule) {
        return ("schedule_notification_" + schedule).hashCode();
    }

    public static void notify(final Context context, final String title, Routine r, List<ScheduleItem> doses, LocalDate date, Intent intent, boolean lost) {

        if (!PreferenceUtils.getBoolean(PreferenceKeys.SETTINGS_ALARM_NOTIFICATIONS, true)) {
            return;
        }

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBigContentTitle(title);
        styleForRoutine(context, style, r, doses, lost);
        Pair<Intent, Intent> intents = lost ? null : getIntentsForRoutine(context, r, date);

        final Intent confirmAll = new Intent(context, NotificationEventReceiver.class);
        confirmAll.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CONFIRM_ALL_ROUTINE);
        confirmAll.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, r.getId());


        NotificationOptions options = new NotificationOptions();
        options.style = style;
        options.lost = lost;
        options.when = r.getTime().toDateTimeToday().getMillis();
        options.tag = NOTIFICATION_ROUTINE_TAG;
        options.notificationNumber = doses.size();
        options.picture = getLargeIcon(context.getResources(), r.getPatient());
        options.text = r.getName() + " (" + doses.size() + " " + context.getString(R.string.home_menu_medicines).toLowerCase() + ")";

        notify(context, routineNotificationId(r.getId().intValue()), title, intents, confirmAll, intent, options);
        showInsistentScreen(context, intent);
    }

    public static void notify(final Context context, final String title, Schedule schedule, LocalDate date, LocalTime time, Intent intent, boolean lost) {

        if (!PreferenceUtils.getBoolean(PreferenceKeys.SETTINGS_ALARM_NOTIFICATIONS, true)) {
            return;
        }

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBigContentTitle(title);
        styleForSchedule(context, style, schedule, lost);
        Pair<Intent, Intent> intents = lost ? null : getIntentsForSchedule(context, schedule, date, time);

        final Intent confirmAll = new Intent(context, NotificationEventReceiver.class);
        confirmAll.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CONFIRM_ALL_SCHEDULE);
        confirmAll.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
        confirmAll.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, time.toString("kk:mm"));

        NotificationOptions options = new NotificationOptions();
        options.style = style;
        options.lost = lost;
        options.when = time.toDateTimeToday().getMillis();
        options.tag = NOTIFICATION_SCHEDULE_TAG;
        options.notificationNumber = 1;
        options.picture = getLargeIcon(context.getResources(), schedule.patient());
        options.text = schedule.medicine().getName() + " (" + schedule.toReadableString(context) + ")";
        notify(context, scheduleNotificationId(schedule.getId().intValue()), title, intents, confirmAll, intent, options);

        showInsistentScreen(context, intent);
    }

    /**
     * Cancels any notifications of this type previously shown using
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context, int id) {
        final NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_ROUTINE_TAG, id);
            nm.cancel(NOTIFICATION_SCHEDULE_TAG, id);
        } else {
            nm.cancel(id);
            nm.cancel(id);
        }
    }

    private static void showInsistentScreen(Context context, Intent i) {
        boolean insistentNotifications = PreferenceUtils.getBoolean(PreferenceKeys.SETTINGS_ALARM_INSISTENT, false);
        if (insistentNotifications) {
            Intent intent = new Intent(context, LockScreenAlarmActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("target", i);
            context.startActivity(intent);
        }
    }

    private static void notify(final Context context, int id, final String title,
                               Pair<Intent, Intent> actionIntents, Intent confirmIntent, Intent intent,
                               NotificationOptions options) {

        boolean notifications = PreferenceUtils.getBoolean(PreferenceKeys.SETTINGS_ALARM_NOTIFICATIONS, true);


        // if notifications are disabled, exit
        if (!notifications) {
            return;
        }

        final Resources res = context.getResources();
        // prepare notification intents
        PendingIntent defaultIntent = PendingIntent.getActivity(context, random.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent delayIntent = null;
        PendingIntent cancelIntent = null;
        PendingIntent confirmAllIntent = null;

        if (!options.lost) {
            delayIntent = PendingIntent.getActivity(context, random.nextInt(), actionIntents.first, PendingIntent.FLAG_UPDATE_CURRENT);
            cancelIntent = PendingIntent.getBroadcast(context, random.nextInt(), actionIntents.second, PendingIntent.FLAG_UPDATE_CURRENT);
            confirmAllIntent = PendingIntent.getBroadcast(context, random.nextInt(), confirmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        int ic = options.lost ? R.drawable.ic_pill_small_lost : R.drawable.ic_pill_small;
        options.picture = options.picture != null ? options.picture : BitmapFactory.decodeResource(res, ic);
        options.title = title;
        options.ticker = title;
        options.defaultIntent = defaultIntent;
        if (!options.lost) {
            options.cancelIntent = cancelIntent;
            options.delayIntent = delayIntent;
            options.confirmAllIntent = confirmAllIntent;
        }
        options.ringtone = getRingtoneUri();

        Notification n = buildNotification(context, options);
        notify(context, id, n, options.tag);
    }


    private static Notification buildNotification(Context context, NotificationOptions options) {

        Resources res = context.getResources();
        boolean insistentNotifications = PreferenceUtils.getBoolean(PreferenceKeys.SETTINGS_ALARM_INSISTENT, false);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound, and vibration.
                .setDefaults(Notification.DEFAULT_ALL)
                // Set required fields, including the small icon, the notification title, and text.
                .setSmallIcon(options.lost ? R.drawable.ic_pill_small_lost : R.drawable.ic_pill_small)
                .setContentTitle(options.title)
                .setContentText(options.text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(options.picture)
                // Set ticker text (preview) information for this notification.
                .setTicker(options.ticker)
                // Show a number. This is useful when stacking notifications of a single type.
                .setNumber(options.notificationNumber)
                .setWhen(options.when)
                // Set the pending intent to be initiated when the user touches the notification.
                .setContentIntent(options.defaultIntent)
                // Show an expanded list of items on devices running Android 4.1
                // or later.
                .setStyle(options.style)
                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        if (!insistentNotifications) {
            // if insistent is enabled, an activity with vibration will start
            builder.setVibrate(new long[]{1000, 200, 100, 500, 400, 200, 100, 500, 400, 200, 100, 500, 1000}).setSound(options.ringtone);
        }

        if (!options.lost) {
            // add delay button and cancel button
            builder.addAction(R.drawable.ic_history_white_24dp, res.getString(R.string.notification_delay), options.delayIntent)
                    .addAction(R.drawable.ic_alarm_off_white_24dp, res.getString(R.string.notification_cancel_now), options.cancelIntent)
                    .addAction(R.drawable.ic_done_white_36dp, res.getString(R.string.take), options.confirmAllIntent);

            builder.extend((new NotificationCompat.WearableExtender()
                    .addAction(new NotificationCompat.Action.Builder(
                            R.drawable.ic_done_white_36dp,
                            res.getString(R.string.done),
                            options.confirmAllIntent).build())
                    .addAction(new NotificationCompat.Action.Builder(
                            R.drawable.ic_history_white_24dp,
                            res.getString(R.string.notification_delay),
                            options.delayIntent).build())
                    .addAction(new NotificationCompat.Action.Builder(
                            R.drawable.ic_alarm_off_white_24dp,
                            res.getString(R.string.notification_cancel_now),
                            options.cancelIntent).build()
                    )));
        }

        Notification n = builder.build();
        n.defaults = 0;
        n.ledARGB = 0x00ffa500;
        n.ledOnMS = 1000;
        n.ledOffMS = 2000;
        return n;
    }

    private static void styleForRoutine(Context ctx, NotificationCompat.InboxStyle style, Routine r, List<ScheduleItem> doses, boolean lost) {


        for (ScheduleItem scheduleItem : doses) {
            //TODO: Use DecimalFormat
            // DecimalFormat timeFormatter = new DecimalFormat("#");
            //String dfDose = timeFormatter.format(scheduleItem.dose());

            final SpannableStringBuilder SpItem = new SpannableStringBuilder();
            final Medicine med = scheduleItem.getSchedule().medicine();
            SpItem.append(med.getName());
            SpItem.append(":  " + scheduleItem.getDose() + " " + med.getPresentation().units(ctx.getResources(), scheduleItem.getDose()));
            style.addLine(SpItem);
        }
        String delayMinutesStr = PreferenceUtils.getString(PreferenceKeys.SETTINGS_ALARM_REPEAT_FREQUENCY, "15");
        int delayMinutes = (int) Long.parseLong(delayMinutesStr);

        if (delayMinutes > 0 && !lost) {
            String repeatTime = DateTime.now().plusMinutes(delayMinutes).toString("HH:mm");
            style.setSummaryText(ctx.getResources().getString(R.string.notification_repeat_message, repeatTime));
        } else {
            style.setSummaryText(doses.size() + " " + ctx.getString(R.string.medicine) + (doses.size() > 1 ? "s, " : ", ") + r.getName());
        }

    }

    private static void styleForSchedule(Context context, NotificationCompat.InboxStyle style, Schedule schedule, boolean lost) {

        final Medicine med = schedule.medicine();
        final SpannableStringBuilder SpItem = new SpannableStringBuilder();
        SpItem.append(med.getName());
        SpItem.append("   " + schedule.dose() + " " + med.getPresentation().units(context.getResources(), schedule.dose()));
        style.addLine(SpItem);

        String delayMinutesStr = PreferenceUtils.getString(PreferenceKeys.SETTINGS_ALARM_REPEAT_FREQUENCY, "15");
        int delayMinutes = (int) Long.parseLong(delayMinutesStr);

        if (delayMinutes > 0 && !lost) {
            String repeatTime = DateTime.now().plusMinutes(delayMinutes).toString("kk:mm");
            style.setSummaryText(context.getString(R.string.notification_repeat_message, repeatTime));
        } else {
            style.setSummaryText(med.getName() + "(" + context.getString(R.string.every) + " " + schedule.rule()
                    .getInterval() + " " + context.getString(R.string.hours) + ")");
        }
    }

    private static Pair<Intent, Intent> getIntentsForRoutine(Context context, Routine r, LocalDate date) {

        // delay intent sent on click delay button
        final Intent delay = new Intent(context, ConfirmActivity.class);
        delay.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, "delay");
        delay.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, r.getId());
        delay.putExtra("date", date.toString(AlarmIntentParams.DATE_FORMAT));

        // delay intent sent on click delay button
        final Intent cancel = new Intent(context, NotificationEventReceiver.class);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CANCEL_ROUTINE);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, r.getId());
        cancel.putExtra("date", date.toString(AlarmIntentParams.DATE_FORMAT));

        return new Pair<>(delay, cancel);
    }

    private static Pair<Intent, Intent> getIntentsForSchedule(Context context, Schedule schedule, LocalDate date, LocalTime time) {

        final Intent delay = new Intent(context, ConfirmActivity.class);
        delay.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, "delay");
        delay.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
        delay.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, date.toString("kk:mm"));
        delay.putExtra("date", date.toString(AlarmIntentParams.DATE_FORMAT));

        final Intent cancel = new Intent(context, NotificationEventReceiver.class);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CANCEL_HOURLY_SCHEDULE);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, time.toString("kk:mm"));
        cancel.putExtra("date", date.toString(AlarmIntentParams.DATE_FORMAT));

        return new Pair<>(delay, cancel);
    }

    private static Uri getRingtoneUri() {
        return Settings.System.DEFAULT_NOTIFICATION_URI;
    }

    private static Bitmap getLargeIcon(Resources r, Patient p) {
        return BitmapFactory.decodeResource(r, AvatarMgr.res(p.getAvatar()));
    }


    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, int id, final Notification notification, String tag) {
        //int id = Math.abs(tag.hashCode());
        final NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(tag, id, notification);
        } else {
            nm.notify(id, notification);
        }
    }

    private static class NotificationOptions {
        int notificationNumber;
        boolean lost = false;
        String title;
        String text;
        String ticker;
        String tag;
        Bitmap picture;
        PendingIntent defaultIntent;
        PendingIntent cancelIntent;
        PendingIntent delayIntent;
        PendingIntent confirmAllIntent;
        NotificationCompat.InboxStyle style;
        Uri ringtone;
        long when;

    }

}