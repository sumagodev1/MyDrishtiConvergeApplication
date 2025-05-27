package com.mydrishti.co.`in`.activities.api

import com.mydrishti.co.`in`.activities.LoginActivity
import okhttp3.Interceptor
import okhttp3.Response
import android.content.Intent
import android.util.Log
import com.mydrishti.co.`in`.activities.utils.SessionManager

class AuthInterceptor : Interceptor {
    private val TAG = "AuthInterceptor"
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val token = SessionManager.getInstance().getAuthToken()
        Log.d("mytag","=========")
        Log.d("mytag",token)
        val newRequest = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        val response = chain.proceed(newRequest)
        
        // Handle 401 Unauthorized error
        if (response.code == 401) {
            Log.d(TAG, "Received 401 Unauthorized")
            
            // Get application context using reflection (not ideal but works for interceptors)
            val context = SessionManager.getInstance().getContext()
            
            if (context != null) {
                // Clear credentials and start login activity
                SessionManager.getInstance().clearSession()
                
                val intent = Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("SESSION_EXPIRED", true)
                }
                
                context.startActivity(intent)
            }
        }
        
        return response
    }
}