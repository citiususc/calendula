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

package es.usc.citius.servando.calendula.activities.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.drugdb.DBRegistry
import es.usc.citius.servando.calendula.drugdb.download.DownloadDatabaseHelper
import es.usc.citius.servando.calendula.jobs.CheckDatabaseUpdatesJob
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils
import org.jetbrains.anko.toast
import java.lang.ref.WeakReference


/**
 * Instantiated via reflection, don't delete!
 * Created by alvaro.brey.vilas on 1/02/18.
 */
class DatabasePrefsFragment : CalendulaPrefsFragment() {


    companion object {
        private const val TAG = "DatabasePrefsFragment"
    }

    private val dbPref: ListPreference by lazy {
        findPreference(getString(R.string.prefkey_drugdb_current_db)) as ListPreference
    }
    private val updateDBPref: Preference by lazy {
        findPreference(getString(R.string.prefkey_settings_database_update))
    }

    private val noneId by lazy { getString(R.string.database_none_id) }
    private val settingUpId by lazy { getString(R.string.database_setting_up_id) }

    private lateinit var currentDb: String


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        LogUtil.d(TAG, "onCreatePreferences called")
        addPreferencesFromResource(R.xml.pref_database)

        // initialize shared prefs we're going to listen to
        currentDb = PreferenceUtils.getString(
            PreferenceKeys.DRUGDB_CURRENT_DB,
            getString(R.string.database_none_id)
        )

        // set listeners for our prefs
        dbPref.setOnPreferenceChangeListener { preference, newValue ->
            onChangeDrugDbPref(
                newValue as String
            )
        }
        updateDBPref.setOnPreferenceClickListener {
            CheckDbUpdateTask().execute(context)
            true
        }

        // load values for db list
        val entryValues =
            DBRegistry.instance().registered.toMutableList().also { it.add(0, noneId) }
                .toTypedArray()
        val entries =
            entryValues.drop(1).map { DBRegistry.instance().db(it).displayName() }.toMutableList()
                .also { it.add(0, getString(R.string.database_none_display)) }.toTypedArray()
        dbPref.entryValues = entryValues
        dbPref.entries = entries

        // finally refresh the UI
        refreshUi()
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        LogUtil.d(TAG, "onSharedPreferenceChanged: preference $key changed")
        when (key) {
            dbPref.key -> {
                currentDb = PreferenceUtils.getString(
                    PreferenceKeys.DRUGDB_CURRENT_DB,
                    noneId
                )
                LogUtil.d(TAG, "onSharedPreferenceChanged: current db is $currentDb")
                dbPref.value = currentDb
                refreshUi()
            }
        }
    }

    private fun refreshUi() {
        LogUtil.d(TAG, "refreshUi called")


        if (currentDb == settingUpId) {
            dbPref.summary = getString(R.string.database_setting_up)
            dbPref.isEnabled = false
        } else {
            dbPref.summary = dbPref.entry
            dbPref.isEnabled = true
        }

        updateDBPref.isEnabled = dbPref.isEnabled && dbPref.value !=
                noneId
    }


    private fun onChangeDrugDbPref(
        newValue: String
    ): Boolean {
        LogUtil.d(TAG, "onChangeDrugDbPref: new value= $newValue, current value = $currentDb")

        if (newValue != currentDb) {
            if (newValue != noneId && noneId != settingUpId) {
                DownloadDatabaseHelper.instance()
                    .showDownloadDialog(activity, newValue, { accepted ->
                        if (accepted) {
                            PreferenceUtils.edit()
                                .putString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), settingUpId)
                                .apply()
                        }
                    })
                return false
            } else if (newValue == noneId) {
                DBRegistry.instance().clear()
                PreferenceUtils.edit().putString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), noneId)
                    .apply()
            }
        }

        return true
    }

    class CheckDbUpdateTask : AsyncTask<Context, Void, Boolean>() {

        private lateinit var ctxRef: WeakReference<Context>

        override fun doInBackground(vararg params: Context?): Boolean {
            if (params.size == 1) {
                val ctx: Context = params[0]!!
                ctxRef = WeakReference(ctx)
                return CheckDatabaseUpdatesJob().checkForUpdate(ctx)
            } else {
                throw IllegalArgumentException("CheckDatabaseTag needs a Context!")
            }
        }

        override fun onPostExecute(update: Boolean?) {
            val ctx = ctxRef.get()
            if (!update!! && ctx != null) {
                ctx.toast(R.string.database_update_not_available)
            }
        }

    }

}