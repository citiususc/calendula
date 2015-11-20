package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import es.usc.citius.servando.calendula.CalendulaApp;

/**
 * This class receives our routine alarms
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmReceiver.class";

    @Override
    public void onReceive(Context context, Intent intent)
    {

        // TODO: Switch to a IntentService
        // https://github.com/mitrejcevski/android_tasks_widget/blob/master/TasksWidget/widget/src/main/java/com/mitrejcevski/widget/notification/WakeIntentService.java


        if (CalendulaApp.disableReceivers) { return; }

        // get intent params with alarm info
        AlarmIntentParams params = intent.getParcelableExtra(AlarmScheduler.EXTRA_PARAMS);

        if(params == null)
        {
            Log.w(TAG, "No extra params supplied");
            return;
        }

        Log.d(TAG, "Alarm received: " + params.toString());

        try {
            params.date();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        switch (params.action)
        {
            case CalendulaApp.ACTION_ROUTINE_TIME:
            case CalendulaApp.ACTION_ROUTINE_DELAYED_TIME:
                AlarmScheduler.instance().onAlarmReceived(params, context);
                break;

            case CalendulaApp.ACTION_HOURLY_SCHEDULE_TIME:
            case CalendulaApp.ACTION_HOURLY_SCHEDULE_DELAYED_TIME:
                AlarmScheduler.instance().onHourlyAlarmReceived(params, context);
                break;

            case CalendulaApp.ACTION_DAILY_ALARM:
                DailyAgenda.instance().setupForToday(context.getApplicationContext(), false);
                break;

            default:
                Log.w(TAG, "Unknown action received");
                break;
        }
    }
}

