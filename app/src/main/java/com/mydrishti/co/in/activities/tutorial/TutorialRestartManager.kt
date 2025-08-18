package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.util.Log

/**
 * Manages tutorial restart functionality with confirmation and analytics
 */
class TutorialRestartManager(
    private val context: Context
) {
    
    private var restartAttemptCount = 0
    
    /**
     * Shows restart confirmation dialog
     */
    fun showRestartConfirmation(activity: android.app.Activity, onResult: (Boolean) -> Unit) {
        val dialog = android.app.AlertDialog.Builder(activity)
            .setTitle("Restart Tutorial")
            .setMessage("Are you sure you want to restart the tutorial from the beginning?")
            .setPositiveButton("Restart") { _, _ -> onResult(true) }
            .setNegativeButton("Cancel") { _, _ -> onResult(false) }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    /**
     * Checks if tutorial restart is currently allowed
     */
    fun canRestartTutorial(): Boolean {
        return restartAttemptCount < 5
    }
    
    /**
     * Records a restart attempt
     */
    fun recordRestartAttempt() {
        restartAttemptCount++
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Restart attempt #$restartAttemptCount")
        }
    }
    
    /**
     * Resets restart attempt count
     */
    fun resetRestartAttempts() {
        restartAttemptCount = 0
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "TutorialRestartManager cleaned up")
        }
    }
    
    enum class RestartSource {
        SETTINGS_MENU,
        TUTORIAL_COMPLETED,
        TUTORIAL_SKIPPED,
        USER_REQUEST,
        DEBUG_MODE
    }
}