package es.usc.citius.servando.calendula.util.security

import android.content.Context
import android.content.SharedPreferences
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceUtils
import java.lang.ref.WeakReference

/**
 * Abstracts access to encryption features: makes SecuredVault behave as normal prefs, and encryption noop, if it's not initialized (such as for testing)
 */
class SecurityProvider {

    companion object {

        private const val TAG = "SecurityProvider"

        private const val STORE_NAME = "secure_vault"
        private const val STORE_PREFIX = "vault_pref"
        private const val SEED_KEY = "CalendulaVault"

        private var securedPrefStore: SecuredPreferenceStore? = null
        private var encryptionProvider: WeakReference<EncryptionProvider>? = null

        @JvmStatic
        fun init(ctx: Context) {
            SecuredPreferenceStore.init(
                ctx.applicationContext,
                STORE_NAME,
                STORE_PREFIX,
                SEED_KEY.toByteArray(),
                DefaultRecoveryHandler()
            )
            securedPrefStore = SecuredPreferenceStore.getSharedInstance()
            encryptionProvider = null
        }

        fun isAvailable(): Boolean = securedPrefStore != null

        @JvmStatic
        fun getEncryptionProvider(): EncryptionProvider {

            if (encryptionProvider == null) {
                encryptionProvider = if (isAvailable()) {
                    WeakReference(LibraryEncryptionProvider(securedPrefStore!!.encryptionManager))
                } else {
                    LogUtil.w(
                        TAG,
                        "getEncryptionProvider: SecurityProvider not initialized! Not using encryption. Is this what you want?"
                    )
                    WeakReference(NoopEncryptionProvider())
                }
            }

            return encryptionProvider!!.get()!!
        }

        fun getPreferences(): SharedPreferences {
            if (isAvailable()) {
                return securedPrefStore as SharedPreferences
            } else {
                LogUtil.w(
                    TAG,
                    "getPreferences: SecurityProvider not initialized! Not using encryption. Is this what you want?"
                )
                return PreferenceUtils.instance().preferences()
            }
        }

    }


}