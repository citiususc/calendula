package es.usc.citius.servando.calendula.util.security

import devliving.online.securedpreferencestore.EncryptionManager
import es.usc.citius.servando.calendula.util.GsonUtil
import es.usc.citius.servando.calendula.util.LogUtil

class LibraryEncryptionProvider(private val encryptionManager: EncryptionManager) :
    EncryptionProvider {

    companion object {
        private const val TAG = "LibraryEncryptionProv"
    }

    override fun encrypt(value: String): String {
        val data = value.toByteArray(charset("UTF-8"))
        val secret = encryptionManager.encrypt(data)
        return GsonUtil.get().toJson(secret)
    }

    override fun decrypt(value: String): String {
        try {
            val secret =
                GsonUtil.get().fromJson(value, EncryptionManager.EncryptedData::class.java)
            val data = encryptionManager.decrypt(secret)
            return data.toString(charset("UTF-8"))
        } catch (e: Exception) {
            LogUtil.d(TAG, "Error decrypting property", e)
            throw e
        }
    }
}
