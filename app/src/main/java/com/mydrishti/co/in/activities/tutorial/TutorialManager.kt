package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Core tutorial management class that orchestrates the complete tutorial experience
 */
class TutorialManager(private val activity: Activity) : DefaultLifecycleObserver {
    
    private val context: Context = activity
    private val tutorialStateManager = TutorialStateManager(context)
    private val animationController = TutorialAnimationController(context)
    private val firstTimeUserDetector = FirstTimeUserDetector(context)
    private val skipManager = TutorialSkipManager(context)
    private val restartManager = TutorialRestartManager(context)
    
    private var currentTutorialConfig: TutorialConfig? = null
    private var currentTutorialOverlay: TutorialOverlay? = null
    private var isActive = false
    private var currentStepIndex = 0
    
    // Lifecycle management
    init {
        // Lifecycle will be managed by the activity
    }
    
    /**
     * Initiates the complete tutorial flow for first-time users
     */
    fun startFirstTimeUserTutorial(): Boolean {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Starting first-time user tutorial")
        }
        
        // Check if tutorial should be shown
        if (!firstTimeUserDetector.shouldTriggerTutorial()) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial not eligible for this user")
            }
            return false
        }
        
        // Load tutorial configuration
        currentTutorialConfig = TutorialConfigFactory.createFirstTimeUserTutorial()
        
        if (currentTutorialConfig?.isValid() != true) {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, "Invalid tutorial configuration")
            return false
        }
        
        // Initialize tutorial state
        isActive = true
        currentStepIndex = 0
        
        // Mark first login completed
        firstTimeUserDetector.markFirstLoginCompleted()
        
        // Start with first step
        return showCurrentStep()
    }
    
    /**
     * Shows the current tutorial step
     */
    private fun showCurrentStep(): Boolean {
        val config = currentTutorialConfig ?: return false
        
        if (currentStepIndex >= config.steps.size) {
            completeTutorial()
            return false
        }
        
        val currentStep = config.steps[currentStepIndex]
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Showing step: ${currentStep.id}")
        }
        
        // Update state - step will be marked completed when user interacts
        
        // Create and show tutorial overlay
        return showModernTapTarget(currentStep)
    }
    
    /**
     * Creates enhanced TapTargetView with modern animations
     */
    fun showModernTapTarget(step: TutorialStep): Boolean {
        try {
            // Find target view if specified
            val targetView = step.targetViewId?.let { viewId ->
                findViewByIdString(viewId)
            }
            
            return showModernTapTarget(step, targetView)
            
        } catch (e: Exception) {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error showing tutorial step", e)
            return false
        }
    }
    
    /**
     * Creates enhanced TapTargetView with modern animations and custom target view
     */
    fun showModernTapTarget(step: TutorialStep, customTargetView: View?): Boolean {
        try {
            // Create tutorial overlay
            currentTutorialOverlay = TutorialOverlay(
                context = context,
                step = step,
                targetView = customTargetView,
                animationController = animationController,
                onStepCompleted = { handleStepCompleted(step) },
                onTutorialSkipped = { handleTutorialSkipped() },
                onTutorialDismissed = { handleTutorialDismissed() }
            )
            
            // Add overlay to activity
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(currentTutorialOverlay)
            
            // Start step animation
            val animation = animationController.createAnimation(
                currentTutorialOverlay!!,
                step.animationType,
                step.duration
            )
            animationController.startAnimation(animation)
            
            return true
            
        } catch (e: Exception) {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error showing tutorial step with custom target", e)
            return false
        }
    }
    
    /**
     * Handles step completion and progression
     */
    private fun handleStepCompleted(step: TutorialStep) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step completed: ${step.id}")
        }
        
        // Mark step as completed
        tutorialStateManager.markStepCompleted(step.id)
        
        // Remove current overlay
        removeCurrentOverlay()
        
        // Move to next step
        currentStepIndex++
        
        // Show next step or complete tutorial
        if (currentStepIndex < (currentTutorialConfig?.steps?.size ?: 0)) {
            showCurrentStep()
        } else {
            completeTutorial()
        }
    }
    
    /**
     * Handles tutorial skip with enhanced confirmation flow
     */
    private fun handleTutorialSkipped() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial skip requested by user")
        }
        
        val config = currentTutorialConfig ?: return
        val currentStep = config.steps.getOrNull(currentStepIndex) ?: return
        
        // Show skip confirmation
        skipManager.showSkipConfirmation(activity) { confirmed ->
            if (confirmed) {
                skipTutorial()
            }
        }
    }
    
    /**
     * Skips the tutorial
     */
    private fun skipTutorial() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial skipped")
        }
        
        tutorialStateManager.markTutorialSkipped()
        cleanup()
    }
    
    /**
     * Handles tutorial dismissal (accidental)
     */
    private fun handleTutorialDismissed() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial dismissed")
        }
        
        // For accidental dismissal, we might want to show a recovery option
        // For now, just pause the tutorial
        pauseTutorial()
    }
    
    /**
     * Completes the tutorial successfully
     */
    private fun completeTutorial() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial completed successfully")
        }
        
        // Show completion celebration
        showCompletionCelebration {
            // Mark tutorial as completed
            tutorialStateManager.markTutorialCompleted()
            
            cleanup()
        }
    }
    
    /**
     * Shows completion celebration animation
     */
    private fun showCompletionCelebration(onComplete: () -> Unit) {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        
        val celebrationAnimation = animationController.createCelebrationAnimation(rootView)
        animationController.startAnimation(celebrationAnimation, onEnd = onComplete)
    }
    
    /**
     * Public helper to trigger the celebration animation for external callers
     */
    fun triggerCelebrationAnimation(rootView: ViewGroup, onComplete: () -> Unit) {
        val celebrationAnimation = animationController.createCelebrationAnimation(rootView)
        animationController.startAnimation(celebrationAnimation, onEnd = onComplete)
    }
    
    /**
     * Pauses the tutorial
     */
    fun pauseTutorial() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial paused")
        }
        
        animationController.cancelAllAnimations()
        removeCurrentOverlay()
    }
    
    /**
     * Resumes the tutorial
     */
    fun resumeTutorial() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial resumed")
        }
        
        // Resume by showing current step again if needed
        
        // Restore current step if tutorial was active
        if (isActive) {
            showCurrentStep()
        }
    }
    
    /**
     * Restarts the tutorial from the beginning with enhanced confirmation
     */
    fun restartTutorial() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial restart requested")
        }
        
        restartManager.showRestartConfirmation(activity) { confirmed ->
            if (confirmed) {
                performTutorialRestart()
            }
        }
    }
    
    /**
     * Performs the actual tutorial restart
     */
    private fun performTutorialRestart() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Performing tutorial restart")
        }
        
        // Cleanup current tutorial
        cleanup()
        
        // Reset tutorial state
        tutorialStateManager.resetTutorialState()
        
        // Start tutorial again
        startFirstTimeUserTutorial()
    }
    
    /**
     * Finds a view by its string ID
     */
    private fun findViewByIdString(viewId: String): View? {
        return try {
            val resourceId = context.resources.getIdentifier(viewId, "id", context.packageName)
            if (resourceId != 0) {
                activity.findViewById(resourceId)
            } else {
                null
            }
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.w(TutorialConstants.DEBUG_LOG_TAG, "View not found: $viewId", e)
            }
            null
        }
    }
    
    /**
     * Removes the current tutorial overlay
     */
    private fun removeCurrentOverlay() {
        currentTutorialOverlay?.let { overlay ->
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.removeView(overlay)
            currentTutorialOverlay = null
        }
    }
    
    /**
     * Gets the current tutorial state
     */
    fun getCurrentState(): TutorialState {
        return tutorialStateManager.getTutorialState()
    }
    
    /**
     * Gets the current tutorial step
     */
    fun getCurrentStep(): TutorialStep? {
        val config = currentTutorialConfig ?: return null
        return if (currentStepIndex < config.steps.size) {
            config.steps[currentStepIndex]
        } else {
            null
        }
    }
    
    /**
     * Marks a specific step as completed
     */
    fun markStepCompleted(stepId: String) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Marking step completed: $stepId")
        }
        
        tutorialStateManager.markStepCompleted(stepId)
    }
    
    /**
     * Checks if tutorial is currently active
     */
    fun isActive(): Boolean {
        return isActive
    }
    
    /**
     * Gets tutorial analytics
     */
    fun getAnalytics(): Map<String, Any> {
        return mapOf(
            "is_active" to isActive,
            "current_step_index" to currentStepIndex,
            "total_steps" to (currentTutorialConfig?.steps?.size ?: 0),
            "has_running_animations" to animationController.hasRunningAnimations(),
            "active_animation_count" to animationController.getActiveAnimationCount()
        )
    }
    
    /**
     * Enables debug mode for development
     */
    fun enableDebugMode(enabled: Boolean) {
        // This would typically update a debug flag
        if (enabled && TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Debug mode enabled")
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Current analytics: ${getAnalytics()}")
        }
    }
    
    /**
     * Handles activity lifecycle events
     */
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        if (isActive) {
            pauseTutorial()
        }
    }
    
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (isActive) {
            resumeTutorial()
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        cleanup()
    }
    
    /**
     * Cleanup method for proper resource management
     */
    private fun cleanup() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Cleaning up TutorialManager")
        }
        
        isActive = false
        currentStepIndex = 0
        
        removeCurrentOverlay()
        animationController.cleanup()
        skipManager.cleanup()
        restartManager.cleanup()
        
        // Lifecycle cleanup handled by activity
    }
    
    companion object {
        /**
         * Creates a TutorialManager instance for the given activity
         */
        fun create(activity: Activity): TutorialManager {
            return TutorialManager(activity)
        }
        
        /**
         * Checks if tutorial should be shown for the given activity
         */
        fun shouldShowTutorial(activity: Activity): Boolean {
            val detector = FirstTimeUserDetector(activity)
            return detector.shouldTriggerTutorial()
        }
    }
}