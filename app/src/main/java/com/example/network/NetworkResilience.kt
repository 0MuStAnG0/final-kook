package com.example.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Retries idempotent-safe and transient connection failures (common on Iranian mobile networks).
 */
class RetryInterceptor(
    private val maxRetries: Int = 2
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastError: IOException? = null
        repeat(maxRetries) { attempt ->
            try {
                // Only retry transport failures — never replay successful HTTP round-trips
                return chain.proceed(request)
            } catch (e: IOException) {
                lastError = e
                if (attempt == maxRetries - 1 || !isTransient(e)) throw e
            }
            try {
                Thread.sleep(300L * (attempt + 1))
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        throw lastError ?: IOException("تلاش اتصال ناموفق بود")
    }

    private fun isTransient(e: IOException): Boolean {
        return e is SocketTimeoutException ||
            e is ConnectException ||
            e is UnknownHostException ||
            e.message?.contains("failed to connect", ignoreCase = true) == true ||
            e.message?.contains("timeout", ignoreCase = true) == true ||
            e.message?.contains("Connection reset", ignoreCase = true) == true ||
            e.message?.contains("Software caused connection abort", ignoreCase = true) == true
    }
}

object NetworkErrors {
    fun toUserMessage(e: Throwable, action: String = "ارتباط با سرور"): String {
        val raw = e.message.orEmpty()
        return when {
            e is UnknownHostException || raw.contains("Unable to resolve host", true) ->
                "آدرس سرور پیدا نشد. اینترنت یا DNS را بررسی کنید."
            raw.contains("failed to connect", true) || e is ConnectException ->
                "دسترسی به سرور کوک (پورت ۴۴۳) برقرار نشد. معمولاً به‌خاطر قطعی موقت اینترنت یا محدودیت اپراتور است. Wi‑Fi را خاموش/روشن کنید یا با اینترنت دیگری دوباره تلاش کنید."
            e is SocketTimeoutException || (raw.contains("timeout", true) || (raw.contains("after ", true) && raw.contains("ms"))) ->
                "مهلت اتصال به سرور تمام شد (شبکه کند). چند لحظه بعد دوباره تلاش کنید یا از Wi‑Fi پایدار استفاده کنید."
            raw.contains("SSL", true) || raw.contains("Certificate", true) || raw.contains("Trust", true) ->
                "خطای امنیتی TLS در اتصال به سرور. ساعت دستگاه را بررسی کنید."
            else ->
                "$action ناموفق بود. لطفاً اتصال اینترنت را چک کرده و دوباره تلاش کنید."
        }
    }
}

fun OkHttpClient.Builder.applyKookTimeouts(): OkHttpClient.Builder {
    connectTimeout(8, TimeUnit.SECONDS)
    readTimeout(10, TimeUnit.SECONDS)
    writeTimeout(10, TimeUnit.SECONDS)
    callTimeout(15, TimeUnit.SECONDS)
    retryOnConnectionFailure(true)
    return this
}
