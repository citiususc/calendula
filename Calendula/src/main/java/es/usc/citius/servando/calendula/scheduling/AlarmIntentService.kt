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

package es.usc.citius.servando.calendula.scheduling

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService

import es.usc.citius.servando.calendula.CalendulaApp
import es.usc.citius.servando.calendula.util.LogUtil

class AlarmIntentService : JobIntentService() {

    companion object {

        private const val TAG = "AlarmIntentService"

        private const val JOB_ID = 1

        @JvmStatic
        fun enqueueWork(context: Context, work: Intent) {
            JobIntentService.enqueueWork(context, AlarmIntentService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {

        LogUtil.d(TAG, "Service started")

        // get intent params with alarm info
        val params = AlarmScheduler.getAlarmParams(intent)

        if (params == null) {
            LogUtil.w(TAG, "No extra params supplied")
            return
        }

        LogUtil.d(TAG, "Alarm received: " + params.toString())

        if (params.action != CalendulaApp.ACTION_DAILY_ALARM) {
            try {
                params.date()
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }

        }

        when (params.action) {
            CalendulaApp.ACTION_ROUTINE_TIME, CalendulaApp.ACTION_ROUTINE_DELAYED_TIME -> {
                AlarmScheduler.instance().onAlarmReceived(
                    params,
                    this.applicationContextg
                )
            }

            CalendulaApp.ACTION_HOURLY_SCHEDULE_TIME, CalendulaApp.ACTION_HOURLY_SCHEDULE_DELAYED_TIME -> {
                AlarmScheduler.instance().onHourlyAlarmReceived(
                    params,
                    this.applicationContext
                )
            }

            CalendulaApp.ACTION_DAILY_ALARM -> {
                LogUtil.d(TAG, "Received daily alarm")
                DailyAgenda.instance().setupForToday(this.applicationContext, false)
            }
            else -> LogUtil.w(TAG, "Unknown action received")
        }
    }

}
