package com.mydrishti.co.`in`.activities

import android.app.Application
import com.mydrishti.co.`in`.activities.utils.SessionManager

class MyDrishtiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SessionManager when the application starts
        SessionManager.getInstance().init(applicationContext)
    }
}