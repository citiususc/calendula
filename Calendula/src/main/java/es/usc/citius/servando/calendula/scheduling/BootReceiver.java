package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This class receives our routine alarms
 */
public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = BootReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(TAG, "Boot completed intent received");
            // Update alarms
            AlarmScheduler.instance().updateAllAlarms(context);
            Log.d(TAG, "Alarms updated!");
        }
    }
}