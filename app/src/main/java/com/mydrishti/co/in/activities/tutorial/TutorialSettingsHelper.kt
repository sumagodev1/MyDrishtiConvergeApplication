package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.util.Log

/**
 * Helper class for tutorial settings and configuration management
 */
class TutorialSettingsHelper(private val context: Context) {
    
    private val tutorialStateManager = TutorialStateManager(context)
    private val firstTimeUserDetector = FirstTimeUserDetector(context)
    private val restartManager = TutorialRestartManager(context)
    
    /**
     * Gets tutorial settings information for display in settings UI
     */
    fun getTutorialSettingsInfo(): TutorialSettingsInfo {
        val currentState = tutorialStateManager.getTutorialState()
        
        return TutorialSettingsInfo(
            isAvailable = true,
            currentStatus = getTutorialStatusText(currentState),
            canRestart = restartManager.canRestartTutorial(),
            restartButtonText = "Restart Tutorial",
            completionDate = if (currentState.isCompleted()) currentState.lastUpdated else null,
            skipDate = if (currentState.isSkipped()) currentState.lastUpdated else null,
            totalStepsCompleted = currentState.completedSteps.size,
            timeSpent = 0L, // Simplified for now
            isFirstTimeUser = firstTimeUserDetector.isFirstTimeUser()
        )
    }
    
    /**
     * Gets human-readable tutorial status text
     */
    private fun getTutorialStatusText(state: TutorialState): String {
        return when (state.completionStatus) {
            CompletionStatus.NOT_STARTED -> "Not started"
            CompletionStatus.IN_PROGRESS -> "In progress (${state.completedSteps.size} steps completed)"
            CompletionStatus.COMPLETED -> "Completed"
            CompletionStatus.SKIPPED -> "Skipped"
            CompletionStatus.ERROR -> "Failed"
        }
    }
    
    /**
     * Handles restart request from settings
     */
    fun handleSettingsRestartRequest(
        onRestartConfirmed: () -> Unit,
        onRestartCancelled: () -> Unit
    ) {
        // For now, just confirm restart
        onRestartConfirmed()
    }
    
    /**
     * Gets tutorial statistics for analytics
     */
    fun getTutorialStatistics(): Map<String, Any> {
        val state = tutorialStateManager.getTutorialState()
        
        return mapOf(
            "tutorial_completed" to state.isCompleted(),
            "tutorial_skipped" to state.isSkipped(),
            "steps_completed" to state.completedSteps.size,
            "is_first_time_user" to firstTimeUserDetector.isFirstTimeUser(),
            "tutorial_available" to true
        )
    }
    
    /**
     * Resets all tutorial data
     */
    fun resetAllTutorialData(onComplete: (success: Boolean) -> Unit) {
        try {
            tutorialStateManager.resetTutorialState()
            restartManager.resetRestartAttempts()
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "All tutorial data reset")
            }
            
            onComplete(true)
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Failed to reset tutorial data", e)
            }
            
            onComplete(false)
        }
    }
    
    /**
     * Exports tutorial data for debugging
     */
    fun exportTutorialData(): String {
        val state = tutorialStateManager.getTutorialState()
        
        return """
            Tutorial Status: ${getTutorialStatusText(state)}
            Steps Completed: ${state.completedSteps.size}
            Is First Time User: ${firstTimeUserDetector.isFirstTimeUser()}
            Last Updated: ${state.lastUpdated}
        """.trimIndent()
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        restartManager.cleanup()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "TutorialSettingsHelper cleaned up")
        }
    }
}

/**
 * Data class for tutorial settings information
 */
data class TutorialSettingsInfo(
    val isAvailable: Boolean,
    val currentStatus: String,
    val canRestart: Boolean,
    val restartButtonText: String,
    val completionDate: Long?,
    val skipDate: Long?,
    val totalStepsCompleted: Int,
    val timeSpent: Long,
    val isFirstTimeUser: Boolean
)