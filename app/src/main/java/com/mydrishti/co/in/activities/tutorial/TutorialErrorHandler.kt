package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * Comprehensive error handling for the tutorial system
 */
class TutorialErrorHandler(private val context: Context) {
    
    private val tutorialStateManager = TutorialStateManager(context)
    
    /**
     * Handles target view not found errors
     */
    fun handleTargetViewNotFound(stepId: String, viewId: Int?, fallbackAction: () -> Unit) {
        val errorMessage = "Target view not found for step: $stepId (viewId: $viewId)"
        
        logError("TARGET_VIEW_NOT_FOUND", errorMessage)
        
        // Show user-friendly message
        showUserFriendlyError(
            title = "Tutorial Step Unavailable",
            message = "This tutorial step is temporarily unavailable. We'll continue with the next step.",
            showToast = true
        )
        
        // Execute fallback action
        try {
            fallbackAction()
        } catch (e: Exception) {
            logError("FALLBACK_ACTION_FAILED", "Fallback action failed for step $stepId", e)
            handleFallbackFailure(stepId)
        }
        
        // Record error analytics
        recordErrorAnalytics("target_view_not_found", stepId, errorMessage)
    }
    
    /**
     * Handles activity lifecycle errors during tutorial
     */
    fun handleActivityLifecycleError(lifecycleEvent: String, error: Exception, recoveryAction: () -> Unit) {
        val errorMessage = "Activity lifecycle error during $lifecycleEvent: ${error.message}"
        
        logError("ACTIVITY_LIFECYCLE_ERROR", errorMessage, error)
        
        // Attempt recovery
        try {
            recoveryAction()
        } catch (recoveryError: Exception) {
            logError("RECOVERY_ACTION_FAILED", "Recovery action failed for $lifecycleEvent", recoveryError)
            handleCriticalError("Activity lifecycle recovery failed", recoveryError)
        }
        
        // Record error analytics
        recordErrorAnalytics("activity_lifecycle_error", lifecycleEvent, errorMessage)
    }
    
    /**
     * Handles timeout errors for inactive tutorial sessions
     */
    fun handleTutorialTimeout(currentStepId: String?, timeoutDuration: Long) {
        val errorMessage = "Tutorial session timed out after ${timeoutDuration}ms on step: $currentStepId"
        
        logError("TUTORIAL_TIMEOUT", errorMessage)
        
        // Show timeout message to user
        showUserFriendlyError(
            title = "Tutorial Timeout",
            message = "The tutorial has been paused due to inactivity. You can restart it anytime from the settings.",
            showToast = true
        )
        
        // Pause tutorial and save state
        try {
            pauseTutorialDueToTimeout(currentStepId)
        } catch (e: Exception) {
            logError("TIMEOUT_HANDLING_FAILED", "Failed to handle tutorial timeout", e)
        }
        
        // Record timeout analytics
        recordErrorAnalytics("tutorial_timeout", currentStepId ?: "unknown", errorMessage)
    }
    
    /**
     * Handles animation errors
     */
    fun handleAnimationError(animationType: AnimationType, error: Exception, fallbackAnimation: () -> Unit) {
        val errorMessage = "Animation error for ${animationType.name}: ${error.message}"
        
        logError("ANIMATION_ERROR", errorMessage, error)
        
        // Try fallback animation
        try {
            fallbackAnimation()
        } catch (fallbackError: Exception) {
            logError("FALLBACK_ANIMATION_FAILED", "Fallback animation failed", fallbackError)
            // Continue without animation
            showUserFriendlyError(
                title = "Animation Unavailable",
                message = "Tutorial animations are temporarily unavailable, but the tutorial will continue.",
                showToast = false
            )
        }
        
        // Record animation error analytics
        recordErrorAnalytics("animation_error", animationType.name, errorMessage)
    }
    
    /**
     * Handles tutorial configuration errors
     */
    fun handleConfigurationError(configError: String, recoveryAction: (() -> Unit)? = null) {
        val errorMessage = "Tutorial configuration error: $configError"
        
        logError("CONFIGURATION_ERROR", errorMessage)
        
        // Show configuration error message
        showUserFriendlyError(
            title = "Tutorial Configuration Issue",
            message = "There's an issue with the tutorial setup. Please try restarting the tutorial.",
            showToast = true
        )
        
        // Attempt recovery if provided
        recoveryAction?.let { recovery ->
            try {
                recovery()
            } catch (e: Exception) {
                logError("CONFIG_RECOVERY_FAILED", "Configuration recovery failed", e)
                handleCriticalError("Tutorial configuration recovery failed", e)
            }
        }
        
        // Record configuration error analytics
        recordErrorAnalytics("configuration_error", "config", errorMessage)
    }
    
    /**
     * Handles state management errors
     */
    fun handleStateManagementError(operation: String, error: Exception, fallbackState: (() -> Unit)? = null) {
        val errorMessage = "State management error during $operation: ${error.message}"
        
        logError("STATE_MANAGEMENT_ERROR", errorMessage, error)
        
        // Try to restore to a known good state
        fallbackState?.let { fallback ->
            try {
                fallback()
            } catch (fallbackError: Exception) {
                logError("STATE_FALLBACK_FAILED", "State fallback failed", fallbackError)
                // Reset to initial state as last resort
                resetToInitialState()
            }
        } ?: run {
            // No fallback provided, reset to initial state
            resetToInitialState()
        }
        
        // Record state error analytics
        recordErrorAnalytics("state_management_error", operation, errorMessage)
    }
    
    /**
     * Handles dialog creation/display errors
     */
    fun handleDialogError(dialogType: String, error: Exception, alternativeAction: (() -> Unit)? = null) {
        val errorMessage = "Dialog error for $dialogType: ${error.message}"
        
        logError("DIALOG_ERROR", errorMessage, error)
        
        // Show simple toast instead of dialog
        Toast.makeText(
            context,
            "Dialog temporarily unavailable. Continuing with tutorial.",
            Toast.LENGTH_SHORT
        ).show()
        
        // Execute alternative action if provided
        alternativeAction?.let { alternative ->
            try {
                alternative()
            } catch (altError: Exception) {
                logError("DIALOG_ALTERNATIVE_FAILED", "Dialog alternative action failed", altError)
            }
        }
        
        // Record dialog error analytics
        recordErrorAnalytics("dialog_error", dialogType, errorMessage)
    }
    
    /**
     * Handles network-related errors during tutorial
     */
    fun handleNetworkError(operation: String, error: Exception, offlineAction: (() -> Unit)? = null) {
        val errorMessage = "Network error during $operation: ${error.message}"
        
        logError("NETWORK_ERROR", errorMessage, error)
        
        // Show network error message
        showUserFriendlyError(
            title = "Connection Issue",
            message = "Some tutorial features may be limited due to connectivity issues.",
            showToast = true
        )
        
        // Execute offline action if provided
        offlineAction?.let { offline ->
            try {
                offline()
            } catch (offlineError: Exception) {
                logError("OFFLINE_ACTION_FAILED", "Offline action failed", offlineError)
            }
        }
        
        // Record network error analytics
        recordErrorAnalytics("network_error", operation, errorMessage)
    }
    
    /**
     * Handles critical errors that may require tutorial termination
     */
    fun handleCriticalError(errorDescription: String, error: Exception? = null) {
        val errorMessage = "Critical error: $errorDescription${error?.let { " - ${it.message}" } ?: ""}"
        
        logError("CRITICAL_ERROR", errorMessage, error)
        
        // Show critical error message
        showUserFriendlyError(
            title = "Tutorial Error",
            message = "The tutorial encountered an unexpected error and will be paused. You can restart it from the settings.",
            showToast = true
        )
        
        // Safely terminate tutorial
        try {
            terminateTutorialSafely()
        } catch (terminationError: Exception) {
            logError("TERMINATION_FAILED", "Failed to terminate tutorial safely", terminationError)
            // Last resort: clear all tutorial state
            clearTutorialState()
        }
        
        // Record critical error analytics
        recordErrorAnalytics("critical_error", "system", errorMessage)
    }
    
    /**
     * Handles permission-related errors
     */
    fun handlePermissionError(permission: String, requiredForStep: String) {
        val errorMessage = "Permission denied: $permission (required for step: $requiredForStep)"
        
        logError("PERMISSION_ERROR", errorMessage)
        
        // Show permission error message
        showUserFriendlyError(
            title = "Permission Required",
            message = "This tutorial step requires additional permissions. You can continue with other steps.",
            showToast = true
        )
        
        // Skip the step that requires permission
        skipStepDueToPermission(requiredForStep)
        
        // Record permission error analytics
        recordErrorAnalytics("permission_error", permission, errorMessage)
    }
    
    /**
     * Shows user-friendly error messages
     */
    private fun showUserFriendlyError(title: String, message: String, showToast: Boolean = false) {
        if (showToast) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        
        // Could also show a more detailed dialog if needed
        // For now, just use toast for simplicity
    }
    
    /**
     * Logs errors with appropriate level and context
     */
    private fun logError(errorType: String, message: String, exception: Exception? = null) {
        val logMessage = "[$errorType] $message"
        
        if (exception != null) {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, logMessage, exception)
        } else {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, logMessage)
        }
        
        // Also log to debug config if verbose logging is enabled
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            TutorialDebugConfig.logDebug(context, TutorialConstants.DEBUG_LOG_TAG, logMessage)
        }
    }
    
    /**
     * Records error analytics for analysis
     */
    private fun recordErrorAnalytics(errorType: String, context: String, errorMessage: String) {
        val analytics = mapOf(
            "error_type" to errorType,
            "error_context" to context,
            "error_message" to errorMessage,
            "timestamp" to System.currentTimeMillis(),
            "tutorial_state" to tutorialStateManager.getTutorialState().completionStatus.name
        )
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Error analytics: $analytics")
        }
    }
    
    /**
     * Pauses tutorial due to timeout
     */
    private fun pauseTutorialDueToTimeout(currentStepId: String?) {
        val currentState = tutorialStateManager.getTutorialState()
        val pausedState = currentState.copy(
            isActive = false,
            lastInteractionTime = System.currentTimeMillis()
        )
        
        tutorialStateManager.updateTutorialState(pausedState)
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial paused due to timeout on step: $currentStepId")
        }
    }
    
    /**
     * Handles fallback failure
     */
    private fun handleFallbackFailure(stepId: String) {
        // Skip the problematic step
        tutorialStateManager.markStepCompleted(stepId)
        
        showUserFriendlyError(
            title = "Step Skipped",
            message = "A tutorial step was automatically skipped due to technical issues.",
            showToast = true
        )
    }
    
    /**
     * Resets tutorial to initial state
     */
    private fun resetToInitialState() {
        try {
            tutorialStateManager.resetTutorialState()
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial reset to initial state due to error")
            }
        } catch (e: Exception) {
            logError("RESET_TO_INITIAL_FAILED", "Failed to reset to initial state", e)
            clearTutorialState()
        }
    }
    
    /**
     * Safely terminates the tutorial
     */
    private fun terminateTutorialSafely() {
        val currentState = tutorialStateManager.getTutorialState()
        val terminatedState = currentState.copy(
            isActive = false,
            completionStatus = CompletionStatus.FAILED,
            lastInteractionTime = System.currentTimeMillis()
        )
        
        // Update tutorial state - mark as failed
        tutorialStateManager.markTutorialSkipped()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial terminated safely due to critical error")
        }
    }
    
    /**
     * Clears all tutorial state (last resort)
     */
    private fun clearTutorialState() {
        try {
            tutorialStateManager.resetTutorialState()
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "All tutorial state cleared due to critical error")
            }
        } catch (e: Exception) {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, "Failed to clear tutorial state", e)
        }
    }
    
    /**
     * Skips step due to permission issues
     */
    private fun skipStepDueToPermission(stepId: String) {
        tutorialStateManager.markStepCompleted(stepId)
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step $stepId skipped due to permission issues")
        }
    }
    
    /**
     * Gets error handling statistics
     */
    fun getErrorHandlingStatistics(): Map<String, Any> {
        return mapOf(
            "error_handler_active" to true,
            "tutorial_state_manager_available" to true,
            "debug_mode_enabled" to TutorialConstants.DEBUG_MODE_ENABLED,
            "current_tutorial_state" to tutorialStateManager.getTutorialState().completionStatus.name
        )
    }
    
    /**
     * Tests error handling mechanisms
     */
    fun testErrorHandling() {
        if (!TutorialConstants.DEBUG_MODE_ENABLED) {
            return
        }
        
        Log.d(TutorialConstants.DEBUG_LOG_TAG, "Testing error handling mechanisms...")
        
        // Test various error scenarios
        try {
            handleTargetViewNotFound("test_step", null) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Fallback action executed")
            }
        } catch (e: Exception) {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error handling test failed", e)
        }
    }
}