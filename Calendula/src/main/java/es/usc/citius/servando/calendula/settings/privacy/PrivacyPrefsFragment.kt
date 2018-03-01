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

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.pinlock.PinLockActivity
import es.usc.citius.servando.calendula.pinlock.fingerprint.FingerprintHelper
import es.usc.citius.servando.calendula.settings.CalendulaPrefsFragment
import es.usc.citius.servando.calendula.util.IconUtils
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceKeys


/**
 * Instantiated via reflection, don't delete!
 */
class PrivacyPrefsFragment :
    CalendulaPrefsFragment<PrivacyPrefsContract.View, PrivacyPrefsContract.Presenter>(),
    PrivacyPrefsContract.View {


    companion object {
        private const val TAG = "PrivacyPrefsFragment"
    }


    override val presenter: PrivacyPrefsContract.Presenter by lazy {
        PrivacyPrefsPresenter(
            FingerprintHelper(context)
        )
    }
    override val fragmentTitle: Int = R.string.pref_header_privacy


    private val pinPref: Preference by lazy { findPreference(PreferenceKeys.UNLOCK_PIN.key()) }
    private val fingerprintPref: SwitchPreference by lazy { findPreference(PreferenceKeys.FINGERPRINT_ENABLED.key()) as SwitchPreference }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        LogUtil.d(TAG, "onCreatePreferences called")
        addPreferencesFromResource(R.xml.pref_privacy)

        pinPref.setOnPreferenceClickListener {
            presenter.onClickPINPref()
            true
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LogUtil.d(TAG, "onActivityResult() called")
        presenter.onResult(requestCode, resultCode, data)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        //noop
    }

    override fun recordPIN() {
        val i = Intent(activity, PinLockActivity::class.java)
        startActivityForResult(i, PinLockActivity.REQUEST_PIN)
    }

    override fun showPINOptions() {
        MaterialStyledDialog.Builder(context)
            .setTitle(getString(R.string.pin_actions_dialog_title))
            .setDescription(R.string.pin_actions_dialog_message)
            .setHeaderColor(R.color.android_green)
            .setStyle(Style.HEADER_WITH_ICON)
            .withDialogAnimation(true)
            .setIcon(
                IconUtils.icon(
                    context,
                    GoogleMaterial.Icon.gmd_key,
                    R.color.white,
                    100
                )
            )
            .setPositiveText(R.string.pin_actions_dialog_delete)
            .setNegativeText(R.string.pin_actions_dialog_modify)
            .setNeutralText(R.string.pin_actions_dialog_cancel)
            .onPositive { dialog, _ ->
                presenter.onClickDeletePIN()
                dialog.dismiss()
            }
            .onNegative { dialog, _ ->
                presenter.onClickModifyPIN()
                dialog.dismiss()
            }
            .onNeutral { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun showConfirmDeletePinChoice() {
        MaterialStyledDialog.Builder(context)
            .setTitle(getString(R.string.pin_delete_dialog_title))
            .setDescription(R.string.pin_delete_dialog_message)
            .setHeaderColor(R.color.android_red_dark)
            .setStyle(Style.HEADER_WITH_ICON)
            .withDialogAnimation(true)
            .setIcon(
                IconUtils.icon(
                    context,
                    GoogleMaterial.Icon.gmd_key,
                    R.color.white,
                    100
                )
            )
            .setPositiveText(R.string.pin_actions_dialog_delete)
            .setNegativeText(R.string.pin_actions_dialog_cancel)
            .onPositive { dialog, _ ->
                presenter.confirmDeletePIN()
                dialog.dismiss()
            }
            .onNegative { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun setPINPrefText(pinPrefText: Int) {
        pinPref.setSummary(pinPrefText)
    }

    override fun setFingerprintPrefEnabled(enabled: Boolean) {
        fingerprintPref.isEnabled = enabled
    }

    override fun showEnableFingerprintDialog() {
        // noop
    }


}