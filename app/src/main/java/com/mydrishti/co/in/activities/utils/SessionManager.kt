package com.mydrishti.co.`in`.activities.utils
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mydrishti.co.`in`.activities.LoginActivity
import android.util.Log

/**
 * SessionManager - Handles user authentication session, token management and secure storage
 * Singleton implementation for app-wide access
 */
class SessionManager private constructor() {
    private var context: Context? = null
    private var encryptedPrefs: SharedPreferences? = null
    private val TAG = "SessionManager"

    companion object {
        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context? = null): SessionManager {
            if (instance == null && context == null) {
                throw IllegalStateException("SessionManager must be initialized with context first")
            }

            return instance ?: synchronized(this) {
                instance ?: SessionManager().also {
                    instance = it
                    context?.let { ctx -> it.init(ctx) }
                }
            }
        }
    }

    fun init(appContext: Context) {
        this.context = appContext.applicationContext
        initEncryptedPrefs()
    }

    fun getContext(): Context? {
        return context
    }

    private fun initEncryptedPrefs() {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot initialize EncryptedPrefs")
            return
        }

        try {
            val masterKey = MasterKey.Builder(context!!)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedPrefs = EncryptedSharedPreferences.create(
                context!!,
                LoginActivity.PREF_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            Log.d(TAG, "EncryptedPrefs initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences: ${e.message}")
            // Fallback to regular SharedPreferences in case of error
            encryptedPrefs = context!!.getSharedPreferences(LoginActivity.PREF_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    // Ensure prefs are initialized before use
    private fun getPrefs(): SharedPreferences {
        if (encryptedPrefs == null) {
            if (context == null) {
                throw IllegalStateException("SessionManager not properly initialized with context")
            }
            initEncryptedPrefs()
        }

        return encryptedPrefs ?: context!!.getSharedPreferences(
            LoginActivity.PREF_FILE_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun isLoggedIn(): Boolean {
        return getPrefs().getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
    }

    fun getAuthToken(): String {
        return getPrefs().getString(LoginActivity.KEY_TOKEN, "") ?: ""
    }

    fun getUserEmail(): String {
        return getPrefs().getString(LoginActivity.KEY_EMAIL, "") ?: ""
    }

    fun getUsername(): String {
        return getPrefs().getString(LoginActivity.KEY_EMAIL,"") ?: ""
    }

    /**
     * Logs out the current user by clearing authentication token and login status
     * but preserves saved email/password for convenience on next login
     */
    fun logout() {
        getPrefs().edit().apply {
            remove(LoginActivity.KEY_TOKEN)
            putBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
            apply()
        }
        Log.d(TAG, "User logged out successfully")
    }

    fun clearSession() {
        getPrefs().edit().apply {
            remove(LoginActivity.KEY_TOKEN)
            putBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    /**
     * Completely clears all user data including saved credentials
     * Use this for "forget me" functionality
     */
    fun clearAllUserData() {
        getPrefs().edit().apply {
            remove(LoginActivity.KEY_TOKEN)
            remove(LoginActivity.KEY_EMAIL)
            remove(LoginActivity.KEY_PASSWORD)
            putBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
            apply()
        }
        Log.d(TAG, "All user data cleared")
    }
}