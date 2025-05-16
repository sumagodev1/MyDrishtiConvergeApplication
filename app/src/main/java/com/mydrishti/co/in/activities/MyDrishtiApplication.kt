package com.mydrishti.co.`in`.activities

import android.app.Application
import android.content.Context
import android.util.Log
import com.mydrishti.co.`in`.activities.utils.SessionManager

/**
 * Custom Application class for MyDrishti app
 * Handles initialization of app-wide singletons and services
 */
class MyDrishtiApplication : Application() {

    companion object {
        private const val TAG = "MyDrishtiApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate started")

        try {
            // Initialize SessionManager with application context
            val sessionManager = SessionManager.getInstance(applicationContext)
            Log.d(TAG, "SessionManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SessionManager: ${e.message}", e)
            // Try again with explicit init
            try {
                val sessionManager = SessionManager.getInstance()
                sessionManager.init(applicationContext)
                Log.d(TAG, "SessionManager initialized with explicit init")
            } catch (e: Exception) {
                Log.e(TAG, "Failed on second attempt to initialize SessionManager: ${e.message}", e)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Log.d(TAG, "Application attachBaseContext called")
    }
}