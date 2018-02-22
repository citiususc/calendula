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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import android.support.v4.content.ContextCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.Toast
import es.usc.citius.servando.calendula.CalendulaActivity
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.settings.database.DatabasePrefsFragment
import es.usc.citius.servando.calendula.util.LogUtil


class CalendulaSettingsActivity : CalendulaActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat?,
        pref: Preference?
    ): Boolean {
        try {
            pref?.let {
                LogUtil.d(TAG, "onPreferenceStartFragment: pref fragment class is ${pref.fragment}")
                val transaction = supportFragmentManager.beginTransaction()
                val fragment = Class.forName(pref.fragment).newInstance() as Fragment
                transaction.setTransition(TRANSIT_FRAGMENT_FADE)
                transaction.replace(R.id.content_layout, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "onPreferenceStartFragment: ", e)
            Toast.makeText(this, R.string.message_generic_error, Toast.LENGTH_SHORT).show()
        }
        return true
    }

    companion object {
        private const val TAG = "CalendulaSettingsActivity"
        const val EXTRA_SHOW_DB_DIALOG = "show_db_dialog" //TODO actually do something with this
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupStatusBar(ContextCompat.getColor(this, R.color.dark_grey_home))

        setupToolbar(
            getString(R.string.title_activity_settings),
            ContextCompat.getColor(this, R.color.dark_grey_home)
        )

        if (!processIntent()) {
            supportFragmentManager.beginTransaction()
                .add(R.id.content_layout, MainPrefsFragment()).commit()
        }


    }

    /**
     * @return `true` if the initialization of the Activity should be interrupted (not load the main fragment)
     */
    private fun processIntent(): Boolean {
        if (intent.getBooleanExtra(EXTRA_SHOW_DB_DIALOG, false)) {
            supportFragmentManager.beginTransaction()
                .add(R.id.content_layout, DatabasePrefsFragment()).commit()
            return true
        }
        return false
    }

}