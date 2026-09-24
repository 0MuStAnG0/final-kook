package com.example.network

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        tokenManager.getAccessToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}

class TokenAuthenticator(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val baseUrl: String
) : Authenticator {

    @Volatile
    private var refreshClient: OkHttpClient? = null

    private fun refreshHttpClient(): OkHttpClient {
        return refreshClient ?: synchronized(this) {
            refreshClient ?: OkHttpClient.Builder()
                .applyKookTimeouts()
                .retryOnConnectionFailure(true)
                .addInterceptor(RetryInterceptor(maxRetries = 2))
                .build()
                .also { refreshClient = it }
        }
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loops on refresh failures
        if (responseCount(response) >= 2) return null

        val refreshTokenStr = tokenManager.getRefreshToken() ?: return null

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(refreshHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val authService = retrofit.create(OrangeItApiService::class.java)

        return runBlocking {
            try {
                val retrofitResponse = authService.refreshToken(RefreshRequest(refreshTokenStr))

                if (retrofitResponse.isSuccessful) {
                    val newTokens = retrofitResponse.body()
                    if (newTokens != null) {
                        tokenManager.saveAccessToken(newTokens.accessToken)
                        tokenManager.saveRefreshToken(newTokens.refreshToken)

                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                    } else {
                        null
                    }
                } else {
                    tokenManager.clearTokens()
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
