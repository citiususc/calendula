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
import android.content.SharedPreferences
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.EncryptionManager
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import es.usc.citius.servando.calendula.util.GsonUtil
import es.usc.citius.servando.calendula.util.LogUtil

/**
 * Allows storage of shared preferences encrypted using a key
 * generated and stored using the android keystore
 */
class SecuredVault private constructor(impl: SecuredPreferenceStore) : SharedPreferences by impl {

    /**
     * This needs to be implemented as a companion object:
     * - We want to compose this class with SecuredPreferenceStore, in order expose the same interface
     * - We can't use an object instead of a class because SecuredPreferenceStore has constructor with params
     * - We want to use this class like a singleton anyway
     */
    companion object {

        private const val TAG = "SecuredVault"
        private const val STORE_NAME = "secure_vault"
        private const val STORE_PREFIX = "vault_pref"
        private const val SEED_KEY = "CalendulaVault"

        private var instance: SecuredVault? = null

        @JvmStatic
        fun instance(): SecuredVault {
            if (instance == null) {
                throw  IllegalStateException("Not initialized")
            } else {
                return instance!!
            }
        }

        @JvmStatic
        fun init(ctx: Context) {
            SecuredPreferenceStore.init(
                ctx.applicationContext,
                STORE_NAME,
                STORE_PREFIX,
                SEED_KEY.toByteArray(),
                DefaultRecoveryHandler()
            )
            instance = SecuredVault(SecuredPreferenceStore.getSharedInstance())
        }

        @JvmStatic
        fun encrypt(value: String?): String? {
            value?.let {
                val data = value.toByteArray(charset("UTF-8"))
                val secret =
                    SecuredPreferenceStore.getSharedInstance().encryptionManager.encrypt(data)
                return GsonUtil.get().toJson(secret)
            }
            return null
        }

        @JvmStatic
        fun decrypt(value: String?): String? {
            if (value != null && value.isNotEmpty()) {
                try {
                    val secret =
                        GsonUtil.get().fromJson(value, EncryptionManager.EncryptedData::class.java)
                    val data =
                        SecuredPreferenceStore.getSharedInstance().encryptionManager.decrypt(secret)
                    return data.toString(charset("UTF-8"))
                } catch (e: Exception) {
                    LogUtil.d(TAG, "Error decrypting property", e)
                }
            }
            return ""
        }
    }


}
