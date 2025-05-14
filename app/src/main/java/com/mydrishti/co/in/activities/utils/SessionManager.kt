package com.mydrishti.co.`in`.activities.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mydrishti.co.`in`.activities.LoginActivity
import android.util.Log

class SessionManager private constructor() {
    private var context: Context? = null
    private lateinit var encryptedPrefs: SharedPreferences
    private val TAG = "SessionManager"
    
    companion object {
        @Volatile
        private var instance: SessionManager? = null
        
        fun getInstance(): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager().also { instance = it }
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
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences: ${e.message}")
            // Fallback to regular SharedPreferences in case of error
            encryptedPrefs = context!!.getSharedPreferences(LoginActivity.PREF_FILE_NAME, Context.MODE_PRIVATE)
        }
    }
    
    fun isLoggedIn(): Boolean {
        return encryptedPrefs.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
    }
    
    fun getAuthToken(): String {
        return encryptedPrefs.getString(LoginActivity.KEY_TOKEN, "") ?: ""
    }
    
    fun getUserEmail(): String {
        return encryptedPrefs.getString(LoginActivity.KEY_EMAIL, "") ?: ""
    }
    
    fun clearSession() {
        encryptedPrefs.edit().apply {
            remove(LoginActivity.KEY_TOKEN)
            putBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
}