package es.usc.citius.servando.calendula.util.security

interface EncryptionProvider {
    fun encrypt(value: String): String
    fun decrypt(value: String): String
}