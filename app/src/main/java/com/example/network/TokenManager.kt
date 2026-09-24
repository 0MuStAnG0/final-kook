package com.example.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val securePrefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "orange_it_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        context.getSharedPreferences("orange_it_secure_prefs_fallback", Context.MODE_PRIVATE)
    }

    fun saveAccessToken(token: String) {
        securePrefs.edit().putString("ACCESS_TOKEN", token).apply()
    }

    fun getAccessToken(): String? {
        return securePrefs.getString("ACCESS_TOKEN", null)
    }

    fun saveRefreshToken(token: String) {
        securePrefs.edit().putString("REFRESH_TOKEN", token).apply()
    }

    fun getRefreshToken(): String? {
        return securePrefs.getString("REFRESH_TOKEN", null)
    }

    fun clearTokens() {
        securePrefs.edit().remove("ACCESS_TOKEN").remove("REFRESH_TOKEN").apply()
    }

    fun hasToken(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }
}
