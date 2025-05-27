package com.mydrishti.co.`in`.activities.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "ApiClient"
    private const val BASE_URL = "http://65.0.150.52:8888/" // MyDrishti API URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d(TAG, "Making request to: ${request.url}")

            // Log request details for chart data endpoints
            val path = request.url.toString()
            if (path.contains("gauge-chart") || path.contains("bar-chart") ||
                path.contains("metric-chart") || path.contains("hourly-bar-chart")) {
                try {
                    val body = request.body
                    if (body != null) {
                        Log.d(TAG, "Chart data request body: $body")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error logging request body: ${e.message}")
                }
            }

            try {
                val response = chain.proceed(request)
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code: ${response.code}")
                } else if (path.contains("gauge-chart") || path.contains("bar-chart") ||
                    path.contains("metric-chart") || path.contains("hourly-bar-chart")) {
                    Log.d(TAG, "Chart data request successful: ${response.code}")
                }
                response
            } catch (e: Exception) {
                Log.e(TAG, "Request error: ${e.message}", e)
                throw e
            }
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(): ApiService = retrofit.create(ApiService::class.java)
}