package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.util.Log

/**
 * Enhanced skip functionality manager with confirmation dialogs and analytics
 */
class TutorialSkipManager(
    private val context: Context
) {
    
    private var skipAttemptCount = 0
    
    /**
     * Shows skip confirmation dialog
     */
    fun showSkipConfirmation(activity: android.app.Activity, onResult: (Boolean) -> Unit) {
        val dialog = android.app.AlertDialog.Builder(activity)
            .setTitle("Skip Tutorial")
            .setMessage("Are you sure you want to skip the tutorial? You can restart it later from settings.")
            .setPositiveButton("Skip") { _, _ -> onResult(true) }
            .setNegativeButton("Continue") { _, _ -> onResult(false) }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    /**
     * Handles skip request with appropriate confirmation flow
     */
    fun handleSkipRequest(
        currentStep: TutorialStep,
        onConfirmSkip: (skipReason: String?) -> Unit,
        onCancelSkip: () -> Unit
    ) {
        skipAttemptCount++
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Skip request #$skipAttemptCount for step: ${currentStep.id}")
        }
        
        // For now, just confirm skip
        onConfirmSkip(null)
    }
    
    /**
     * Records a skip attempt
     */
    fun recordSkipAttempt() {
        skipAttemptCount++
    }
    
    /**
     * Gets skip attempt count
     */
    fun getSkipAttemptCount(): Int = skipAttemptCount
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "TutorialSkipManager cleaned up")
        }
    }
}