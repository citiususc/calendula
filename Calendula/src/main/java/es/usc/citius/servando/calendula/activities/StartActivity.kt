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

package es.usc.citius.servando.calendula.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle

import es.usc.citius.servando.calendula.HomePagerActivity
import es.usc.citius.servando.calendula.pinlock.PINManager
import es.usc.citius.servando.calendula.pinlock.PinLockActivity
import es.usc.citius.servando.calendula.pinlock.UnlockStateManager


class StartActivity : Activity() {

    companion object {
        const val EXTRA_RETURN_TO_PREVIOUS = "StartActivity.extras.return_to_previous"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyUnlockAndLaunch()
    }

    private fun verifyUnlockAndLaunch() {
        if (PINManager.isPINSet() && !UnlockStateManager.getInstance().isUnlocked) {
            val i = Intent(this, PinLockActivity::class.java)
            i.action = PinLockActivity.ACTION_VERIFY_PIN
            startActivityForResult(i, PinLockActivity.REQUEST_VERIFY)
        } else {
            val returnToPrevious  = intent.getBooleanExtra(EXTRA_RETURN_TO_PREVIOUS,false)
            if (!returnToPrevious) {
                // if "return to previous" is specified, just finish this activity to go back in the stack
                startActivity(Intent(this, HomePagerActivity::class.java))
            }
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinLockActivity.REQUEST_VERIFY) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            } else {
                verifyUnlockAndLaunch()
            }
        }
    }
}
