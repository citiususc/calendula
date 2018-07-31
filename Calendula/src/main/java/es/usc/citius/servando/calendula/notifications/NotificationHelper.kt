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

package es.usc.citius.servando.calendula.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import es.usc.citius.servando.calendula.R


object NotificationHelper {


    /**
     * Intended for high-importance med notifications such as intake reminders.
     */
    const val CHANNEL_MEDS_ID = "calendula.channels.meds"
    const val CHANNEL_DEFAULT_ID = "calendula.channels.default"


    /**
     * Creates notification channels for the app (required from api 26 up).
     * This does nothing if the channels already exist: it's safe to call on every app startup.
     *
     * @param context a [Context], required for getting strings and the notification manager
     */
    @JvmStatic
    fun createNotificationChannels(context: Context) {
        // don't do anything under api 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //create meds channel
            val medChannelName = context.getString(R.string.channel_meds_name)
            val medChannelDesc = context.getString(R.string.channel_meds_description)
            val medChannel = NotificationChannel(
                CHANNEL_MEDS_ID,
                medChannelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            medChannel.description = medChannelDesc
            //create other channel
            val defaultChannelName = context.getString(R.string.channel_default_name)
            val defaultChannelDesc = context.getString(R.string.channel_default_description)
            val defaultChannel = NotificationChannel(
                CHANNEL_DEFAULT_ID,
                defaultChannelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            defaultChannel.description = defaultChannelDesc

            // register the channels
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(listOf(medChannel, defaultChannel))
        }
    }

}