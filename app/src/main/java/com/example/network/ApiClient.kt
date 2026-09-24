package com.example.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var sharedOkHttp: OkHttpClient? = null

    fun resetClient() {
        retrofit = null
        sharedOkHttp = null
    }

    fun getClient(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(context).also { retrofit = it }
        }
    }

    fun getApiService(context: Context): OrangeItApiService {
        return getClient(context).create(OrangeItApiService::class.java)
    }

    fun getOkHttp(context: Context): OkHttpClient {
        return sharedOkHttp ?: synchronized(this) {
            sharedOkHttp ?: buildOkHttp(context).also { sharedOkHttp = it }
        }
    }

    private fun buildOkHttp(context: Context): OkHttpClient {
        val tokenManager = TokenManager(context)
        val serverConfig = ServerConfigManager(context)
        val baseUrl = serverConfig.getBaseUrl()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (com.example.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .applyKookTimeouts()
            .retryOnConnectionFailure(true)
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(RetryInterceptor(maxRetries = 2))
            .authenticator(TokenAuthenticator(context, tokenManager, baseUrl))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val serverConfig = ServerConfigManager(context)
        val baseUrl = serverConfig.getBaseUrl()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getOkHttp(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
