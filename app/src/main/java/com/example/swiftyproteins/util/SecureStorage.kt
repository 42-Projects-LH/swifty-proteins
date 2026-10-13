package com.example.swiftyproteins.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

class SecureStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val SALT_LENGTH_BYTES = 16

        /**
         * Generates a 16-byte (128-bit) cryptographically secure pseudo-random salt.
         */
        fun generateSalt(): String {
            val random = SecureRandom()
            val salt = ByteArray(SALT_LENGTH_BYTES)
            random.nextBytes(salt)
            return salt.joinToString("") { "%02x".format(it) }
        }

        /**
         * Computes a cryptographic SHA-256 hash using salt + password.
         */
        fun hashPassword(password: String, saltHex: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(saltHex.toByteArray(Charsets.UTF_8))
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }

    fun isUserRegistered(username: String): Boolean {
        val normalized = username.lowercase().trim()
        return sharedPreferences.getString("pwd_hash_$normalized", null) != null
    }

    fun registerUser(username: String, password: String): Boolean {
        val normalized = username.lowercase().trim()
        if (isUserRegistered(normalized)) {
            return false
        }
        val salt = generateSalt()
        val passwordHash = hashPassword(password, salt)
        val storedRecord = "$salt:$passwordHash"

        val users = getAllUsernames().toMutableSet()
        users.add(normalized)

        sharedPreferences.edit()
            .putString("pwd_hash_$normalized", storedRecord)
            .putStringSet("all_users", users)
            .putString("last_user", normalized)
            .apply()
        return true
    }

    fun validateCredentials(username: String, password: String): Boolean {
        val normalized = username.lowercase().trim()
        val storedRecord = sharedPreferences.getString("pwd_hash_$normalized", null) ?: return false

        val isValid = if (storedRecord.contains(":")) {
            val parts = storedRecord.split(":", limit = 2)
            val salt = parts[0]
            val expectedHash = parts[1]
            hashPassword(password, salt) == expectedHash
        } else {
            // Backward compatibility with legacy unsalted hashes:
            // Verify against legacy SHA-256, and if valid, seamlessly upgrade record with fresh salt.
            val legacyDigest = MessageDigest.getInstance("SHA-256")
            val legacyHash = legacyDigest.digest(password.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            if (legacyHash == storedRecord) {
                val newSalt = generateSalt()
                val newHash = hashPassword(password, newSalt)
                sharedPreferences.edit()
                    .putString("pwd_hash_$normalized", "$newSalt:$newHash")
                    .apply()
                true
            } else {
                false
            }
        }

        if (isValid) {
            setLastUser(normalized)
        }
        return isValid
    }

    fun getAllUsernames(): Set<String> {
        val set = sharedPreferences.getStringSet("all_users", null)
        if (set != null) return set

        // Migrate legacy single user if present
        val legacyUser = sharedPreferences.getString("username", null)?.lowercase()?.trim()
        val legacyHash = sharedPreferences.getString("password_hash", null)
        if (!legacyUser.isNullOrEmpty() && !legacyHash.isNullOrEmpty()) {
            val initial = setOf(legacyUser)
            sharedPreferences.edit()
                .putStringSet("all_users", initial)
                .putString("pwd_hash_$legacyUser", legacyHash)
                .putString("last_user", legacyUser)
                .apply()
            return initial
        }
        return emptySet()
    }

    fun hasUser(): Boolean = getAllUsernames().isNotEmpty()

    fun getLastUser(): String? = sharedPreferences.getString("last_user", null)

    fun setLastUser(username: String) {
        val normalized = username.lowercase().trim()
        sharedPreferences.edit().putString("last_user", normalized).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
