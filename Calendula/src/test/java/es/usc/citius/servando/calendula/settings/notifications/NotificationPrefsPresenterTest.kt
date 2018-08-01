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

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import es.usc.citius.servando.calendula.BuildConfig
import es.usc.citius.servando.calendula.kotlinAny
import es.usc.citius.servando.calendula.kotlinEq
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class NotificationPrefsPresenterTest {


    companion object {
        private const val TEST_RINGTONE_URI = "com.foo"
        private const val INITIAL_RINGTONE_URI = "com.test"
    }

    @Mock
    private lateinit var view: NotificationPrefsContract.View
    @Mock
    private lateinit var resolver: RingtoneNameResolver

    private lateinit var presenter: NotificationPrefsContract.Presenter

    @Before
    fun setUp() {

        MockitoAnnotations.initMocks(this)

        Mockito.`when`(resolver.resolveRingtoneName(kotlinAny<Uri>())).thenReturn("foo")

        PreferenceUtils.edit().putString(
            PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE.key(),
            INITIAL_RINGTONE_URI
        ).apply()

        presenter = NotificationPrefsPresenter(
            resolver
        )

        presenter.attachView(view)
    }

    @Test
    fun start() {
        presenter.start()

        // presenter should initialize ringtone names
        verify(view).setInsistentRingtoneText(kotlinAny<String>())
        verify(view).setNotificationRingtoneText(kotlinAny<String>())
    }

    @Test
    fun onResultValid() {
        presenter.onResult(
            NotificationPrefsPresenter.REQ_CODE_INSIST_RINGTONE, Activity.RESULT_OK,
            Intent().putExtra(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                Uri.parse(TEST_RINGTONE_URI)
            )
        )


        // check the pref is set
        Assert.assertEquals(
            "Ringtone URI is not correct", TEST_RINGTONE_URI, PreferenceUtils.getString(
                PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE, ""
            )
        )
        // check the summary has been updated
        verify(view).setInsistentRingtoneText(kotlinAny<String>())

    }

    @Test
    fun onResultInvalid() {
        // missing data
        presenter.onResult(
            NotificationPrefsPresenter.REQ_CODE_INSIST_RINGTONE, Activity.RESULT_OK,
            null
        )

        // check the preference is still the same
        Assert.assertEquals(
            "Ringtone URI is not correct", INITIAL_RINGTONE_URI, PreferenceUtils.getString(
                PreferenceKeys.SETTINGS_INSISTENT_NOTIFICATION_TONE, ""
            )
        )
        // check the summary has NOT been updated
        verify(view, never()).setInsistentRingtoneText(kotlinAny<String>())
    }

    @Test
    fun selectNotificationRingtone() {
        presenter.selectNotificationRingtone()

        verify(view).requestRingtone(
            ArgumentMatchers.anyInt(), kotlinEq(RingtoneManager.TYPE_NOTIFICATION),
            kotlinAny<Uri?>()
        )
    }

    @Test
    fun selectInsistentRingtone() {
        presenter.selectInsistentRingtone()

        verify(view).requestRingtone(
            ArgumentMatchers.anyInt(), kotlinEq(RingtoneManager.TYPE_ALARM),
            kotlinAny<Uri?>()
        )
    }

}