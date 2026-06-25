package ni.edu.uam.raccooncash.data.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

class PinSecurityStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasPin(): Boolean = prefs.getString(KEY_PIN_HASH, null) != null

    fun isPinEnabled(): Boolean = prefs.getBoolean(KEY_PIN_ENABLED, false) && hasPin()

    fun setPin(pin: String) {
        prefs.edit()
            .putString(KEY_PIN_HASH, hashPin(pin, getOrCreateSalt()))
            .apply()
    }

    fun setPinEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_PIN_ENABLED, enabled && hasPin())
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return savedHash == hashPin(pin, salt)
    }

    private fun getOrCreateSalt(): String {
        prefs.getString(KEY_PIN_SALT, null)?.let { return it }

        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        prefs.edit().putString(KEY_PIN_SALT, salt).apply()
        return salt
    }

    private fun hashPin(pin: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$salt:$pin".toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    private companion object {
        const val PREFS_NAME = "pin_security"
        const val KEY_PIN_ENABLED = "pin_enabled"
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
    }
}
