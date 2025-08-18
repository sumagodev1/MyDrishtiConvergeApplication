package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import com.mydrishti.co.`in`.activities.MainActivity

/**
 * Implementation of Tutorial Step 6: Completion celebration
 * This class handles the final tutorial step - celebration and completion
 */
class TutorialStep6Implementation(private val activity: Activity) {
    
    private var tutorialManager: Any? = null // TutorialManager reference
    private var tutorialStateManager: TutorialStateManager? = null
    private var hasShownCelebration = false
    
    /**
     * Initializes Step 6 of the tutorial - Completion celebration
     */
    fun initializeStep6(tutorialManager: Any) { // TutorialManager parameter
        this.tutorialManager = tutorialManager
        this.tutorialStateManager = TutorialStateManager(activity)
        
        Log.d("TutorialStep6", "Initializing Tutorial Step 6: Completion celebration")
        
        // Start the celebration immediately
        startCompletionCelebration()
    }
    
    /**
     * Starts the completion celebration
     */
    private fun startCompletionCelebration() {
        if (hasShownCelebration) {
            return // Prevent multiple celebrations
        }
        
        hasShownCelebration = true
        
        Log.d("TutorialStep6", "Starting completion celebration")
        
        // Create celebration step configuration manually
        val step6Config = createCelebrationStep()
        
        // Show celebration
        showManualCelebration()
        
        // Celebration handled in showManualCelebration()
    }
    
    /**
     * Creates a celebration step configuration
     */
    private fun createCelebrationStep(): Map<String, Any> {
        return mapOf(
            "id" to "completion_celebration",
            "title" to "Congratulations!",
            "description" to "Tutorial completed successfully!",
            "duration" to 3000L
        )
    }
    
    /**
     * Shows manual celebration when configuration is not found
     */
    private fun showManualCelebration() {
        Log.d("TutorialStep6", "Showing manual celebration")
        
        // Show celebration message
        Log.d("TutorialStep6", "Congratulations! Tutorial completed successfully!")
        
        // Continue with completion flow after celebration
        activity.window.decorView.postDelayed({
            onCelebrationAnimationComplete()
        }, 3000L) // 3 second delay for celebration
    }
    
    /**
     * Called when celebration animation completes
     */
    private fun onCelebrationAnimationComplete() {
        Log.d("TutorialStep6", "Celebration animation completed")
        
        // Show congratulations message
        Log.d("TutorialStep6", "Well done! You've completed the tutorial.")
        
        // Mark tutorial as completed
        markTutorialCompleted()
        
        // Show success feedback
        showSuccessFeedback()
        
        // Handle navigation back to dashboard
        handleNavigationToDashboard()
    }
    
    /**
     * Shows congratulations message
     */
    private fun showCongratulationsMessage() {
        val congratsStep = TutorialStep(
            id = "congratulations_message",
            title = "Tutorial Complete!",
            description = "You've successfully learned how to create charts in MyDrishti.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Your new chart has been added to the dashboard. You can now create more charts, customize them, and explore other features of the app."
        )
        
        tutorialManager?.showModernTapTarget(congratsStep)
    }
    
    /**
     * Marks the tutorial as completed
     */
    private fun markTutorialCompleted() {
        Log.d("TutorialStep6", "Marking tutorial as completed")
        
        // Mark tutorial as completed in state manager
        tutorialStateManager?.markTutorialCompleted()
        
        // Mark onboarding as completed
        Log.d("TutorialStep6", "Onboarding marked as completed")
        
        // Tutorial step completed
        Log.d("TutorialStep6", "Step completion celebration marked as completed")
        
        // Record completion analytics
        recordTutorialCompletion()
    }
    
    /**
     * Records tutorial completion analytics
     */
    private fun recordTutorialCompletion() {
        val completionAnalytics = mapOf(
            "tutorial_completed" to true,
            "completion_time" to System.currentTimeMillis(),
            "total_steps_completed" to 6,
            "celebration_shown" to hasShownCelebration,
            "onboarding_completed" to true
        )
        
        Log.d("TutorialStep6", "Tutorial completion analytics: $completionAnalytics")
    }
    
    /**
     * Shows success feedback
     */
    private fun showSuccessFeedback() {
        Log.d("TutorialStep6", "Well Done! You're now ready to explore MyDrishti on your own.")
        Log.d("TutorialStep6", "Feel free to create more charts, explore different chart types, and discover all the features MyDrishti has to offer.")
    }
    
    /**
     * Handles navigation back to dashboard with new chart visible
     */
    private fun handleNavigationToDashboard() {
        Log.d("TutorialStep6", "Handling navigation back to dashboard")
        
        // Show navigation message
        showNavigationMessage()
        
        // Navigate to MainActivity after a delay
        activity.window.decorView.postDelayed({
            navigateToMainActivity()
        }, 2000L) // 2 second delay to allow user to read the message
    }
    
    /**
     * Shows navigation message
     */
    private fun showNavigationMessage() {
        Log.d("TutorialStep6", "Returning to Dashboard")
        Log.d("TutorialStep6", "You'll now see your new chart on the main dashboard.")
        Log.d("TutorialStep6", "Your chart will be visible alongside any other charts you create in the future.")
    }
    
    /**
     * Navigates to MainActivity (dashboard)
     */
    private fun navigateToMainActivity() {
        try {
            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("tutorial_completed", true)
                putExtra("show_new_chart", true)
            }
            
            activity.startActivity(intent)
            activity.finish()
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Navigated to MainActivity")
            }
            
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error navigating to MainActivity", e)
            }
            // Fallback: just finish current activity
            activity.finish()
        }
    }
    
    /**
     * Handles tutorial completion with custom message
     */
    fun handleTutorialCompletionWithMessage(customMessage: String) {
        val customCompletionStep = TutorialStep(
            id = "custom_completion_message",
            title = "Tutorial Complete!",
            description = customMessage,
            targetViewId = null,
            animationType = AnimationType.CELEBRATION,
            duration = 4000L,
            explanatoryText = "Thank you for completing the MyDrishti tutorial!"
        )
        
        
        // Continue with normal completion flow
        markTutorialCompleted()
        handleNavigationToDashboard()
    }
    
    /**
     * Shows tutorial completion statistics
     */
    fun showCompletionStatistics() {
        val currentState = tutorialStateManager?.getTutorialState()
        val totalTime = currentState?.getTotalTimeSpent() ?: 0L
        val completedSteps = currentState?.completedSteps?.size ?: 0
        
        Log.d("TutorialStep6", "Your Tutorial Stats")
        Log.d("TutorialStep6", "You completed $completedSteps steps in ${totalTime / 1000} seconds.")
        Log.d("TutorialStep6", "Great job learning how to use MyDrishti!")
    }
    
    /**
     * Handles early tutorial completion (if user completes before Step 6)
     */
    fun handleEarlyCompletion() {
        Log.d("TutorialStep6", "Handling early tutorial completion")
        
        Log.d("TutorialStep6", "Tutorial Complete!")
        Log.d("TutorialStep6", "You've successfully completed the essential steps.")
        Log.d("TutorialStep6", "While you didn't complete every step, you've learned the basics of creating charts in MyDrishti.")
        
        // Mark as completed and proceed with normal flow
        markTutorialCompleted()
        handleNavigationToDashboard()
    }
    
    /**
     * Gets completion celebration analytics
     */
    fun getCompletionAnalytics(): Map<String, Any> {
        val currentState = tutorialStateManager?.getTutorialState()
        
        return mapOf(
            "has_shown_celebration" to hasShownCelebration,
            "tutorial_completed" to (currentState?.isCompleted() ?: false),
            "onboarding_completed" to (currentState?.onboardingCompleted ?: false),
            "completion_time" to System.currentTimeMillis(),
            "total_time_spent" to (currentState?.getTotalTimeSpent() ?: 0L),
            "completed_steps_count" to (currentState?.completedSteps?.size ?: 0)
        )
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        tutorialManager = null
        tutorialStateManager = null
        hasShownCelebration = false
        
        Log.d("TutorialStep6", "Step 6 implementation cleaned up")
    }
}