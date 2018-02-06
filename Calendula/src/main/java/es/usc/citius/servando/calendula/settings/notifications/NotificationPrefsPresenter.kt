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
    val rnameResolver: RingtoneNameResolver
) :
    NotificationPrefsContract.Presenter {


    companion object {
        private const val TAG = "NotifPrefsPresenter"
        private const val REQ_CODE_NOTIF_RINGTONE = 40
        private const val REQ_CODE_INSIST_RINGTONE = 41
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
        if (reqCode == REQ_CODE_INSIST_RINGTONE || reqCode == REQ_CODE_NOTIF_RINGTONE && data != null) {
            val ringtone: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
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

        val currentUri: Uri? = uri(PreferenceKeys.SETTINGS_NOTIFICATION_TONE)
        LogUtil.d(TAG, "selectNotificationRingtone: currentUri=$currentUri")

        view.requestRingtone(REQ_CODE_NOTIF_RINGTONE, currentUri)

    }

    override fun selectInsistentRingtone() {

        val currentUri: Uri? = uri(PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE)
        LogUtil.d(TAG, "selectInsistentRingtone: currentUri=$currentUri")

        view.requestRingtone(REQ_CODE_INSIST_RINGTONE, currentUri)
    }

    private fun uri(key: PreferenceKeys): Uri? {
        val currentTone =
            PreferenceUtils.getString(key, "default")
        return when (currentTone) {
            "" -> null
            "default" -> Settings.System.DEFAULT_NOTIFICATION_URI
            else -> Uri.parse(currentTone)
        }
    }

    private fun updateRingtoneSummaries() {
        view.setNotificationRingtoneText(rnameResolver.resolveRingtoneName(uri(PreferenceKeys.SETTINGS_NOTIFICATION_TONE)))
        view.setInsistentRingtoneText(rnameResolver.resolveRingtoneName(uri(PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE)))
    }


}