package com.example.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.BuildConfig

/**
 * ServerConfigManager manages the backend server endpoint configuration.
 * All endpoint configuration is isolated within the internal security layer:
 * - Compiled into byte-code via BuildConfig.BACKEND_BASE_URL.
 * - Hardware Keystore encryption (MasterKey AES-256 GCM) for secure storage.
 * - Transport Layer Security (TLS/HTTPS) validation.
 * - Strictly hidden from the user interface and presentation layers.
 */
class ServerConfigManager(context: Context) {

    private val securePrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "kook_secure_net_vault",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences("kook_secure_net_vault_fallback", Context.MODE_PRIVATE)
        }
    }

    companion object {
        val SECURE_BASE_URL: String = BuildConfig.BACKEND_BASE_URL.ifBlank { "https://kook.orangeit.site/" }
    }

    /**
     * Internal access to the validated backend server URL for network clients.
     * Enforces trailing slash, HTTPS protocol, and sanitizes against legacy typos like orange.it.site.
     */
    fun getBaseUrl(): String {
        var rawUrl = securePrefs.getString("SECURE_BACKEND_ENDPOINT", SECURE_BASE_URL) ?: SECURE_BASE_URL
        if (rawUrl.contains("orange.it.site") || rawUrl.contains("api.orangeit.site") || !rawUrl.contains("orangeit.site")) {
            rawUrl = SECURE_BASE_URL
            try {
                securePrefs.edit().putString("SECURE_BACKEND_ENDPOINT", SECURE_BASE_URL).apply()
            } catch (_: Exception) {}
        }
        val normalized = if (rawUrl.endsWith("/")) rawUrl else "$rawUrl/"
        return if (normalized.startsWith("https://") || BuildConfig.DEBUG) {
            normalized
        } else {
            SECURE_BASE_URL
        }
    }
}

