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

package es.usc.citius.servando.calendula.settings.privacy

import android.app.Activity
import android.content.Intent
import android.os.Build
import es.usc.citius.servando.calendula.BuildConfig
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.kotlinEq
import es.usc.citius.servando.calendula.pinlock.PinLockActivity
import es.usc.citius.servando.calendula.pinlock.fingerprint.FingerprintHelper
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = [Build.VERSION_CODES.M])
class PrivacyPrefsPresenterTest {

    companion object {
        private const val FAKE_PIN = "1234"
        private const val FAKE_PIN_HASH = "3a2b4c"
    }


    @Mock
    private lateinit var view: PrivacyPrefsContract.View
    @Mock
    private lateinit var fpHelper: FingerprintHelper

    private lateinit var presenter: PrivacyPrefsContract.Presenter

    @Before
    fun setUp() {

        MockitoAnnotations.initMocks(this)
        // setup fpHelper mock
        `when`(fpHelper.canUseFingerPrint()).thenReturn(true)


        //reset pin prefs
        PreferenceUtils.edit()
            .putString(PreferenceKeys.UNLOCK_PIN_HASH.key(), null)
            .putString(PreferenceKeys.UNLOCK_PIN_SALT.key(), null)
            .putString(PreferenceKeys.UNLOCK_PIN.key(), null)
            .commit()


        presenter = PrivacyPrefsPresenter(fpHelper)
        presenter.attachView(view)
    }

    @Test
    fun start() {
        presenter.start()
        // when started without pin, PIN summary should be "no pin"

        verify(view).setPINPrefText(kotlinEq(R.string.pref_summary_pin_lock_unset))
    }


    @Test
    fun startWithPinSet() {

        PreferenceUtils.edit()
            .putString(PreferenceKeys.UNLOCK_PIN_HASH.key(), FAKE_PIN_HASH)
            .commit()

        presenter.start()

        // when started PIN, PIN summary should be "PIN set", and fingerprint pref should be enabled
        verify(view).setPINPrefText(kotlinEq(R.string.pref_summary_pin_lock_set))
        verify(view).setPINDependentPrefsEnabled(kotlinEq(true))
    }

    @Test
    fun onResultValid() {
        presenter.onResult(
            PinLockActivity.REQUEST_PIN,
            Activity.RESULT_OK,
            Intent().putExtra(PinLockActivity.EXTRA_NEW_PIN, FAKE_PIN)
        )

        verify(view).setPINPrefText(kotlinEq(R.string.pref_summary_pin_lock_set))
        verify(view).setPINDependentPrefsEnabled(kotlinEq(true))
        verify(view).showEnableFingerprintDialog()
    }

    @Test
    fun onResultInvalid() {
        presenter.onResult(
            PinLockActivity.REQUEST_PIN,
            Activity.RESULT_OK,
            null
        )

        verify(view, never()).setPINPrefText(Mockito.anyInt())
        verify(view, never()).setPINDependentPrefsEnabled(Mockito.anyBoolean())
        verify(view, never()).showEnableFingerprintDialog()
    }

    @Test
    fun onResultValidDelete() {
        presenter.onResult(
            PrivacyPrefsPresenter.REQUEST_DELETE,
            Activity.RESULT_OK,
            Intent().putExtra(PinLockActivity.EXTRA_VERIFY_PIN_RESULT, true)
        )

        verify(view).setPINPrefText(kotlinEq(R.string.pref_summary_pin_lock_unset))
        verify(view).setPINDependentPrefsEnabled(kotlinEq(false))
    }


    @Test
    fun onResultValidModify() {
        presenter.onResult(
            PrivacyPrefsPresenter.REQUEST_MODIFY,
            Activity.RESULT_OK,
            Intent().putExtra(PinLockActivity.EXTRA_VERIFY_PIN_RESULT, true)
        )

        verify(view).recordPIN()
    }


    @Test
    fun onClickPINPrefWithPinSet() {

        PreferenceUtils.edit()
            .putString(PreferenceKeys.UNLOCK_PIN_HASH.key(), FAKE_PIN_HASH)
            .commit()

        presenter.onClickPINPref()

        verify(view).showPINOptions()
    }

    @Test
    fun onClickPINPrefWithPinUnset() {
        presenter.onClickPINPref()

        verify(view).recordPIN()
    }

    @Test
    fun onClickDeletePIN() {
        presenter.onClickDeletePIN()

        verify(view).showConfirmDeletePinChoice()
    }

    @Test
    fun confirmDeletePIN() {
        presenter.confirmDeletePIN()

        verify(view).verifyPIN(ArgumentMatchers.anyInt())
    }

    @Test
    fun onClickModifyPIN() {
        presenter.onClickModifyPIN()

        verify(view).verifyPIN(ArgumentMatchers.anyInt())
    }

}