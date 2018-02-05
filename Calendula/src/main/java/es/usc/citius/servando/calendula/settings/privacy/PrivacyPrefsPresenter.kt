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
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.pinlock.PINManager
import es.usc.citius.servando.calendula.pinlock.PinLockActivity
import es.usc.citius.servando.calendula.pinlock.fingerprint.FingerprintHelper
import es.usc.citius.servando.calendula.util.LogUtil


/**
 * Created by alvaro.brey.vilas on 5/02/18.
 */
class PrivacyPrefsPresenter(val view: PrivacyPrefsContract.View, val fpHelper: FingerprintHelper) :
    PrivacyPrefsContract.Presenter {


    companion object {
        private const val TAG = "PrivacyPrefsPresenter"
    }

    init {
        view.presenter = this
    }

    override fun start() {
        if (PINManager.isPINSet()) {
            view.setPINPrefText(R.string.pref_summary_pin_lock_set)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && fpHelper.canUseFingerPrint()) {
                view.setFingerprintPrefEnabled(true)
            }
        } else {
            view.setPINPrefText(R.string.pref_summary_pin_lock_unset)
        }
    }

    override fun onResult(request: Int, result: Int, data: Intent?) {
        LogUtil.d(TAG, "onResult() called with request=$request, result=$result, data=$data")
        if (request == PinLockActivity.REQUEST_PIN && result == Activity.RESULT_OK && data != null) {
            val pin = data.getStringExtra(PinLockActivity.EXTRA_PIN)
            val pinManagerResult = PINManager.savePIN(pin)
            if (pinManagerResult) {
                view.setPINPrefText(R.string.pref_summary_pin_lock_set)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && fpHelper.canUseFingerPrint()) {
                    view.setFingerprintPrefEnabled(true)
                    view.showEnableFingerprintDialog()
                }
            }
        } else {
            LogUtil.w(TAG, "onResult: invalid result or missing data")
        }

    }

    override fun onClickPINPref() {
        if (PINManager.isPINSet()) {
            view.showPINOptions()
        } else {
            view.recordPIN()
        }
    }

    override fun onClickDeletePIN() {
        view.showConfirmDeletePinChoice()
    }

    override fun confirmDeletePIN() {
        LogUtil.d(TAG, "confirmDeletePIN: deleting PIN")
        PINManager.clearPIN()
        view.setPINPrefText(R.string.pref_summary_pin_lock_unset)
        view.setFingerprintPrefEnabled(false)
    }

    override fun onClickModifyPIN() {
        view.recordPIN()
    }


}