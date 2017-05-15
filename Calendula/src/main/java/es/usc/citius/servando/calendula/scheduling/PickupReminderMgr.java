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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
public class PickupReminderMgr {


    private static final String TAG = "PickupReminderMgr";
    // static instance
    private static final PickupReminderMgr instance = new PickupReminderMgr();

    private PickupReminderMgr() {
    }


    // static method to get the PickupReminderMgr instance
    public static PickupReminderMgr instance() {
        return instance;
    }

    public void setCheckPickupsAlarm(Context ctx, LocalDate date) {

        DateTime d = date.toDateTimeAtStartOfDay().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
        PendingIntent calendarReminderPendingIntent = alarmPendingIntent(ctx);
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, d.getMillis(), calendarReminderPendingIntent);
            LogUtil.d(TAG, "Pickup check alarm scheduled!");
        }
    }

    private PendingIntent alarmPendingIntent(Context ctx) {
        Intent intent = new Intent(ctx, PickupAlarmReceiver.class);
        intent.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CHECK_PICKUPS_ALARM);
        int intent_id = "ACTION_CHECK_PICKUPS_ALARM".hashCode();
        return PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
