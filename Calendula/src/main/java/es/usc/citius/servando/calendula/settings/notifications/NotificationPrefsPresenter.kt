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

package es.usc.citius.servando.calendula.settings.notifications

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import es.usc.citius.servando.calendula.modules.ModuleManager
import es.usc.citius.servando.calendula.modules.modules.StockModule
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils


/**
 * Created by alvaro.brey.vilas on 5/02/18.
 */
class NotificationPrefsPresenter(
    val view: NotificationPrefsContract.View,
    private val ringtoneNameResolver: RingtoneNameResolver
) :
    NotificationPrefsContract.Presenter {

    companion object {
        private const val TAG = "NotifPrefsPresenter"
        const val REQ_CODE_NOTIF_RINGTONE = 40
        const val REQ_CODE_INSIST_RINGTONE = 41
    }

    init {
        view.presenter = this
    }

    override fun start() {
        if (!ModuleManager.isEnabled(StockModule.ID)) {
            view.hideStockPref()
        }
        updateRingtoneSummaries()
    }


    override fun onResult(reqCode: Int, result: Int, data: Intent?) {
        LogUtil.d(TAG, "onResult() called with reqCode=$reqCode, result=$result, data=$data")
        if ((reqCode == REQ_CODE_INSIST_RINGTONE || reqCode == REQ_CODE_NOTIF_RINGTONE) && data != null) {
            val ringtone: Uri? = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val prefKey: PreferenceKeys = when (reqCode) {
                REQ_CODE_NOTIF_RINGTONE -> PreferenceKeys.SETTINGS_NOTIFICATION_TONE
                REQ_CODE_INSIST_RINGTONE -> PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE
                else -> {
                    LogUtil.wtf(TAG, "invalid request!")
                    throw IllegalArgumentException("Invalid request code $reqCode")
                }
            }
            LogUtil.d(TAG, "onResult: key=$prefKey, selected tone=$ringtone")
            PreferenceUtils.edit().putString(prefKey.key(), ringtone?.toString() ?: "").apply()
            updateRingtoneSummaries()
        } else {
            LogUtil.d(TAG, "onResult: unexpected result or missing data")
        }
    }


    override fun selectNotificationRingtone() {
        doRequestRingtone(
            PreferenceKeys.SETTINGS_NOTIFICATION_TONE,
            REQ_CODE_NOTIF_RINGTONE,
            RingtoneManager.TYPE_NOTIFICATION
        )
    }

    override fun selectInsistentRingtone() {
        doRequestRingtone(
            PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE,
            REQ_CODE_INSIST_RINGTONE,
            RingtoneManager.TYPE_ALARM
        )
    }


    private fun doRequestRingtone(pref: PreferenceKeys, requestCode: Int, ringtoneType: Int) {
        val currentUri: Uri? = findRingtoneUri(pref)
        LogUtil.d(
            TAG,
            "callSelectRingtone: currentUri=$currentUri, pref=$pref, requestCode=$requestCode"
        )

        view.requestRingtone(requestCode, ringtoneType, currentUri)
    }

    private fun findRingtoneUri(key: PreferenceKeys): Uri? {
        val currentTone =
            PreferenceUtils.getString(key, "default")
        return when (currentTone) {
            "" -> null
            "default" -> Settings.System.DEFAULT_NOTIFICATION_URI
            else -> Uri.parse(currentTone)
        }
    }

    private fun updateRingtoneSummaries() {
        view.setNotificationRingtoneText(
            ringtoneNameResolver.resolveRingtoneName(
                findRingtoneUri(
                    PreferenceKeys.SETTINGS_NOTIFICATION_TONE
                )
            )
        )
        view.setInsistentRingtoneText(
            ringtoneNameResolver.resolveRingtoneName(
                findRingtoneUri(
                    PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE
                )
            )
        )
    }


}