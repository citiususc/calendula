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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.activities.CalendarActivity;
import es.usc.citius.servando.calendula.activities.PickupNotification;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * This class receives our pickup check alarms
 */
public class PickupAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "PickupReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        {
            if (CalendulaApp.disableReceivers)
                return;

            // get action type
            int action = intent.getIntExtra(CalendulaApp.INTENT_EXTRA_ACTION, -1);

            LogUtil.d(TAG, "Alarm received - Action : " + action);

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