package es.usc.citius.servando.calendula.util.security

/**
 * Noop encrypt/decrypt provider to be used when encryption is not available (for testing)
 */
class NoopEncryptionProvider : EncryptionProvider {
    override fun encrypt(value: String): String = value
    override fun decrypt(value: String): String = value
}
