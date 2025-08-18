package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.util.Log

/**
 * Detects first-time users and determines tutorial eligibility
 */
class FirstTimeUserDetector(private val context: Context) {
    
    private val tutorialStateManager = TutorialStateManager(context)
    
    /**
     * Checks if the current user is a first-time user
     */
    fun isFirstTimeUser(): Boolean {
        val isFirstTime = tutorialStateManager.isFirstTimeUser()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "First time user check: $isFirstTime")
        }
        
        return isFirstTime
    }
    
    /**
     * Checks if the tutorial should be triggered after login
     */
    fun shouldTriggerTutorial(): Boolean {
        val shouldTrigger = tutorialStateManager.shouldShowTutorial()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Should trigger tutorial: $shouldTrigger")
        }
        
        return shouldTrigger
    }
    
    /**
     * Handles first-time user detection after successful login
     */
    fun handlePostLoginDetection(): FirstTimeUserResult {
        val isFirstTime = isFirstTimeUser()
        val shouldTrigger = shouldTriggerTutorial()
        
        val result = FirstTimeUserResult(
            isFirstTimeUser = isFirstTime,
            shouldShowTutorial = shouldTrigger,
            tutorialEligible = isFirstTime && shouldTrigger,
            detectionTimestamp = System.currentTimeMillis()
        )
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Post-login detection result: $result")
        }
        
        return result
    }
    
    /**
     * Marks the user as having completed the first login
     */
    fun markFirstLoginCompleted() {
        // This could be used to track first login separately from tutorial completion
        val sharedPrefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putBoolean("first_login_completed", true)
            .putLong("first_login_timestamp", System.currentTimeMillis())
            .apply()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "First login marked as completed")
        }
    }
    
    /**
     * Checks if this is the very first login (different from first-time user)
     */
    fun isFirstLogin(): Boolean {
        val sharedPrefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        return !sharedPrefs.getBoolean("first_login_completed", false)
    }
    
    /**
     * Gets user onboarding status
     */
    fun getUserOnboardingStatus(): UserOnboardingStatus {
        val isFirstTime = isFirstTimeUser()
        val isFirstLogin = isFirstLogin()
        val tutorialCompleted = tutorialStateManager.isTutorialCompleted()
        val tutorialSkipped = tutorialStateManager.isTutorialSkipped()
        
        return UserOnboardingStatus(
            isFirstTimeUser = isFirstTime,
            isFirstLogin = isFirstLogin,
            tutorialCompleted = tutorialCompleted,
            tutorialSkipped = tutorialSkipped,
            needsOnboarding = isFirstTime && !tutorialCompleted && !tutorialSkipped
        )
    }
    
    /**
     * Validates tutorial eligibility with additional checks
     */
    fun validateTutorialEligibility(): TutorialEligibilityResult {
        val onboardingStatus = getUserOnboardingStatus()
        val issues = mutableListOf<String>()
        
        // Check basic eligibility
        if (!onboardingStatus.isFirstTimeUser) {
            issues.add("User is not a first-time user")
        }
        
        if (onboardingStatus.tutorialCompleted) {
            issues.add("Tutorial already completed")
        }
        
        if (onboardingStatus.tutorialSkipped) {
            issues.add("Tutorial was previously skipped")
        }
        
        // Additional validation checks could go here
        // e.g., app version compatibility, device capabilities, etc.
        
        val isEligible = issues.isEmpty()
        
        return TutorialEligibilityResult(
            isEligible = isEligible,
            issues = issues,
            onboardingStatus = onboardingStatus,
            validationTimestamp = System.currentTimeMillis()
        )
    }
}

/**
 * Result of first-time user detection
 */
data class FirstTimeUserResult(
    val isFirstTimeUser: Boolean,
    val shouldShowTutorial: Boolean,
    val tutorialEligible: Boolean,
    val detectionTimestamp: Long
)

/**
 * User onboarding status
 */
data class UserOnboardingStatus(
    val isFirstTimeUser: Boolean,
    val isFirstLogin: Boolean,
    val tutorialCompleted: Boolean,
    val tutorialSkipped: Boolean,
    val needsOnboarding: Boolean
)

/**
 * Tutorial eligibility validation result
 */
data class TutorialEligibilityResult(
    val isEligible: Boolean,
    val issues: List<String>,
    val onboardingStatus: UserOnboardingStatus,
    val validationTimestamp: Long
)