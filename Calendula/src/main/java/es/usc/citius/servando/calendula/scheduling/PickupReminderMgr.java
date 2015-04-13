package es.usc.citius.servando.calendula.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.DateTime;

import es.usc.citius.servando.calendula.CalendulaApp;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
public class PickupReminderMgr {


    private static final String TAG = PickupReminderMgr.class.getName();
    // static instance
    private static final PickupReminderMgr instance = new PickupReminderMgr();

    private PickupReminderMgr() {
    }


    // static method to get the PickupReminderMgr instance
    public static PickupReminderMgr instance() {
        return instance;
    }


    private PendingIntent alarmPendingIntent(Context ctx) {
        // intent our receiver will receive
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        // indicate thar is for a routine
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CHECK_PICKUPS_ALARM);
        // create pending intent
        int intent_id = "ACTION_CHECK_PICKUPS_ALARM".hashCode();
        return PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setCheckPickupsAlarm(Context ctx) {
        // TODO: Get time from settings
        DateTime d = DateTime.now().withHourOfDay(10).withMinuteOfHour(0).withSecondOfMinute(0);

        PendingIntent routinePendingIntent = alarmPendingIntent(ctx);
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        // set the routine alarm, with repetition every day
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, d.getMillis(), AlarmManager.INTERVAL_DAY, routinePendingIntent);
            Log.d(TAG, "Pickup check alarm scheduled!");
        }

    }


}
