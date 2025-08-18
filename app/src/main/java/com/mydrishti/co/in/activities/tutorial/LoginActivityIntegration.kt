package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.mydrishti.co.`in`.activities.MainActivity

/**
 * Integration helper for connecting tutorial system with LoginActivity
 * This class provides methods to integrate tutorial functionality into the existing LoginActivity
 */
class LoginActivityIntegration(private val activity: Activity) {
    
    private val loginTutorialIntegration = LoginTutorialIntegration(activity)
    
    /**
     * Enhanced post-login navigation that integrates with tutorial system
     * This method should replace the existing navigateToDashboard() call in LoginActivity
     */
    fun handlePostLoginNavigation(email: String) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Handling post-login navigation for: $email")
        }
        
        // Use the existing LoginTutorialIntegration
        loginTutorialIntegration.handlePostLoginNavigation(email)
    }
    
    /**
     * Alternative method that provides more control over the navigation process
     * This can be used if you need custom logic before navigation
     */
    fun handlePostLoginNavigationWithCallback(
        email: String,
        onNavigationReady: (shouldShowTutorial: Boolean, isFirstTimeUser: Boolean) -> Unit
    ) {
        // Record login events
        loginTutorialIntegration.recordUserAction("login_attempt")
        loginTutorialIntegration.recordUserAction("login_success")
        
        // Get tutorial decision
        val shouldShowTutorial = LoginTutorialIntegration.shouldShowTutorial(activity)
        val isFirstTimeUser = LoginTutorialIntegration.isFirstTimeUser(activity)
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            val analytics = loginTutorialIntegration.getUserAnalytics()
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Post-login analytics: $analytics")
        }
        
        // Callback with decision
        onNavigationReady(shouldShowTutorial, isFirstTimeUser)
    }
    
    /**
     * Creates the MainActivity intent with tutorial flags
     */
    fun createMainActivityIntent(shouldShowTutorial: Boolean, isFirstTimeUser: Boolean): Intent {
        return Intent(activity, MainActivity::class.java).apply {
            putExtra(LoginTutorialIntegration.EXTRA_SHOW_TUTORIAL, shouldShowTutorial)
            putExtra(LoginTutorialIntegration.EXTRA_IS_FIRST_TIME_USER, isFirstTimeUser)
            putExtra("login_source", "login_activity")
            putExtra("login_timestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Navigates to MainActivity with tutorial information and custom animation
     */
    fun navigateToMainActivityWithAnimation(
        shouldShowTutorial: Boolean,
        isFirstTimeUser: Boolean,
        sharedElementView: android.view.View? = null,
        transitionName: String? = null
    ) {
        val intent = createMainActivityIntent(shouldShowTutorial, isFirstTimeUser)
        
        if (sharedElementView != null && transitionName != null) {
            val options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                sharedElementView,
                transitionName
            )
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
        }
        
        activity.finish()
    }
    
    /**
     * Gets tutorial analytics for debugging
     */
    fun getTutorialAnalytics(): Map<String, Any> {
        return loginTutorialIntegration.getUserAnalytics()
    }
    
    /**
     * Forces tutorial eligibility (for testing)
     */
    fun forceTutorialEligibility() {
        loginTutorialIntegration.forceTutorialEligibility()
    }
    
    /**
     * Resets first-time user data (for testing)
     */
    fun resetFirstTimeUserData() {
        loginTutorialIntegration.resetFirstTimeUserData()
    }
    
    companion object {
        /**
         * Static method to check if tutorial should be shown
         */
        fun shouldShowTutorial(activity: Activity): Boolean {
            return LoginTutorialIntegration.shouldShowTutorial(activity)
        }
        
        /**
         * Static method to check if user is first-time
         */
        fun isFirstTimeUser(activity: Activity): Boolean {
            return LoginTutorialIntegration.isFirstTimeUser(activity)
        }
        
        /**
         * Creates integration instance
         */
        fun create(activity: Activity): LoginActivityIntegration {
            return LoginActivityIntegration(activity)
        }
    }
}