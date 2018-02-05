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

package es.usc.citius.servando.calendula.settings

import android.content.SharedPreferences
import android.support.v7.preference.PreferenceFragmentCompat
import es.usc.citius.servando.calendula.util.PreferenceUtils

/**
 * A [PreferenceFragmentCompat] that registers and unregisters itself as a [SharedPreferences.OnSharedPreferenceChangeListener]
 */
abstract class CalendulaPrefsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onResume() {
        super.onResume()
        PreferenceUtils.instance().preferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceUtils.instance().preferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}