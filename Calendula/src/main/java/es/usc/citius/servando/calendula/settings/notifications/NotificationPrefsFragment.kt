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
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.preference.Preference
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.settings.CalendulaPrefsFragment
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceKeys


/**
 * Instantiated via reflection, don't delete!
 */
class NotificationPrefsFragment :
    CalendulaPrefsFragment<NotificationPrefsContract.View, NotificationPrefsContract.Presenter>(),
    NotificationPrefsContract.View {

    companion object {
        private const val TAG = "NotificationPrefsFragm"
    }

    private val applicationPackageName: String by lazy { activity!!.packageName as String }
    override val fragmentTitle: Int = R.string.pref_header_notifications
    override val presenter: NotificationPrefsContract.Presenter by lazy {
        NotificationPrefsPresenter(
            RingtoneNameResolver(context!!)
        )
    }

    private val notificationPref by lazy { findPreference(PreferenceKeys.SETTINGS_NOTIFICATION_TONE.key()) }
    private val insistentNotificationPref by lazy { findPreference(PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE.key()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        LogUtil.d(TAG, "onCreatePreferences called")
        addPreferencesFromResource(R.xml.pref_notifications)
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
            PreferenceKeys.SETTINGS_NOTIFICATION_MANAGEMENT.key() -> {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                           .putExtra(Settings.EXTRA_APP_PACKAGE, applicationPackageName)
                   startActivity(intent)
                }
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
        val preference = findPreference(PreferenceKeys.SETTINGS_STOCK_ALERT_DAYS.key())
        preference.isEnabled = false
        preference.isVisible = false
    }

    override fun setNotificationRingtoneText(text: String) {
        notificationPref.summary = text
    }

    override fun setInsistentRingtoneText(text: String) {
        insistentNotificationPref.summary = text
    }

    override fun setVisibleNotificationManagementPref(visible: Boolean){
        findPreference(PreferenceKeys.SETTINGS_NOTIFICATION_TONE.key()).apply{
            isEnabled = !visible
            isVisible = !visible
        }
        findPreference(PreferenceKeys.SETTINGS_NOTIFICATION_VIBRATION.key()).apply{
            isEnabled = !visible
            isVisible = !visible
        }
        findPreference(PreferenceKeys.SETTINGS_NOTIFICATION_MANAGEMENT.key()).apply{
            isEnabled = visible
            isVisible = visible
        }
    }

}