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
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.preference.Preference
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.settings.CalendulaPrefsFragment
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceKeys


/**
 * Instantiated via reflection, don't delete!
 *
 * Created by alvaro.brey.vilas on 1/02/18.
 */
class NotificationPrefsFragment : CalendulaPrefsFragment(), NotificationPrefsContract.View {

    companion object {
        private const val TAG = "NotificationPrefsFragm"
    }

    override lateinit var presenter: NotificationPrefsContract.Presenter
    override val fragmentTitle: Int = R.string.pref_header_notifications


    private val notificationPref by lazy { findPreference(PreferenceKeys.SETTINGS_NOTIFICATION_TONE.key()) }
    private val insistentNotificationPref by lazy { findPreference(PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE.key()) }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        LogUtil.d(TAG, "onCreatePreferences called")
        addPreferencesFromResource(R.xml.pref_notifications)

        NotificationPrefsPresenter(this, RingtoneNameResolver(context))
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            PreferenceKeys.SETTINGS_NOTIFICATION_TONE.key() -> {
                presenter.selectNotificationRingtone()
                return true
            }
            PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE.key() -> {
                presenter.selectInsistentRingtone()
                return true
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onResult(requestCode, resultCode, data)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        //noop
    }


    override fun requestRingtone(reqCode: Int, ringtoneType: Int, currentValue: Uri?) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        // show default and silent ringtones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)

        // select ringtone type (alarm or notification)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType)

        // set default element
        val defaultUri = when (ringtoneType) {
            RingtoneManager.TYPE_ALARM -> Settings.System.DEFAULT_ALARM_ALERT_URI
            else -> Settings.System.DEFAULT_NOTIFICATION_URI
        }
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
            defaultUri
        )

        // set the current value
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
            currentValue
        )

        startActivityForResult(intent, reqCode)
    }

    override fun hideStockPref() {
        findPreference(PreferenceKeys.SETTINGS_STOCK_ALERT_DAYS.key()).isEnabled = false
        findPreference(PreferenceKeys.SETTINGS_STOCK_ALERT_DAYS.key()).isVisible = false
    }

    override fun setNotificationRingtoneText(text: String) {
        notificationPref.summary = text
    }

    override fun setInsistentRingtoneText(text: String) {
        insistentNotificationPref.summary = text
    }


}