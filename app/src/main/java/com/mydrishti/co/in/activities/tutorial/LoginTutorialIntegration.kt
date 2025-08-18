package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.mydrishti.co.`in`.activities.MainActivity

/**
 * Integration helper for connecting tutorial system with login flow
 */
class LoginTutorialIntegration(private val activity: Activity) {
    
    private val firstTimeUserDetector = FirstTimeUserDetector(activity)
    
    /**
     * Handles post-login navigation with tutorial integration
     * This should be called after successful login instead of direct navigation to MainActivity
     */
    fun handlePostLoginNavigation(email: String) {
        // Record the login attempt and success
        firstTimeUserDetector.recordLoginAttempt()
        firstTimeUserDetector.recordSuccessfulLogin()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Post-login navigation for user: $email")
        }
        
        // Check if tutorial should be shown
        val shouldShowTutorial = firstTimeUserDetector.shouldShowTutorial()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            val analytics = firstTimeUserDetector.getUserAnalytics()
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial decision: $shouldShowTutorial")
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "User analytics: $analytics")
        }
        
        // Navigate to MainActivity with tutorial flag
        navigateToMainActivity(shouldShowTutorial)
    }
    
    /**
     * Navigates to MainActivity with tutorial information
     */
    private fun navigateToMainActivity(shouldShowTutorial: Boolean) {
        val intent = Intent(activity, MainActivity::class.java)
        
        // Add tutorial flag to intent
        intent.putExtra(EXTRA_SHOW_TUTORIAL, shouldShowTutorial)
        intent.putExtra(EXTRA_IS_FIRST_TIME_USER, firstTimeUserDetector.isFirstLogin())
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, 
                "Navigating to MainActivity with tutorial flag: $shouldShowTutorial")
        }
        
        activity.startActivity(intent)
        activity.finish()
    }
    
    /**
     * Records user actions that might affect tutorial eligibility
     */
    fun recordUserAction(action: String) {
        firstTimeUserDetector.recordUserAction(action)
    }
    
    /**
     * Gets current user analytics for debugging
     */
    fun getUserAnalytics(): Map<String, Any> {
        return firstTimeUserDetector.getUserAnalytics()
    }
    
    /**
     * Forces tutorial eligibility (for testing)
     */
    fun forceTutorialEligibility() {
        firstTimeUserDetector.forceTutorialEligibility()
    }
    
    /**
     * Resets first-time user data (for testing)
     */
    fun resetFirstTimeUserData() {
        firstTimeUserDetector.resetFirstTimeUserData()
    }
    
    companion object {
        // Intent extras for MainActivity
        const val EXTRA_SHOW_TUTORIAL = "show_tutorial"
        const val EXTRA_IS_FIRST_TIME_USER = "is_first_time_user"
        
        /**
         * Helper method to check if MainActivity should show tutorial
         */
        fun shouldShowTutorialFromIntent(intent: Intent): Boolean {
            return intent.getBooleanExtra(EXTRA_SHOW_TUTORIAL, false)
        }
        
        /**
         * Helper method to check if user is first-time from intent
         */
        fun isFirstTimeUserFromIntent(intent: Intent): Boolean {
            return intent.getBooleanExtra(EXTRA_IS_FIRST_TIME_USER, false)
        }
    }
}