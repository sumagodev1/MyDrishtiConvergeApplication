package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.content.Intent
import android.util.Log

/**
 * Integration helper for connecting tutorial system with MainActivity
 * This class provides methods to integrate tutorial functionality into MainActivity
 */
class MainActivityIntegration(private val activity: Activity) {
    
    private var tutorialManager: TutorialManager? = null
    
    /**
     * Handles tutorial initialization based on intent extras from LoginActivity
     * Call this method in MainActivity's onCreate() or onResume()
     */
    fun handleTutorialFromIntent(intent: Intent) {
        val shouldShowTutorial = LoginTutorialIntegration.shouldShowTutorialFromIntent(intent)
        val isFirstTimeUser = LoginTutorialIntegration.isFirstTimeUserFromIntent(intent)
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, 
                "MainActivity received tutorial intent - Show: $shouldShowTutorial, FirstTime: $isFirstTimeUser")
        }
        
        if (shouldShowTutorial) {
            startTutorial()
        }
    }
    
    /**
     * Starts the tutorial system
     */
    fun startTutorial() {
        if (tutorialManager == null) {
            tutorialManager = TutorialManager.create(activity)
        }
        
        val success = tutorialManager?.startFirstTimeUserTutorial() ?: false
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial start result: $success")
        }
    }
    
    /**
     * Checks if tutorial should be shown (alternative to intent-based approach)
     */
    fun checkAndStartTutorial() {
        if (TutorialManager.shouldShowTutorial(activity)) {
            startTutorial()
        }
    }
    
    /**
     * Handles tutorial restart from settings or other sources
     */
    fun restartTutorial(source: TutorialRestartManager.RestartSource = TutorialRestartManager.RestartSource.USER_REQUEST) {
        if (tutorialManager == null) {
            tutorialManager = TutorialManager.create(activity)
        }
        
        tutorialManager?.restartTutorial(source)
    }
    
    /**
     * Pauses tutorial (call in onPause())
     */
    fun pauseTutorial() {
        tutorialManager?.pauseTutorial()
    }
    
    /**
     * Resumes tutorial (call in onResume())
     */
    fun resumeTutorial() {
        tutorialManager?.resumeTutorial()
    }
    
    /**
     * Checks if tutorial is currently active
     */
    fun isTutorialActive(): Boolean {
        return tutorialManager?.isActive() ?: false
    }
    
    /**
     * Gets tutorial analytics
     */
    fun getTutorialAnalytics(): Map<String, Any> {
        return tutorialManager?.getAnalytics() ?: emptyMap()
    }
    
    /**
     * Gets tutorial settings helper for settings integration
     */
    fun getTutorialSettingsHelper(): TutorialSettingsHelper {
        return TutorialSettingsHelper(activity)
    }
    
    /**
     * Handles back button press during tutorial
     */
    fun handleBackPressedDuringTutorial(): Boolean {
        return if (isTutorialActive()) {
            // Tutorial is active, let it handle back press
            // You might want to show a confirmation dialog or pause tutorial
            pauseTutorial()
            true // Consumed the back press
        } else {
            false // Let normal back press handling continue
        }
    }
    
    /**
     * Cleanup method (call in onDestroy())
     */
    fun cleanup() {
        tutorialManager = null
    }
    
    companion object {
        /**
         * Creates integration instance
         */
        fun create(activity: Activity): MainActivityIntegration {
            return MainActivityIntegration(activity)
        }
        
        /**
         * Quick check if intent contains tutorial information
         */
        fun hasTutorialIntent(intent: Intent): Boolean {
            return intent.hasExtra(LoginTutorialIntegration.EXTRA_SHOW_TUTORIAL) ||
                   intent.hasExtra(LoginTutorialIntegration.EXTRA_IS_FIRST_TIME_USER)
        }
    }
}