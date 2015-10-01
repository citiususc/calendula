package es.usc.citius.servando.calendula.activities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.NotificationEventReceiver;

/**
 * Helper class for showing and canceling message
 * notifications.
 * <p/>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class ReminderNotification {

    public static final String TAG = ReminderNotification.class.getName();
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Reminder";
    private static final String NOTIFICATION_SCHEDULE_TAG = "ScheduleReminder";

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p/>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p/>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of message notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(Context)
     */
    public static void notify(final Context context, final String exampleString, Routine r,
        List<ScheduleItem> doses, Intent intent)
    {

        // if notifications are disabled, exit
        boolean notifications = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("alarm_notifications", true);
        if (!notifications)
        {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");
        boolean inistentNotifications = prefs.getBoolean("alarm_insistent", false);
        long delayMinutes = Long.parseLong(delayMinutesStr);

        //        if(delayMinutes < 0){
        //            delayMinutes = 15;
        //        }

        final Resources res = context.getResources();

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_pill_48dp);
        //final Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_pill_48dp);
        final String title =
            res.getString(R.string.message_notification_title_template, exampleString);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        // DecimalFormat df = new DecimalFormat("#"); //TODO: Use DecimalFormat
        for (ScheduleItem scheduleItem : doses)
        {

            //String dfDose = df.format(scheduleItem.dose());

            Medicine med = scheduleItem.schedule().medicine();
            final SpannableStringBuilder SpItem = new SpannableStringBuilder();
            SpItem.append(med.name());
            SpItem.append(":  " + scheduleItem.dose()  + " " + med.presentation()
                .units(context.getResources()));
            // add to style
            style.addLine(SpItem);
        }
        style.setBigContentTitle(title);

        if (delayMinutes > 0)
        {
            String repeatTime = DateTime.now().plusMinutes((int) delayMinutes).toString("HH:mm");
            style.setSummaryText(res.getString(R.string.notification_repeat_message, repeatTime));
        } else
        {
            style.setSummaryText(doses.size() + " meds to take at " + r.name());
        }

        final String ticker = exampleString;

        final String text =
            res.getString(R.string.message_notification_placeholder_text_template, exampleString);

        PendingIntent defaultIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        final Intent delay = new Intent(context, StartActivity.class);
        delay.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, StartActivity.ACTION_SHOW_REMINDERS);
        delay.putExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID, r.getId());

        PendingIntent delayIntent =
            PendingIntent.getActivity(context, 1, delay, PendingIntent.FLAG_CANCEL_CURRENT);

        final Intent cancel = new Intent(context, NotificationEventReceiver.class);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CANCEL_ROUTINE);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, r.getId());

        PendingIntent cancelIntent =
            PendingIntent.getBroadcast(context, 2, cancel, PendingIntent.FLAG_UPDATE_CURRENT);

        String ringtonePref = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("pref_notification_tone", null);

        Uri ringtoneUri;
        if (inistentNotifications)
        {
            ringtoneUri = ringtonePref != null ? Uri.parse(ringtonePref)
                : Settings.System.DEFAULT_ALARM_ALERT_URI;
        } else
        {
            ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

            // Set appropriate defaults for the notification light, sound,
            // and vibration.
            .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
            .setSmallIcon(R.drawable.ic_pill_small)
            .setLargeIcon(picture)
            .setContentTitle(title)
            .setContentText(text).setAutoCancel(true)
                // All fields below this line are optional.
                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
            .setLargeIcon(picture)

                // Set ticker text (preview) information for this notification.
            .setTicker(ticker)

                // Show a number. This is useful when stacking notifications of
                // a single type.
            .setNumber(doses.size())

                // If this notification relates to a past or upcoming event, you
                // should set the relevant time information using the setWhen
                // method below. If this call is omitted, the notification's
                // timestamp will by set to the time at which it was shown.
                // TODO: Call setWhen if this notification relates to a past or
                // upcoming event. The sole argument to this method should be
                // the notification timestamp in milliseconds.
                //.setWhen(...)

                // Set the pending intent to be initiated when the user touches
                // the notification.
            .setContentIntent(defaultIntent)
                //.setOngoing(true)
                // add delay button
            .addAction(R.drawable.ic_history_white_24dp, res.getString(R.string.notification_delay),
                delayIntent).addAction(R.drawable.ic_alarm_off_white_24dp,
                res.getString(R.string.notification_cancel_now), cancelIntent)
                // Show an expanded list of items on devices running Android 4.1
                // or later.
            .setStyle(style)
                //.setLights(0x00ff0000, 500, 1000)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setVibrate(new long[] { 1000, 200, 500, 200, 100, 200, 1000 }).setSound(
                ringtoneUri != null ? ringtoneUri : Settings.System.DEFAULT_NOTIFICATION_URI)
                // Automatically dismiss the notification when it is touched.
            .setAutoCancel(false);

        Notification n = builder.build();
        n.defaults = 0;
        n.ledARGB = 0x00ffa500;
        n.ledOnMS = 1000;
        n.ledOffMS = 2000;

        if (inistentNotifications)
        {
            n.flags |= Notification.FLAG_INSISTENT;
        }

        notify(context, n, NOTIFICATION_TAG);
    }

    public static void notifyHourly(final Context context, final String exampleString,
        Schedule schedule, LocalTime time, Intent intent)
    {

        // if notifications are disabled, exit
        boolean notifications = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("alarm_notifications", true);
        if (!notifications)
        {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");
        boolean inistentNotifications = prefs.getBoolean("alarm_insistent", false);
        long delayMinutes = Long.parseLong(delayMinutesStr);

        //        if(delayMinutes < 0){
        //            delayMinutes = 15;
        //        }

        final Resources res = context.getResources();

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_pill_48dp);
        //final Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_pill_48dp);
        final String title =
            res.getString(R.string.message_notification_title_template, exampleString);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        Medicine med = schedule.medicine();
        final SpannableStringBuilder SpItem = new SpannableStringBuilder();
        SpItem.append(med.name());
        SpItem.append(
            "   " + schedule.dose() + " " + med.presentation().units(context.getResources()));
        style.addLine(SpItem);

        style.setBigContentTitle(title);

        if (delayMinutes > 0)
        {
            String repeatTime = DateTime.now().plusMinutes((int) delayMinutes).toString("kk:mm");
            style.setSummaryText(res.getString(R.string.notification_repeat_message, repeatTime));
        } else
        {
            style.setSummaryText(
                med.name() + "(" + context.getString(R.string.every) + " " + schedule.rule()
                    .interval() + " " + context.getString(R.string.hours) + ")");
        }

        final String ticker = exampleString;

        final String text =
            res.getString(R.string.message_notification_placeholder_text_template, exampleString);

        PendingIntent defaultIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        final Intent delay = new Intent(context, StartActivity.class);
        delay.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, StartActivity.ACTION_SHOW_REMINDERS);
        delay.putExtra(CalendulaApp.INTENT_EXTRA_DELAY_SCHEDULE_ID, schedule.getId());
        delay.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, time.toString("kk:mm"));

        PendingIntent delayIntent =
            PendingIntent.getActivity(context, 1, delay, PendingIntent.FLAG_CANCEL_CURRENT);

        final Intent cancel = new Intent(context, NotificationEventReceiver.class);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ACTION,
            CalendulaApp.ACTION_CANCEL_HOURLY_SCHEDULE);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, time.toString("kk:mm"));

        PendingIntent cancelIntent =
            PendingIntent.getBroadcast(context, 2, cancel, PendingIntent.FLAG_UPDATE_CURRENT);

        String ringtonePref = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("pref_notification_tone", null);

        Uri ringtoneUri;
        if (inistentNotifications)
        {
            ringtoneUri = ringtonePref != null ? Uri.parse(ringtonePref)
                : Settings.System.DEFAULT_ALARM_ALERT_URI;
        } else
        {
            ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

            // Set appropriate defaults for the notification light, sound,
            // and vibration.
            .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
            .setSmallIcon(R.drawable.ic_pill_small)
            .setLargeIcon(picture)
            .setContentTitle(title)
            .setContentText(text).setAutoCancel(true)
                // All fields below this line are optional.
                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
            .setLargeIcon(picture)

                // Set ticker text (preview) information for this notification.
            .setTicker(ticker)

                // Show a number. This is useful when stacking notifications of
                // a single type.
            .setNumber(1)

                // If this notification relates to a past or upcoming event, you
                // should set the relevant time information using the setWhen
                // method below. If this call is omitted, the notification's
                // timestamp will by set to the time at which it was shown.
                // TODO: Call setWhen if this notification relates to a past or
                // upcoming event. The sole argument to this method should be
                // the notification timestamp in milliseconds.
                //.setWhen(...)

                // Set the pending intent to be initiated when the user touches
                // the notification.
            .setContentIntent(defaultIntent)
                //.setOngoing(true)
                // add delay button
            .addAction(R.drawable.ic_history_white_24dp, res.getString(R.string.notification_delay),
                delayIntent).addAction(R.drawable.ic_alarm_off_white_24dp,
                res.getString(R.string.notification_cancel_now), cancelIntent)
                // Show an expanded list of items on devices running Android 4.1
                // or later.
            .setStyle(style)
                //.setLights(0x00ff0000, 500, 1000)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setVibrate(new long[] { 1000, 200, 500, 200, 100, 200, 1000 }).setSound(
                ringtoneUri != null ? ringtoneUri : Settings.System.DEFAULT_NOTIFICATION_URI)
                // Automatically dismiss the notification when it is touched.
            .setAutoCancel(false);

        Notification n = builder.build();
        n.defaults = 0;
        n.ledARGB = 0x00ffa500;
        n.ledOnMS = 1000;
        n.ledOffMS = 2000;

        if (inistentNotifications)
        {
            n.flags |= Notification.FLAG_INSISTENT;
        }

        notify(context, n, NOTIFICATION_SCHEDULE_TAG);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification, String tag)
    {
        final NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
        {
            nm.notify(tag, 0, notification);
        } else
        {
            nm.notify(tag.hashCode(), notification);
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, Routine, java.util.List, android.content.Intent)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context)
    {
        final NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
        {
            nm.cancel(NOTIFICATION_TAG, 0);
            nm.cancel(NOTIFICATION_SCHEDULE_TAG, 0);
        } else
        {
            nm.cancel(NOTIFICATION_SCHEDULE_TAG.hashCode());
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}