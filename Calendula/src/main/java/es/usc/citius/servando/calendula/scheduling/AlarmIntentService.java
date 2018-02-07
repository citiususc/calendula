/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

import android.content.Intent;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.WakeIntentService;

/**
 * Created by joseangel.pineiro on 11/20/15.
 */
public class AlarmIntentService extends WakeIntentService {

    private static final String TAG = "AlarmIntentService";

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    public void doReminderWork(Intent intent) {


        LogUtil.d(TAG, "Service started");

        // get intent params with alarm info
        AlarmIntentParams params = AlarmScheduler.getAlarmParams(intent);

        if (params == null) {
            LogUtil.w(TAG, "No extra params supplied");
            return;
        }

        LogUtil.d(TAG, "Alarm received: " + params.toString());

        if (params.action != CalendulaApp.ACTION_DAILY_ALARM) {
            try {
                params.date();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        switch (params.action) {
            case CalendulaApp.ACTION_ROUTINE_TIME:
            case CalendulaApp.ACTION_ROUTINE_DELAYED_TIME:
                AlarmScheduler.instance().onAlarmReceived(params, this.getApplicationContext());
                break;

            case CalendulaApp.ACTION_HOURLY_SCHEDULE_TIME:
            case CalendulaApp.ACTION_HOURLY_SCHEDULE_DELAYED_TIME:
                AlarmScheduler.instance().onHourlyAlarmReceived(params, this.getApplicationContext());
                break;

            case CalendulaApp.ACTION_DAILY_ALARM:
                LogUtil.d(TAG, "Received daily alarm");
                DailyAgenda.instance().setupForToday(this.getApplicationContext(), false);
                break;
            default:
                LogUtil.w(TAG, "Unknown action received");
                break;
        }


    }
}
