package com.example.network

import androidx.compose.ui.graphics.Color

// Visual & UI Handshake DTOs
data class UiHandshakeRequest(
    val appVersion: String = "2.4.0",
    val platform: String = "Android",
    val deviceTheme: String = "DARK", // "DARK" or "LIGHT"
    val screenDensity: Float = 2.5f,
    val locale: String = "fa-IR",
    val clientPrimaryColor: String = "#6366F1",
    val clientAccentColor: String = "#F43F5E",
    val isGlassmorphismSupported: Boolean = true
)

data class RemoteThemeColors(
    val primaryHex: String = "#6366F1",
    val secondaryHex: String = "#3B82F6",
    val accentHex: String = "#F43F5E",
    val backgroundDarkHex: String = "#0B0F19",
    val surfaceDarkHex: String = "#131B2E",
    val emeraldSuccessHex: String = "#10B981",
    val orangeItPurpleHex: String = "#818CF8"
)

data class RemoteBranding(
    val appTitle: String = "کوک | Kook Market (Orange IT)",
    val brandTagline: String = "پلتفرم تجارت اجتماعی و استریم زنده ویدیو - تحت مالکیت انحصاری Orange IT",
    val announcementText: String? = "🎉 تمام حقوق مادی، معنوی و حق اثر متعلق به Orange IT می‌باشد.",
    val isAnnouncementVisible: Boolean = true,
    val logoEmoji: String = "🍊",
    val verifiedBadgeLabel: String = "تایید شده اصناف و امنیت Orange IT",
    val copyrightNotice: String = "تمام حق اثر متعلق به Orange IT است (All Rights Reserved © Orange IT)"
)

data class RemoteFeatureFlags(
    val enableLiveStreamStreamers: Boolean = true,
    val enable360VirtualTour: Boolean = true,
    val enableSmartTryOn: Boolean = true,
    val enableAiSmartTryOn: Boolean = true,
    val enableInstantEscrowPayment: Boolean = true,
    val enableGlassmorphicSurfaces: Boolean = true,
    val feedCardAspect: String = "9:16"
)

data class UiHandshakeResponse(
    val serverStatus: String = "ONLINE_SYNCED",
    val serverTime: Long = System.currentTimeMillis(),
    val configVersion: String = "v1.4.2",
    val theme: RemoteThemeColors = RemoteThemeColors(),
    val branding: RemoteBranding = RemoteBranding(),
    val features: RemoteFeatureFlags = RemoteFeatureFlags()
)

// Helper extension to safely parse hex colors
fun String.toComposeColor(defaultColor: Color): Color {
    return try {
        val cleanHex = this.removePrefix("#")
        val colorInt = when (cleanHex.length) {
            6 -> 0xFF000000.toInt() or cleanHex.toLong(16).toInt()
            8 -> cleanHex.toLong(16).toInt()
            else -> return defaultColor
        }
        Color(colorInt)
    } catch (_: Exception) {
        defaultColor
    }
}
