package com.mydrishti.co.`in`.activities

import android.app.Application
import android.content.Context
import android.util.Log
import com.mydrishti.co.`in`.activities.api.ApiClient
import com.mydrishti.co.`in`.activities.dao.ChartDao
import com.mydrishti.co.`in`.activities.dao.ParameterDao
import com.mydrishti.co.`in`.activities.database.AppDatabase
import com.mydrishti.co.`in`.activities.repositories.ChartRepository
import com.mydrishti.co.`in`.activities.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Custom Application class for MyDrishti app
 * Handles initialization of app-wide singletons and services
 */
class MyDrishtiApplication : Application() {

    companion object {
        private const val TAG = "MyDrishtiApplication"
    }
    
    // Lazy initialization of the chart repository
    val chartRepository: ChartRepository by lazy {
        val apiService = ApiClient.getApiService()
        val chartDao = AppDatabase.getDatabase(this).chartDao()
        val parameterDao = AppDatabase.getDatabase(this).parameterDao()
        val sessionManager = SessionManager.getInstance(this)

        ChartRepository(
            chartDao,
            parameterDao,
            apiService,
            sessionManager
        )
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate started")

        // Clear any existing database file to force recreation
        clearDatabaseFiles()

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
        
        // Initialize database
        initializeDatabase()
        
        // Initialize chart repository
        Log.d(TAG, "Chart repository initialized")
    }

    private fun clearDatabaseFiles() {
        try {
            // Delete the old database files
            val oldDbFile = getDatabasePath("my_drishti_database")
            if (oldDbFile.exists()) {
                oldDbFile.delete()
                Log.d(TAG, "Deleted old database file")
            }
            
            val oldDbShm = getDatabasePath("my_drishti_database-shm")
            if (oldDbShm.exists()) {
                oldDbShm.delete()
                Log.d(TAG, "Deleted old database shm file")
            }
            
            val oldDbWal = getDatabasePath("my_drishti_database-wal")
            if (oldDbWal.exists()) {
                oldDbWal.delete()
                Log.d(TAG, "Deleted old database wal file")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete old database files: ${e.message}", e)
        }
    }
    
    private fun initializeDatabase() {
        // Initialize the database on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Force database creation by accessing a DAO method
                val db = AppDatabase.getDatabase(applicationContext)
                val charts = db.chartDao().getAllChartConfigsSync()
                Log.d(TAG, "Database initialized successfully with ${charts.size} charts")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing database: ${e.message}", e)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Log.d(TAG, "Application attachBaseContext called")
    }
}