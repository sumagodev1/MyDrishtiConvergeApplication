package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Debug configuration management for the tutorial system
 */
object TutorialDebugConfig {
    
    private const val DEBUG_PREFS_NAME = "tutorial_debug_preferences"
    private const val PREF_DEBUG_MODE_ENABLED = "debug_mode_enabled"
    private const val PREF_VERBOSE_LOGGING = "verbose_logging"
    private const val PREF_SHOW_STEP_TRANSITIONS = "show_step_transitions"
    private const val PREF_ANIMATION_DEBUG = "animation_debug"
    private const val PREF_STATE_TRACKING = "state_tracking"
    private const val PREF_PERFORMANCE_MONITORING = "performance_monitoring"
    
    /**
     * Initializes debug configuration
     */
    fun initialize(context: Context) {
        val prefs = getDebugPreferences(context)
        
        // Set default debug settings if not already configured
        if (!prefs.contains(PREF_DEBUG_MODE_ENABLED)) {
            prefs.edit()
                .putBoolean(PREF_DEBUG_MODE_ENABLED, TutorialConstants.DEBUG_MODE_ENABLED)
                .putBoolean(PREF_VERBOSE_LOGGING, true)
                .putBoolean(PREF_SHOW_STEP_TRANSITIONS, true)
                .putBoolean(PREF_ANIMATION_DEBUG, false)
                .putBoolean(PREF_STATE_TRACKING, true)
                .putBoolean(PREF_PERFORMANCE_MONITORING, false)
                .apply()
        }
        
        if (isDebugModeEnabled(context)) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial debug mode initialized")
        }
    }
    
    /**
     * Gets debug preferences
     */
    private fun getDebugPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(DEBUG_PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Checks if debug mode is enabled
     */
    fun isDebugModeEnabled(context: Context): Boolean {
        return getDebugPreferences(context).getBoolean(PREF_DEBUG_MODE_ENABLED, TutorialConstants.DEBUG_MODE_ENABLED)
    }
    
    /**
     * Enables or disables debug mode
     */
    fun setDebugModeEnabled(context: Context, enabled: Boolean) {
        getDebugPreferences(context).edit()
            .putBoolean(PREF_DEBUG_MODE_ENABLED, enabled)
            .apply()
        
        Log.d(TutorialConstants.DEBUG_LOG_TAG, "Debug mode ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Checks if verbose logging is enabled
     */
    fun isVerboseLoggingEnabled(context: Context): Boolean {
        return isDebugModeEnabled(context) && 
               getDebugPreferences(context).getBoolean(PREF_VERBOSE_LOGGING, true)
    }
    
    /**
     * Enables or disables verbose logging
     */
    fun setVerboseLoggingEnabled(context: Context, enabled: Boolean) {
        getDebugPreferences(context).edit()
            .putBoolean(PREF_VERBOSE_LOGGING, enabled)
            .apply()
    }
    
    /**
     * Checks if step transition logging is enabled
     */
    fun isStepTransitionLoggingEnabled(context: Context): Boolean {
        return isDebugModeEnabled(context) && 
               getDebugPreferences(context).getBoolean(PREF_SHOW_STEP_TRANSITIONS, true)
    }
    
    /**
     * Enables or disables step transition logging
     */
    fun setStepTransitionLoggingEnabled(context: Context, enabled: Boolean) {
        getDebugPreferences(context).edit()
            .putBoolean(PREF_SHOW_STEP_TRANSITIONS, enabled)
            .apply()
    }
    
    /**
     * Checks if animation debugging is enabled
     */
    fun isAnimationDebugEnabled(context: Context): Boolean {
        return isDebugModeEnabled(context) && 
               getDebugPreferences(context).getBoolean(PREF_ANIMATION_DEBUG, false)
    }
    
    /**
     * Enables or disables animation debugging
     */
    fun setAnimationDebugEnabled(context: Context, enabled: Boolean) {
        getDebugPreferences(context).edit()
            .putBoolean(PREF_ANIMATION_DEBUG, enabled)
            .apply()
    }
    
    /**
     * Checks if state tracking is enabled
     */
    fun isStateTrackingEnabled(context: Context): Boolean {
        return isDebugModeEnabled(context) && 
               getDebugPreferences(context).getBoolean(PREF_STATE_TRACKING, true)
    }
    
    /**
     * Enables or disables state tracking
     */
    fun setStateTrackingEnabled(context: Context, enabled: Boolean) {
        getDebugPreferences(context).edit()
            .putBoolean(PREF_STATE_TRACKING, enabled)
            .apply()
    }
    
    /**
     * Checks if performance monitoring is enabled
     */
    fun isPerformanceMonitoringEnabled(context: Context): Boolean {
        return isDebugModeEnabled(context) && 
               getDebugPreferences(context).getBoolean(PREF_PERFORMANCE_MONITORING, false)
    }
    
    /**
     * Enables or disables performance monitoring
     */
    fun setPerformanceMonitoringEnabled(context: Context, enabled: Boolean) {
        getDebugPreferences(context).edit()
            .putBoolean(PREF_PERFORMANCE_MONITORING, enabled)
            .apply()
    }
    
    /**
     * Logs debug message if verbose logging is enabled
     */
    fun logDebug(context: Context, tag: String, message: String) {
        if (isVerboseLoggingEnabled(context)) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Logs step transition if step transition logging is enabled
     */
    fun logStepTransition(context: Context, fromStep: String?, toStep: String) {
        if (isStepTransitionLoggingEnabled(context)) {
            val transitionMessage = if (fromStep != null) {
                "Step transition: $fromStep -> $toStep"
            } else {
                "Starting tutorial with step: $toStep"
            }
            Log.d(TutorialConstants.DEBUG_LOG_TAG, transitionMessage)
        }
    }
    
    /**
     * Logs animation debug info if animation debugging is enabled
     */
    fun logAnimationDebug(context: Context, animationType: AnimationType, duration: Long, targetView: String?) {
        if (isAnimationDebugEnabled(context)) {
            val animationMessage = "Animation: ${animationType.name}, Duration: ${duration}ms, Target: ${targetView ?: "none"}"
            Log.d(TutorialConstants.DEBUG_LOG_TAG, animationMessage)
        }
    }
    
    /**
     * Logs state change if state tracking is enabled
     */
    fun logStateChange(context: Context, oldState: String, newState: String) {
        if (isStateTrackingEnabled(context)) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "State change: $oldState -> $newState")
        }
    }
    
    /**
     * Logs performance metrics if performance monitoring is enabled
     */
    fun logPerformanceMetric(context: Context, operation: String, durationMs: Long) {
        if (isPerformanceMonitoringEnabled(context)) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Performance: $operation took ${durationMs}ms")
        }
    }
    
    /**
     * Gets all debug configuration settings
     */
    fun getDebugConfiguration(context: Context): Map<String, Boolean> {
        return mapOf(
            "debug_mode_enabled" to isDebugModeEnabled(context),
            "verbose_logging" to isVerboseLoggingEnabled(context),
            "step_transition_logging" to isStepTransitionLoggingEnabled(context),
            "animation_debug" to isAnimationDebugEnabled(context),
            "state_tracking" to isStateTrackingEnabled(context),
            "performance_monitoring" to isPerformanceMonitoringEnabled(context)
        )
    }
    
    /**
     * Resets all debug configuration to defaults
     */
    fun resetDebugConfiguration(context: Context) {
        getDebugPreferences(context).edit().clear().apply()
        initialize(context)
        
        Log.d(TutorialConstants.DEBUG_LOG_TAG, "Debug configuration reset to defaults")
    }
    
    /**
     * Exports debug configuration
     */
    fun exportDebugConfiguration(context: Context): String {
        val config = getDebugConfiguration(context)
        
        return buildString {
            appendLine("=== Tutorial Debug Configuration ===")
            appendLine("Export Time: ${java.util.Date()}")
            appendLine()
            config.forEach { (key, value) ->
                appendLine("$key: $value")
            }
        }
    }
}