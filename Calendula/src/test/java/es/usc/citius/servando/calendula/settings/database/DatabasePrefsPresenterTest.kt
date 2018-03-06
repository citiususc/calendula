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

import android.content.Intent
import es.usc.citius.servando.calendula.BuildConfig
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.drugdb.DBRegistry
import es.usc.citius.servando.calendula.kotlinAny
import es.usc.citius.servando.calendula.kotlinEq
import es.usc.citius.servando.calendula.settings.CalendulaSettingsActivity
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class DatabasePrefsPresenterTest {

    companion object {
        private const val INITIAL_DB_ID = "INITIAL_DB_ID"
        private const val NEW_DB_ID = "NEW_DB_ID"
    }

    @Mock
    private lateinit var dbPrefView: DatabasePrefsContract.View

    private lateinit var dbPrefPresenter: DatabasePrefsContract.Presenter


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        // do not return nulls
        Mockito.`when`(dbPrefView.getIntent()).thenReturn(Intent())
        Mockito.`when`(dbPrefView.resolveString(Mockito.anyInt())).thenAnswer {
            RuntimeEnvironment.application.getString(it.arguments[0] as Int)
        }
        Mockito.`when`(dbPrefView.hasDownloadPermission()).thenReturn(true)

        // init DB registry so there are DB handlers
        DBRegistry.init(RuntimeEnvironment.application)

        // Reset db id
        PreferenceUtils.edit()
            .putString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), INITIAL_DB_ID)
            .commit()


        dbPrefPresenter = DatabasePrefsPresenter(INITIAL_DB_ID)
        dbPrefPresenter.attachView(dbPrefView)
    }

    @Test
    fun start() {
        dbPrefPresenter.start()


        verify(dbPrefView).setDbList(
            kotlinAny<Array<String>>(),
            kotlinAny<Array<String>>()
        )

        // no DB intent was passed, so db dialog shouldn't have been opened
        verify(dbPrefView, never()).openDatabaseSelection()
    }

    @Test
    fun startWithDBIntent() {
        Mockito.`when`(dbPrefView.getIntent())
            .thenReturn(Intent().putExtra(CalendulaSettingsActivity.EXTRA_SHOW_DB_DIALOG, true))

        dbPrefPresenter.start()

        verify(dbPrefView).setDbList(
            kotlinAny<Array<String>>(),
            kotlinAny<Array<String>>()
        )

        verify(dbPrefView).openDatabaseSelection()
    }

    @Test
    fun currentDbUpdated() {
        dbPrefPresenter.currentDbUpdated(NEW_DB_ID)

        verify(dbPrefView).showSelectedDb(kotlinEq(NEW_DB_ID))
    }

    @Test
    fun selectDifferentDb() {
        dbPrefPresenter.selectNewDb(NEW_DB_ID)

        verify(dbPrefView).showDatabaseDownloadChoice(kotlinEq(NEW_DB_ID))
    }


    @Test
    fun selectDifferentDbNoPerms() {
        Mockito.`when`(dbPrefView.hasDownloadPermission()).thenReturn(false)

        dbPrefPresenter.selectNewDb(NEW_DB_ID)

        verify(dbPrefView, never()).showDatabaseDownloadChoice(kotlinEq(NEW_DB_ID))
        verify(dbPrefView).askForDownloadPermission(kotlinEq(NEW_DB_ID))
    }


    @Test
    fun selectSameDb() {
        dbPrefPresenter.selectNewDb(INITIAL_DB_ID)

        verify(dbPrefView, never()).showDatabaseDownloadChoice(kotlinAny())
    }

    @Test
    fun selectNoneDb() {
        val updatePref: Boolean =
            dbPrefPresenter.selectNewDb(RuntimeEnvironment.application.getString(R.string.database_none_id))

        Assert.assertEquals(
            "Calling selectDb with None should let the pref update",
            true,
            updatePref
        )
    }

    @Test
    fun onDbDownloadChoiceResultFalse() {

        dbPrefPresenter.onDbDownloadChoiceResult(false)

        Assert.assertEquals(
            "Drug DB ID should still be the same if DB download is not accepted",
            INITIAL_DB_ID,
            PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, null)
        )
    }

    @Test
    fun onDbDownloadChoiceResultTrue() {
        dbPrefPresenter.onDbDownloadChoiceResult(true)

        Assert.assertEquals(
            "Current DB should be setting up ID",
            RuntimeEnvironment.application.getString(R.string.database_setting_up_id),
            PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, null)
        )
    }

    @Test
    fun checkDatabaseUpdate() {
        //right now, just see it doesn't crash
        dbPrefPresenter.checkDatabaseUpdate(RuntimeEnvironment.application)
    }


}