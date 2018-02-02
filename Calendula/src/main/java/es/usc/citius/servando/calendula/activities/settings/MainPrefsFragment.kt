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

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.util.LogUtil

/**
 * Created by alvaro.brey.vilas on 1/02/18.
 */
class MainPrefsFragment : PreferenceFragmentCompat() {

    companion object {
        private const val TAG = "MainPrefsFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        LogUtil.d(TAG, "onCreatePreferences called")
        setPreferencesFromResource(R.xml.pref_main, rootKey)
    }

}