package com.example.swiftyproteins.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

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
        fun hashPassword(password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
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
        val passwordHash = hashPassword(password)
        val users = getAllUsernames().toMutableSet()
        users.add(normalized)

        sharedPreferences.edit()
            .putString("pwd_hash_$normalized", passwordHash)
            .putStringSet("all_users", users)
            .putString("last_user", normalized)
            .apply()
        return true
    }

    fun validateCredentials(username: String, password: String): Boolean {
        val normalized = username.lowercase().trim()
        val storedHash = sharedPreferences.getString("pwd_hash_$normalized", null) ?: return false
        val isValid = hashPassword(password) == storedHash
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
