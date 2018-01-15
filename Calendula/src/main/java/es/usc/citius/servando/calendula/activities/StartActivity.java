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

package es.usc.citius.servando.calendula.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import es.usc.citius.servando.calendula.HomePagerActivity;
import es.usc.citius.servando.calendula.pinlock.PINManager;
import es.usc.citius.servando.calendula.pinlock.PinLockActivity;
import es.usc.citius.servando.calendula.pinlock.UnlockStateManager;


public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PINManager.isPINSet() && !UnlockStateManager.getInstance().isUnlocked()) {
            startActivity(new Intent(this, PinLockActivity.class));
        } else {
            startActivity(new Intent(this, HomePagerActivity.class));
        }
        finish();
    }
}
