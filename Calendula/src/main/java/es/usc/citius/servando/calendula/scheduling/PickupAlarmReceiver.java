package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.activities.CalendarActivity;
import es.usc.citius.servando.calendula.activities.PickupNotification;

/**
 * This class receives our pickup check alarms
 */
public class PickupAlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "PickupReceiver.class";


    @Override
    public void onReceive(Context context, Intent intent) {
        {
            if (CalendulaApp.disableReceivers)
                return;

            // get action type
            int action = intent.getIntExtra(CalendulaApp.INTENT_EXTRA_ACTION, -1);

            Log.d(TAG, "Alarm received - Action : " + action);

            if (action == CalendulaApp.ACTION_CHECK_PICKUPS_ALARM) {
                String description = "Recuerda recoger tus medicinas!";
                showNotification(context, description);
            }
        }
    }

    private void showNotification(Context ctx, String description) {
        String title = "Remember to pickup your meds";
        Intent i = new Intent(ctx, CalendarActivity.class);
        i.putExtra("action", CalendarActivity.ACTION_SHOW_REMINDERS);
        PickupNotification.notify(ctx, title, description, i);
    }

}