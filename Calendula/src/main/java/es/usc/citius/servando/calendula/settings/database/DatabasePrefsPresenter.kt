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

import android.content.Context
import android.os.AsyncTask
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.drugdb.DBRegistry
import es.usc.citius.servando.calendula.jobs.CheckDatabaseUpdatesJob
import es.usc.citius.servando.calendula.mvp.BasePresenter
import es.usc.citius.servando.calendula.settings.CalendulaSettingsActivity
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils

/**
 * Created by alvaro.brey.vilas on 5/02/18.
 */
class DatabasePrefsPresenter(
    private var currentDbId: String
) :
    DatabasePrefsContract.Presenter, BasePresenter<DatabasePrefsContract.View>() {

    companion object {
        private val TAG = "DatabasePrefsPresenter"
    }

    private lateinit var noneId: String
    private lateinit var settingUpId: String
    private lateinit var noneDisplay: String


    override fun attachView(view: DatabasePrefsContract.View) {
        super.attachView(view)
        noneDisplay = view.resolveString(R.string.database_none_display)
        noneId = view.resolveString(R.string.database_none_id)
        settingUpId = view.resolveString(R.string.database_setting_up_id)
    }

    override fun start() {
        LogUtil.d(TAG, "start() called")
        val (entries, entryValues) = getActiveDbs()
        view.setDbList(entryValues, entries)
        if (view.getIntent().getBooleanExtra(
                CalendulaSettingsActivity.EXTRA_SHOW_DB_DIALOG,
                false
            )) {
            view.openDatabaseSelection()
        }
    }

    override fun currentDbUpdated(dbId: String) {
        LogUtil.d(TAG, "currentDbUpdated() called with dbId=$dbId")
        currentDbId = dbId
        view.showSelectedDb(currentDbId)
    }

    private fun getActiveDbs(): Pair<Array<String>, Array<String>> {
        // load values for db list
        val entryValues =
            DBRegistry.instance().registered.toMutableList().also { it.add(0, noneId) }
                .toTypedArray()
        val entries =
            entryValues.drop(1).map { DBRegistry.instance().db(it).displayName() }.toMutableList()
                .also { it.add(0, noneDisplay) }.toTypedArray()
        return Pair(entries, entryValues)
    }

    /**
     * Called when the user tries to select a new database pref.
     *
     * @param dbId the selected DB's ID
     * @return `true` if the pref can be directly updated (with no further operations needed), `false` otherwise
     */
    override fun selectNewDb(dbId: String): Boolean {
        LogUtil.d(TAG, "selectNewDb() called with dbId=$dbId")
        if (dbId != currentDbId) {
            // if there is no actual update just return true and skip checks

            if (dbId != noneId && noneId != settingUpId) {
                view.showDatabaseDownloadChoice(dbId)
                return false
            } else if (dbId == noneId) {
                // if the db ID is "none", delete the current DB and let the pref update
                DBRegistry.instance().clear()
                return true
            }
        }

        return true
    }

    override fun onDbDownloadChoiceResult(result: Boolean) {
        if (result) {
            PreferenceUtils.edit()
                .putString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), settingUpId)
                .apply()
        }
    }

    override fun checkDatabaseUpdate(ctx: Context) {
        CheckDbUpdateTask(this).execute(ctx)
    }

    private fun notifyNoUpdate() {
        view.showDatabaseUpdateNotAvailable()
    }

    class CheckDbUpdateTask(private val presenter: DatabasePrefsPresenter) :
        AsyncTask<Context, Void, Boolean>() {


        override fun doInBackground(vararg params: Context?): Boolean {
            if (params.size == 1) {
                val ctx: Context = params[0]!!
                return CheckDatabaseUpdatesJob().checkForUpdate(ctx)
            } else {
                throw IllegalArgumentException("CheckDatabaseTag needs a Context!")
            }
        }

        override fun onPostExecute(update: Boolean?) {
            if (!update!!) {
                presenter.notifyNoUpdate()
            }
        }

    }

}