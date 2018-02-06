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

package es.usc.citius.servando.calendula.settings.database

import android.content.SharedPreferences
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.drugdb.download.DownloadDatabaseHelper
import es.usc.citius.servando.calendula.settings.CalendulaPrefsFragment
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils
import org.jetbrains.anko.toast


/**
 * Instantiated via reflection, don't delete!
 * Created by alvaro.brey.vilas on 1/02/18.
 */
class DatabasePrefsFragment : CalendulaPrefsFragment(), DatabasePrefsContract.View {

    companion object {
        private const val TAG = "DatabasePrefsFragment"
    }

    override lateinit var presenter: DatabasePrefsContract.Presenter
    override val fragmentTitle: Int = R.string.pref_header_prescriptions


    private val dbPref: ListPreference by lazy {
        findPreference(getString(R.string.prefkey_drugdb_current_db)) as ListPreference
    }
    private val updateDBPref: Preference by lazy {
        findPreference(getString(R.string.prefkey_settings_database_update))
    }

    private val noneId by lazy { getString(R.string.database_none_id) }
    private val settingUpId by lazy { getString(R.string.database_setting_up_id) }


    override fun onResume() {
        super.onResume()
        presenter.start()
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        LogUtil.d(TAG, "onCreatePreferences() called")
        addPreferencesFromResource(R.xml.pref_database)

        // set listeners for our prefs
        dbPref.setOnPreferenceChangeListener { _, newValue ->
            presenter.selectNewDb(newValue as String)
        }
        updateDBPref.setOnPreferenceClickListener {
            presenter.checkDatabaseUpdate(context)
            true
        }

        DatabasePrefsPresenter(
            this, PreferenceUtils.getString(
                PreferenceKeys.DRUGDB_CURRENT_DB,
                getString(R.string.database_none_id)
            )
        )

    }

    /**
     * From [SharedPreferences.OnSharedPreferenceChangeListener]
     *
     * @see [CalendulaPrefsFragment]
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        LogUtil.d(TAG, "onSharedPreferenceChanged: preference $key changed")
        if (key == dbPref.key) {
            presenter.currentDbUpdated(
                PreferenceUtils.getString(
                    PreferenceKeys.DRUGDB_CURRENT_DB,
                    noneId
                )
            )
        }
    }


    override fun setDbList(dbIds: Array<String>, dbDisplayNames: Array<String>) {
        dbPref.entryValues = dbIds
        dbPref.entries = dbDisplayNames
        refreshUi()
    }

    override fun resolveString(@StringRes stringRes: Int): String {
        return context.getString(stringRes)
    }

    override fun showSelectedDb(dbId: String) {
        dbPref.value = dbId
        refreshUi()
    }

    override fun showDatabaseDownloadChoice(dbId: String) {
        DownloadDatabaseHelper.instance()
            .showDownloadDialog(activity, dbId, { accepted ->
                presenter.onDbDownloadChoiceResult(accepted)
            })
    }

    override fun showDatabaseUpdateNotAvailable() {
        activity.toast(R.string.database_update_not_available)
    }


    private fun refreshUi() {
        LogUtil.d(TAG, "refreshUi() called")
        if (dbPref.value == settingUpId) {
            dbPref.summary = getString(R.string.database_setting_up)
            dbPref.isEnabled = false
        } else {
            dbPref.summary = dbPref.entry
            dbPref.isEnabled = true
        }

        updateDBPref.isEnabled = dbPref.isEnabled && dbPref.value !=
                noneId
    }


}