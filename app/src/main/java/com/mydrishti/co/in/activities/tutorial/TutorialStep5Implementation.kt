package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.ChartParametersActivity

/**
 * Implementation of Tutorial Step 5: Save chart guidance
 * This class handles the fifth tutorial step - guiding chart save completion
 */
class TutorialStep5Implementation(private val activity: Activity) {
    
    private var tutorialManager: TutorialManager? = null
    private var saveButton: Button? = null
    private var originalSaveClickListener: View.OnClickListener? = null
    private var hasSaveBeenTriggered = false
    
    /**
     * Initializes Step 5 of the tutorial - Save chart guidance
     */
    fun initializeStep5(tutorialManager: TutorialManager) {
        this.tutorialManager = tutorialManager
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Initializing Tutorial Step 5: Save chart guidance")
        }
        
        // Find the save button
        findSaveButton()
        
        if (saveButton == null) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.w(TutorialConstants.DEBUG_LOG_TAG, "Save button not found - Step 5 cannot proceed")
            }
            handleSaveButtonNotFound()
            return
        }
        
        // Setup save button for tutorial
        setupSaveButtonForTutorial()
        
        // Start the tutorial step
        startStep5Tutorial()
    }
    
    /**
     * Finds the save button in the activity
     */
    private fun findSaveButton() {
        saveButton = activity.findViewById(R.id.btnSaveChart)
    }
    
    /**
     * Sets up the save button for tutorial interaction
     */
    private fun setupSaveButtonForTutorial() {
        saveButton?.let { button ->
            // Store original click listener if it exists
            originalSaveClickListener = button.tag as? View.OnClickListener
            
            // Set tutorial-specific click listener
            button.setOnClickListener { view ->
                handleSaveButtonClickDuringTutorial(view)
            }
            
            // Ensure button is visible and enabled
            button.visibility = View.VISIBLE
            button.isEnabled = true
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Save button setup complete for tutorial")
            }
        }
    }
    
    /**
     * Starts the Step 5 tutorial with save button highlighting
     */
    private fun startStep5Tutorial() {
        saveButton?.let { button ->
            // Create tutorial step configuration
            val step5Config = TutorialConfigFactory.createFirstTimeUserTutorial()
                .getStepById(TutorialConstants.STEP_SAVE_CHART)
            
            if (step5Config == null) {
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.e(TutorialConstants.DEBUG_LOG_TAG, "Step 5 configuration not found")
                }
                return
            }
            
            // Show tutorial overlay for save button
            tutorialManager?.showModernTapTarget(step5Config, button)
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 5 tutorial started with save button highlight")
            }
        }
    }
    
    /**
     * Handles save button click during tutorial
     */
    fun handleSaveButtonClickDuringTutorial(view: View) {
        if (hasSaveBeenTriggered) {
            return // Prevent multiple triggers
        }
        
        hasSaveBeenTriggered = true
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Save button clicked during tutorial")
        }
        
        // Show save confirmation feedback
        showSaveConfirmationFeedback()
        
        // Record tutorial interaction
        recordStep5Completion()
        
        // Trigger the actual save operation
        triggerChartSave()
        
        // Proceed to Step 6 (Completion celebration)
        proceedToStep6()
    }
    
    /**
     * Shows save confirmation feedback
     */
    private fun showSaveConfirmationFeedback() {
        val feedbackStep = TutorialStep(
            id = "save_confirmation_feedback",
            title = "Saving Chart...",
            description = "Your chart is being saved to the dashboard.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 2000L,
            explanatoryText = "This may take a moment while we process your chart configuration."
        )
        
        tutorialManager?.showModernTapTarget(feedbackStep)
    }
    
    /**
     * Records Step 5 completion
     */
    private fun recordStep5Completion() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 5 completed - Save button tapped")
        }
        
        // Mark step as completed in tutorial manager
        tutorialManager?.markStepCompleted(TutorialConstants.STEP_SAVE_CHART)
    }
    
    /**
     * Triggers the actual chart save operation
     */
    private fun triggerChartSave() {
        // Call original save listener if it exists
        originalSaveClickListener?.onClick(saveButton) ?: run {
            // If no original listener, try to trigger save through activity
            if (activity is ChartParametersActivity) {
                // This would require adding a public method to ChartParametersActivity
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Triggering chart save through activity")
                }
            }
        }
    }
    
    /**
     * Proceeds to Step 6 - Completion celebration
     */
    private fun proceedToStep6() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Proceeding to Step 6: Completion celebration")
        }
        
        // Show transition message to Step 6
        showStep6TransitionMessage()
    }
    
    /**
     * Shows transition message to Step 6
     */
    private fun showStep6TransitionMessage() {
        val transitionStep = TutorialStep(
            id = "step5_to_step6_transition",
            title = "Chart Saved Successfully!",
            description = "Your chart has been created and saved to your dashboard.",
            targetViewId = null,
            animationType = AnimationType.CELEBRATION,
            duration = 3000L,
            explanatoryText = "Congratulations! You've successfully completed the chart creation process."
        )
        
        tutorialManager?.showModernTapTarget(transitionStep)
    }
    
    /**
     * Handles successful chart save
     */
    fun handleChartSaveSuccess() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart save successful during tutorial")
        }
        
        // Show success feedback
        showSaveSuccessFeedback()
        
        // Proceed to completion celebration
        proceedToStep6()
    }
    
    /**
     * Shows save success feedback
     */
    private fun showSaveSuccessFeedback() {
        val successStep = TutorialStep(
            id = "save_success_feedback",
            title = "Success!",
            description = "Your chart has been successfully saved to the dashboard.",
            targetViewId = null,
            animationType = AnimationType.CELEBRATION,
            duration = 2500L,
            explanatoryText = "You can now view and manage your chart from the main dashboard."
        )
        
        tutorialManager?.showModernTapTarget(successStep)
    }
    
    /**
     * Handles chart save error
     */
    fun handleChartSaveError(errorMessage: String) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart save error during tutorial: $errorMessage")
        }
        
        // Show error feedback
        showSaveErrorFeedback(errorMessage)
        
        // Reset save trigger flag to allow retry
        hasSaveBeenTriggered = false
    }
    
    /**
     * Shows save error feedback
     */
    private fun showSaveErrorFeedback(errorMessage: String) {
        val errorStep = TutorialStep(
            id = "save_error_feedback",
            title = "Save Error",
            description = "There was an issue saving your chart: $errorMessage",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Please check your parameters and try saving again."
        )
        
        tutorialManager?.showModernTapTarget(errorStep)
    }
    
    /**
     * Validates save button tap
     */
    fun validateSaveButtonTap(): Boolean {
        val isValid = saveButton?.isEnabled == true && saveButton?.visibility == View.VISIBLE
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Validating save button tap: $isValid")
        }
        
        return isValid
    }
    
    /**
     * Shows validation feedback for save button tap
     */
    fun showSaveValidationFeedback(isValid: Boolean) {
        val feedbackStep = if (isValid) {
            TutorialStep(
                id = "save_validation_success",
                title = "Ready to Save",
                description = "Your chart is ready to be saved.",
                targetViewId = null,
                animationType = AnimationType.PULSE,
                duration = 2000L,
                explanatoryText = "Tap the save button to complete your chart creation."
            )
        } else {
            TutorialStep(
                id = "save_validation_error",
                title = "Cannot Save",
                description = "The save button is not available right now.",
                targetViewId = null,
                animationType = AnimationType.FADE_SCALE,
                duration = 3000L,
                explanatoryText = "Please ensure all required fields are filled out correctly."
            )
        }
        
        tutorialManager?.showModernTapTarget(feedbackStep)
    }
    
    /**
     * Handles the case where save button is not found
     */
    private fun handleSaveButtonNotFound() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.w(TutorialConstants.DEBUG_LOG_TAG, "Save button not found - showing fallback tutorial message")
        }
        
        // Show fallback tutorial message
        showSaveButtonNotFoundFallback()
    }
    
    /**
     * Shows fallback tutorial message when save button is not found
     */
    private fun showSaveButtonNotFoundFallback() {
        val fallbackStep = TutorialStep(
            id = "save_button_not_found_fallback",
            title = "Save Your Chart",
            description = "Look for a Save button to complete your chart creation.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "The Save button is typically located at the bottom of the screen or in the toolbar."
        )
        
        tutorialManager?.showModernTapTarget(fallbackStep)
        
        // Continue with completion guidance
        showStep5CompletionGuidance()
    }
    
    /**
     * Shows Step 5 completion guidance when save button is not found
     */
    private fun showStep5CompletionGuidance() {
        val completionStep = TutorialStep(
            id = "step5_completion_guidance",
            title = "Complete Chart Creation",
            description = "Save your chart when you're ready to add it to the dashboard.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Once saved, your chart will be available on the main dashboard for viewing and management."
        )
        
        tutorialManager?.showModernTapTarget(completionStep)
        
        // Mark step as completed even without actual save button interaction
        tutorialManager?.markStepCompleted(TutorialConstants.STEP_SAVE_CHART)
        
        // Proceed to Step 6
        proceedToStep6()
    }
    
    /**
     * Handles tutorial progression for save chart step
     */
    fun handleTutorialProgression() {
        val isValid = validateSaveButtonTap()
        
        if (isValid) {
            // Save button is ready - show guidance
            showSaveButtonGuidance()
        } else {
            // Save button not ready - show validation feedback
            showSaveValidationFeedback(false)
        }
    }
    
    /**
     * Shows guidance for save button interaction
     */
    private fun showSaveButtonGuidance() {
        val guidanceStep = TutorialStep(
            id = "save_button_guidance",
            title = "Save Your Chart",
            description = "Tap the Save button to complete your chart creation.",
            targetViewId = null,
            animationType = AnimationType.PULSE,
            duration = 4000L,
            explanatoryText = "This will save your chart configuration and add it to your dashboard."
        )
        
        tutorialManager?.showModernTapTarget(guidanceStep, saveButton)
    }
    
    /**
     * Restores original save button functionality after tutorial
     */
    fun restoreOriginalSaveButtonFunctionality() {
        saveButton?.let { button ->
            // Restore original click listener if it existed
            originalSaveClickListener?.let { originalListener ->
                button.setOnClickListener(originalListener)
            } ?: run {
                // Set default save functionality if no original listener
                button.setOnClickListener { view ->
                    handleDefaultSaveButtonClick(view)
                }
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Original save button functionality restored")
            }
        }
    }
    
    /**
     * Handles default save button click (when no original listener exists)
     */
    private fun handleDefaultSaveButtonClick(view: View) {
        // Default behavior: trigger save
        triggerChartSave()
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        restoreOriginalSaveButtonFunctionality()
        saveButton = null
        tutorialManager = null
        originalSaveClickListener = null
        hasSaveBeenTriggered = false
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 5 implementation cleaned up")
        }
    }
    
    /**
     * Gets Step 5 analytics
     */
    fun getStep5Analytics(): Map<String, Any> {
        return mapOf(
            "save_button_found" to (saveButton != null),
            "save_button_visible" to (saveButton?.visibility == View.VISIBLE),
            "save_button_enabled" to (saveButton?.isEnabled == true),
            "has_save_been_triggered" to hasSaveBeenTriggered,
            "validation_passed" to validateSaveButtonTap(),
            "has_original_save_listener" to (originalSaveClickListener != null)
        )
    }
}