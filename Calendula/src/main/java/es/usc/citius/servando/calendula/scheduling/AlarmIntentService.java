package es.usc.citius.servando.calendula.scheduling;

import android.content.Intent;
import android.util.Log;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.util.WakeIntentService;

/**
 * Created by joseangel.pineiro on 11/20/15.
 */
public class AlarmIntentService extends WakeIntentService {

    public static final String TAG = "AlarmIntentService";

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    public void doReminderWork(Intent intent) {


        Log.d(TAG, "Service started");

        // get intent params with alarm info
        AlarmIntentParams params = intent.getParcelableExtra(AlarmScheduler.EXTRA_PARAMS);

        if(params == null)
        {
            Log.w(TAG, "No extra params supplied");
            return;
        }

        Log.d(TAG, "Alarm received: " + params.toString());

        if(params.action != CalendulaApp.ACTION_DAILY_ALARM) {
            try {
                params.date();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        switch (params.action)
        {
            case CalendulaApp.ACTION_ROUTINE_TIME:
            case CalendulaApp.ACTION_ROUTINE_DELAYED_TIME:
                AlarmScheduler.instance().onAlarmReceived(params, this.getApplicationContext());
                break;

            case CalendulaApp.ACTION_HOURLY_SCHEDULE_TIME:
            case CalendulaApp.ACTION_HOURLY_SCHEDULE_DELAYED_TIME:
                AlarmScheduler.instance().onHourlyAlarmReceived(params, this.getApplicationContext());
                break;

            case CalendulaApp.ACTION_DAILY_ALARM:
                Log.d(TAG, "Received daily alarm");
                DailyAgenda.instance().setupForToday(this.getApplicationContext(), false);
                break;
            default:
                Log.w(TAG, "Unknown action received");
                break;
        }


    }
}
