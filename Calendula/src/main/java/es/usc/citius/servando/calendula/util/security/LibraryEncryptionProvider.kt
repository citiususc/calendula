package es.usc.citius.servando.calendula.util.security

import android.util.Base64
import devliving.online.securedpreferencestore.EncryptionManager
import es.usc.citius.servando.calendula.util.LogUtil

class LibraryEncryptionProvider(private val encryptionManager: EncryptionManager) :
    EncryptionProvider {

    companion object {
        private const val TAG = "LibraryEncryptionProv"
        private const val DELIMITER = "]"
    }

    override fun encrypt(value: String): String {
        if(value.isEmpty()){
            throw IllegalArgumentException("Can't encrypt empty string!")
        }
        val data = value.toByteArray(charset("UTF-8"))
        val secret = encryptionManager.encrypt(data)
        return encodeEncryptedData(secret)
    }

    override fun decrypt(value: String): String {
        if(value.isEmpty()){
            throw IllegalArgumentException("Can't decrypt empty string!")
        }
        try {
            val secret = decodeEncryptedText(value)
            val data = encryptionManager.decrypt(secret)
            return data.toString(charset("UTF-8"))
        } catch (e: Exception) {
            LogUtil.d(TAG, "Error decrypting property", e)
            throw e
        }
    }

    private fun base64Encode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }

    private fun base64Decode(text: String): ByteArray {
        return Base64.decode(text, Base64.NO_WRAP)
    }

    private fun encodeEncryptedData(data: EncryptionManager.EncryptedData): String {
        return if (data.mac != null) {
            base64Encode(data.iv) + DELIMITER + base64Encode(data.encryptedData) + DELIMITER + base64Encode(
                data.mac
            )
        } else {
            base64Encode(data.iv) + DELIMITER + base64Encode(data.encryptedData)
        }
    }

    private fun decodeEncryptedText(text: String): EncryptionManager.EncryptedData {
        val result = EncryptionManager.EncryptedData()
        val parts = text.split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        result.iv = base64Decode(parts[0])
        result.encryptedData = base64Decode(parts[1])

        if (parts.size > 2) {
            result.mac = base64Decode(parts[2])
        }

        return result
    }
}
