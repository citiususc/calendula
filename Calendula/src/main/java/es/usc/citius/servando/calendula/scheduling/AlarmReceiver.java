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

        if (CalendulaApp.disableReceivers) { return; }

        AlarmIntentParams params = AlarmScheduler.getAlarmParams(intent);

        if(params == null)
        {
            Log.w(TAG, "No extra params supplied");
            return;
        }else{
            Log.d(TAG, "Received alarm: " + params.action);
        }

        Intent serviceIntent = new Intent(context, AlarmIntentService.class);
        serviceIntent.putExtra(AlarmScheduler.EXTRA_PARAMS, AlarmScheduler.alarmParams(params));
        context.startService(serviceIntent);
    }
}