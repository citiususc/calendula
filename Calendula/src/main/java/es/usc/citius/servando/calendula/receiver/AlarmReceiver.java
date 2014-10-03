package es.usc.citius.servando.calendula.receiver;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import es.usc.citius.servando.calendula.AlarmScheduler;

/**
 * This class receives our routine alarms
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = AlarmReceiver.class.getName();


	@Override
	public void onReceive(Context context, Intent intent)
	{
		{
            // get action type
            int action = intent.getIntExtra(AlarmScheduler.INTENT_EXTRA_ACTION, -1);

            Log.d(TAG, "Alarm received - Action : " + action);

            // routine time
            if (action == AlarmScheduler.ACTION_ROUTINE_TIME)
            {
                // get the routine hash code from the intent
                String routineId = intent.getStringExtra(AlarmScheduler.INTENT_EXTRA_ROUTINE_ID);
                // and call scheduler
                AlarmScheduler.instance().onAlarmReceived(routineId, context);
            }
		}
	}

}