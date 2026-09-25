package com.example.swiftyproteins.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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

    fun saveUser(username: String, passwordHash: String) {
        sharedPreferences.edit()
            .putString("username", username)
            .putString("password_hash", passwordHash)
            .apply()
    }

    fun getUsername(): String? = sharedPreferences.getString("username", null)

    fun getPasswordHash(): String? = sharedPreferences.getString("password_hash", null)

    fun hasUser(): Boolean = getUsername() != null && getPasswordHash() != null
}
