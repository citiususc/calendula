/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
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

package es.usc.citius.servando.calendula.util.security

import android.content.Context
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import es.usc.citius.servando.calendula.util.PreferenceKeys


object SecuredVault {

    private const val STORE_NAME = "secure_vault"
    private const val STORE_PREFIX = "vault_pref"
    private const val SEED_KEY = "CalendulaVault"

    private val securedPrefs: SecuredPreferenceStore by lazy { SecuredPreferenceStore.getSharedInstance() }

    @JvmOverloads
    fun init(context: Context, deftSecrets: Map<PreferenceKeys, Any> = mapOf()) {

        // init secured preference store
        SecuredPreferenceStore.init(
            context.applicationContext,
            STORE_NAME,
            STORE_PREFIX,
            SEED_KEY.toByteArray(),
            DefaultRecoveryHandler()
        )

        // store default values if any
        if (deftSecrets.isNotEmpty()) {
            val editor = securedPrefs.Editor()
            for ((k, v) in deftSecrets) {
                store(k, v, editor, apply = false)
            }
            editor.apply()
        }
    }

    @JvmOverloads
    fun store(
        name: PreferenceKeys,
        value: Any,
        editor: SecuredPreferenceStore.Editor = securedPrefs.Editor(),
        apply: Boolean = true
    ) {
        when (value) {
            is String -> editor.putString(name.key(), value)
            is Int -> editor.putInt(name.key(), value)
            is Boolean -> editor.putBoolean(name.key(), value)
            else -> throw IllegalArgumentException("Unsupported value type")
        }
        if (apply) {
            editor.apply()
        }
    }

    @JvmOverloads
    fun retrieve(name: PreferenceKeys, defValue: String? = null): String? {
        return securedPrefs.getString(name.key(), defValue)
    }

    fun contains(name: PreferenceKeys): Boolean {
        return securedPrefs.contains(name.key())
    }

    fun remove(vararg prefs: PreferenceKeys) {
        val editor = securedPrefs.Editor()
        for (p in prefs) {
            editor.remove(p.key())
        }
        editor.apply()
    }

    fun preferences(): SecuredPreferenceStore {
        return securedPrefs
    }


}