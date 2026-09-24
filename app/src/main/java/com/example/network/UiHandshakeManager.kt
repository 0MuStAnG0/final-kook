package com.example.network

import android.content.Context
import android.util.Log
import com.example.data.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UiHandshakeManager(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun performHandshake(): ApiResult<UiHandshakeResponse> = withContext(Dispatchers.IO) {
        try {
            val metrics = context.resources.displayMetrics
            val request = UiHandshakeRequest(
                appVersion = com.example.BuildConfig.VERSION_NAME,
                platform = "Android (Jetpack Compose)",
                deviceTheme = "DARK",
                screenDensity = metrics.density,
                locale = "fa-IR",
                clientPrimaryColor = "#6366F1",
                clientAccentColor = "#F43F5E",
                isGlassmorphismSupported = true
            )

            val response = apiService.performUiHandshake(request)
            if (response.isSuccessful && response.body() != null) {
                val config = response.body()!!
                withContext(Dispatchers.Main) {
                    AppState.uiConfig = config
                    AppState.isUiSyncedWithBackend = true
                    AppState.uiSyncStatusText = "ظاهر و پیکربندی بصری با سرور همگام است (${config.configVersion})"
                }
                Log.d("UiHandshake", "Successfully synced visual UI config from backend: ${config.branding.appTitle}")
                ApiResult.Success(config)
            } else {
                val fallbackConfig = UiHandshakeResponse()
                withContext(Dispatchers.Main) {
                    AppState.uiSyncStatusText = "عدم پاسخ سرور برای تم، استفاده از پیش‌فرض (${response.code()})"
                }
                ApiResult.Error("سرور پیکربندی بصری را ارسال نکرد (${response.code()})")
            }
        } catch (e: java.net.UnknownHostException) {
            withContext(Dispatchers.Main) {
                AppState.uiSyncStatusText = "سرور بک‌اند آفلاین است (استفاده از تم پیش‌فرض)"
            }
            Log.d("UiHandshake", "Server host not reachable yet: ${e.message}")
            ApiResult.Error("آدرس سرور هنوز در دسترس نیست. می‌توانید آدرس سرور را در بخش تنظیمات وارد کنید.")
        } catch (e: java.net.ConnectException) {
            withContext(Dispatchers.Main) {
                AppState.uiSyncStatusText = "عدم اتصال به سرور بک‌اند (استفاده از تم پیش‌فرض)"
            }
            Log.d("UiHandshake", "Connection refused: ${e.message}")
            ApiResult.Error("امکان اتصال به سرور وجود ندارد.")
        } catch (e: java.net.SocketTimeoutException) {
            withContext(Dispatchers.Main) {
                AppState.uiSyncStatusText = "پایان مهلت اتصال به سرور"
            }
            Log.d("UiHandshake", "Socket timeout: ${e.message}")
            ApiResult.Error("مهلت اتصال به سرور به پایان رسید.")
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                AppState.uiSyncStatusText = "استفاده از تم محلی آفلاین"
            }
            Log.d("UiHandshake", "Handshake notice: ${e.message}")
            ApiResult.Error("خطا در همگام‌سازی بصری با سرور: ${e.localizedMessage}")
        }
    }
}
