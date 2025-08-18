package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages tutorial state persistence using SharedPreferences
 */
class TutorialStateManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "tutorial_preferences", 
        Context.MODE_PRIVATE
    )
    
    /**
     * Checks if this is a first-time user
     */
    fun isFirstTimeUser(): Boolean {
        return sharedPreferences.getBoolean(TutorialConstants.PREF_FIRST_TIME_USER, true)
    }
    
    /**
     * Marks the user as no longer first-time
     */
    fun markUserAsReturning() {
        sharedPreferences.edit()
            .putBoolean(TutorialConstants.PREF_FIRST_TIME_USER, false)
            .apply()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "User marked as returning")
        }
    }
    
    /**
     * Checks if the tutorial has been completed
     */
    fun isTutorialCompleted(): Boolean {
        return sharedPreferences.getBoolean(TutorialConstants.PREF_TUTORIAL_COMPLETED, false)
    }
    
    /**
     * Marks the tutorial as completed
     */
    fun markTutorialCompleted() {
        sharedPreferences.edit()
            .putBoolean(TutorialConstants.PREF_TUTORIAL_COMPLETED, true)
            .putLong("tutorial_completed_timestamp", System.currentTimeMillis())
            .apply()
        
        markUserAsReturning()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial marked as completed")
        }
    }
    
    /**
     * Checks if the tutorial was skipped
     */
    fun isTutorialSkipped(): Boolean {
        return sharedPreferences.getBoolean(TutorialConstants.PREF_TUTORIAL_SKIPPED, false)
    }
    
    /**
     * Marks the tutorial as skipped
     */
    fun markTutorialSkipped() {
        sharedPreferences.edit()
            .putBoolean(TutorialConstants.PREF_TUTORIAL_SKIPPED, true)
            .putLong("tutorial_skipped_timestamp", System.currentTimeMillis())
            .apply()
        
        markUserAsReturning()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial marked as skipped")
        }
    }
    
    /**
     * Checks if a specific tutorial step has been completed
     */
    fun isStepCompleted(stepId: String): Boolean {
        return sharedPreferences.getBoolean(
            TutorialConstants.PREF_TUTORIAL_STEP_COMPLETED + stepId, 
            false
        )
    }
    
    /**
     * Marks a specific tutorial step as completed
     */
    fun markStepCompleted(stepId: String) {
        sharedPreferences.edit()
            .putBoolean(TutorialConstants.PREF_TUTORIAL_STEP_COMPLETED + stepId, true)
            .putLong("step_${stepId}_completed_timestamp", System.currentTimeMillis())
            .apply()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step marked as completed: $stepId")
        }
    }
    
    /**
     * Gets the current tutorial step (last incomplete step)
     */
    fun getCurrentStep(config: TutorialConfig): TutorialStep? {
        for (step in config.steps) {
            if (!isStepCompleted(step.id)) {
                return step
            }
        }
        return null // All steps completed
    }
    
    /**
     * Gets tutorial progress as a percentage
     */
    fun getTutorialProgress(config: TutorialConfig): Float {
        if (config.steps.isEmpty()) return 100f
        
        val completedSteps = config.steps.count { isStepCompleted(it.id) }
        return (completedSteps.toFloat() / config.steps.size) * 100f
    }
    
    /**
     * Resets all tutorial state (for restart functionality)
     */
    fun resetTutorialState() {
        val editor = sharedPreferences.edit()
        
        // Reset main tutorial flags
        editor.putBoolean(TutorialConstants.PREF_TUTORIAL_COMPLETED, false)
        editor.putBoolean(TutorialConstants.PREF_TUTORIAL_SKIPPED, false)
        editor.putBoolean(TutorialConstants.PREF_FIRST_TIME_USER, true)
        
        // Reset all step completion flags
        val allKeys = sharedPreferences.all.keys
        for (key in allKeys) {
            if (key.startsWith(TutorialConstants.PREF_TUTORIAL_STEP_COMPLETED)) {
                editor.remove(key)
            }
        }
        
        editor.apply()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial state reset")
        }
    }
    
    /**
     * Gets tutorial state summary
     */
    fun getTutorialState(): TutorialState {
        return TutorialState(
            isActive = false, // This would be managed by TutorialManager
            currentStepId = null, // This would be managed by TutorialManager
            completedSteps = getCompletedSteps().toSet(),
            startTime = sharedPreferences.getLong("tutorial_start_time", 0L),
            lastInteractionTime = System.currentTimeMillis(),
            skipCount = 0,
            completionStatus = when {
                isTutorialCompleted() -> CompletionStatus.COMPLETED
                isTutorialSkipped() -> CompletionStatus.SKIPPED
                else -> CompletionStatus.NOT_STARTED
            },
            wasSkipped = isTutorialSkipped(),
            onboardingCompleted = isTutorialCompleted(),
            canRestart = true
        )
    }
    
    /**
     * Gets list of completed step IDs
     */
    private fun getCompletedSteps(): List<String> {
        val completedSteps = mutableListOf<String>()
        val allKeys = sharedPreferences.all.keys
        
        for (key in allKeys) {
            if (key.startsWith(TutorialConstants.PREF_TUTORIAL_STEP_COMPLETED) && 
                sharedPreferences.getBoolean(key, false)) {
                val stepId = key.removePrefix(TutorialConstants.PREF_TUTORIAL_STEP_COMPLETED)
                completedSteps.add(stepId)
            }
        }
        
        return completedSteps
    }
    
    /**
     * Saves tutorial version
     */
    fun saveTutorialVersion(version: String) {
        sharedPreferences.edit()
            .putString(TutorialConstants.PREF_TUTORIAL_VERSION, version)
            .apply()
    }
    
    /**
     * Gets saved tutorial version
     */
    fun getTutorialVersion(): String {
        return sharedPreferences.getString(
            TutorialConstants.PREF_TUTORIAL_VERSION, 
            TutorialConstants.TUTORIAL_CONFIG_VERSION
        ) ?: TutorialConstants.TUTORIAL_CONFIG_VERSION
    }
    
    /**
     * Checks if tutorial should be shown based on state
     */
    fun shouldShowTutorial(): Boolean {
        return isFirstTimeUser() && !isTutorialCompleted() && !isTutorialSkipped()
    }
}