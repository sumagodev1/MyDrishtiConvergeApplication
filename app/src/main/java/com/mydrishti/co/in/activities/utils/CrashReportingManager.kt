package com.mydrishti.co.`in`.activities.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Centralized crash reporting and error handling manager
 * Provides consistent error handling throughout the app
 */
object CrashReportingManager {

    const val TAG = "CrashReportingManager"
    private var isInitialized = false

    /**
     * Initialize the crash reporting system
     * @param context Application context
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            // Set up global exception handler
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                handleUncaughtException(thread, exception, context)
            }
            
            isInitialized = true
            Log.d(TAG, "CrashReportingManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize CrashReportingManager: ${e.message}")
        }
    }

    /**
     * Handle uncaught exceptions
     */
    private fun handleUncaughtException(thread: Thread, exception: Throwable, context: Context) {
        try {
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)
            
            // Log the stack trace
            val stackTrace = getStackTraceString(exception)
            Log.e(TAG, "Stack trace: $stackTrace")
            
            // In a production app, you would send this to a crash reporting service
            // For now, we'll just log it
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling uncaught exception: ${e.message}")
        }
    }

    /**
     * Safe execution wrapper for operations that might throw exceptions
     * @param operation The operation to execute safely
     * @param onError Optional error handler
     * @param defaultValue Default value to return if operation fails
     */
    fun <T> safeExecute(
        operation: () -> T,
        onError: ((Exception) -> Unit)? = null,
        defaultValue: T? = null
    ): T? {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e(TAG, "Safe execution failed: ${e.message}", e)
            onError?.invoke(e)
            defaultValue
        }
    }

    /**
     * Safe execution for suspend functions
     * @param scope Coroutine scope
     * @param operation The suspend operation to execute
     * @param onError Optional error handler
     */
    fun safeExecuteAsync(
        scope: CoroutineScope,
        operation: suspend () -> Unit,
        onError: ((Exception) -> Unit)? = null
    ) {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "Coroutine execution failed: ${exception.message}", exception)
            onError?.invoke(exception as? Exception ?: Exception(exception))
        }
        
        scope.launch(Dispatchers.Main + exceptionHandler) {
            try {
                operation()
            } catch (e: Exception) {
                Log.e(TAG, "Async execution failed: ${e.message}", e)
                onError?.invoke(e)
            }
        }
    }

    /**
     * Log an error with context information
     * @param tag Log tag
     * @param message Error message
     * @param throwable Optional throwable
     * @param context Additional context information
     */
    fun logError(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        context: Map<String, Any>? = null
    ) {
        try {
            val contextInfo = context?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
            val fullMessage = if (contextInfo.isNotEmpty()) {
                "$message | Context: $contextInfo"
            } else {
                message
            }
            
            if (throwable != null) {
                Log.e(tag, fullMessage, throwable)
            } else {
                Log.e(tag, fullMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging error: ${e.message}")
        }
    }

    /**
     * Log a warning with context information
     * @param tag Log tag
     * @param message Warning message
     * @param context Additional context information
     */
    fun logWarning(
        tag: String,
        message: String,
        context: Map<String, Any>? = null
    ) {
        try {
            val contextInfo = context?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
            val fullMessage = if (contextInfo.isNotEmpty()) {
                "$message | Context: $contextInfo"
            } else {
                message
            }
            
            Log.w(tag, fullMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error logging warning: ${e.message}")
        }
    }

    /**
     * Get stack trace as string
     */
    private fun getStackTraceString(throwable: Throwable): String {
        return try {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            throwable.printStackTrace(printWriter)
            stringWriter.toString()
        } catch (e: Exception) {
            "Failed to get stack trace: ${e.message}"
        }
    }

    /**
     * Check if a view is safe to access (not null and attached)
     * @param view The view to check
     * @return True if view is safe to access
     */
    fun isViewSafe(view: android.view.View?): Boolean {
        return try {
            view != null && view.isAttachedToWindow
        } catch (e: Exception) {
            Log.e(TAG, "Error checking view safety: ${e.message}")
            false
        }
    }

    /**
     * Safe view operation wrapper
     * @param view The view to operate on
     * @param operation The operation to perform on the view
     */
    fun safeViewOperation(view: android.view.View?, operation: (android.view.View) -> Unit) {
        try {
            if (isViewSafe(view)) {
                view?.let(operation)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Safe view operation failed: ${e.message}", e)
        }
    }

    /**
     * Memory usage monitoring
     * @param tag Tag for logging
     * @param context Context for memory info
     */
    fun logMemoryUsage(tag: String, context: Context) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val availableMemory = maxMemory - usedMemory
            
            Log.d(tag, "Memory Usage - Used: ${usedMemory / 1024 / 1024}MB, " +
                    "Available: ${availableMemory / 1024 / 1024}MB, " +
                    "Max: ${maxMemory / 1024 / 1024}MB")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging memory usage: ${e.message}")
        }
    }
}