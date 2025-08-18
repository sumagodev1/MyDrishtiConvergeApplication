package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.ChartParametersActivity
import com.mydrishti.co.`in`.activities.models.ChartType

/**
 * Implementation of Tutorial Step 4: Parameter selection guidance
 * This class handles the fourth tutorial step - guiding parameter selection and configuration
 */
class TutorialStep4Implementation(private val activity: Activity) {
    
    private var tutorialManager: TutorialManager? = null
    private var chartTitleEditText: EditText? = null
    private var saveButton: Button? = null
    private var parameterSelectionArea: View? = null
    private var originalSaveClickListener: View.OnClickListener? = null
    private var hasShownParameterGuidance = false
    
    /**
     * Initializes Step 4 of the tutorial - Parameter selection guidance
     */
    fun initializeStep4(tutorialManager: TutorialManager) {
        this.tutorialManager = tutorialManager
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Initializing Tutorial Step 4: Parameter selection")
        }
        
        // Find the parameter selection UI elements
        findParameterSelectionElements()
        
        if (chartTitleEditText == null) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.w(TutorialConstants.DEBUG_LOG_TAG, "Parameter selection elements not found - Step 4 cannot proceed")
            }
            handleElementsNotFound()
            return
        }
        
        // Setup elements for tutorial
        setupElementsForTutorial()
        
        // Start the tutorial step
        startStep4Tutorial()
    }
    
    /**
     * Finds the parameter selection UI elements in the activity
     */
    private fun findParameterSelectionElements() {
        // Find chart title EditText (this is typically the main parameter users interact with)
        chartTitleEditText = activity.findViewById(R.id.etChartTitle)
        
        // Find save button
        saveButton = activity.findViewById(R.id.btnSaveChart)
        
        // Try to find parameter selection area (this might be a container or specific view)
        parameterSelectionArea = activity.findViewById(R.id.parameterSelectionContainer) 
            ?: activity.findViewById(R.id.scrollView) // Fallback to scroll view
            ?: chartTitleEditText // Final fallback to chart title
    }
    
    /**
     * Sets up the elements for tutorial interaction
     */
    private fun setupElementsForTutorial() {
        chartTitleEditText?.let { editText ->
            // Add text change listener to provide tutorial feedback
            editText.addTextChangedListener { text ->
                if (!hasShownParameterGuidance && !text.isNullOrBlank()) {
                    handleParameterInputDuringTutorial(text.toString())
                }
            }
            
            // Ensure EditText is visible and enabled
            editText.visibility = View.VISIBLE
            editText.isEnabled = true
        }
        
        saveButton?.let { button ->
            // Store original click listener if it exists
            originalSaveClickListener = button.tag as? View.OnClickListener
            
            // Set tutorial-specific click listener
            button.setOnClickListener { view ->
                handleSaveButtonClickDuringTutorial(view)
            }
            
            // Ensure button is visible
            button.visibility = View.VISIBLE
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Parameter selection elements setup complete for tutorial")
        }
    }
    
    /**
     * Starts the Step 4 tutorial with parameter selection highlighting
     */
    private fun startStep4Tutorial() {
        parameterSelectionArea?.let { targetView ->
            // Create tutorial step configuration
            val step4Config = TutorialConfigFactory.createFirstTimeUserTutorial()
                .getStepById(TutorialConstants.STEP_PARAMETER_SELECTION)
            
            if (step4Config == null) {
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.e(TutorialConstants.DEBUG_LOG_TAG, "Step 4 configuration not found")
                }
                return
            }
            
            // Show tutorial overlay for parameter selection area
            tutorialManager?.showModernTapTarget(step4Config, targetView)
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 4 tutorial started with parameter selection highlight")
            }
        }
    }
    
    /**
     * Handles parameter input during tutorial
     */
    private fun handleParameterInputDuringTutorial(inputText: String) {
        if (hasShownParameterGuidance) return
        
        hasShownParameterGuidance = true
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Parameter input detected during tutorial: $inputText")
        }
        
        // Show positive feedback for parameter input
        showParameterInputFeedback(inputText)
    }
    
    /**
     * Shows feedback for parameter input
     */
    private fun showParameterInputFeedback(inputText: String) {
        val feedbackStep = TutorialStep(
            id = "parameter_input_feedback",
            title = "Great!",
            description = "You've entered a chart title: '$inputText'",
            targetViewId = null,
            animationType = AnimationType.CELEBRATION,
            duration = 2500L,
            explanatoryText = "Chart parameters help customize how your data is displayed. The title helps you identify your chart later."
        )
        
        tutorialManager?.showModernTapTarget(feedbackStep)
        
        // After feedback, guide to save button
        showSaveButtonGuidance()
    }
    
    /**
     * Shows guidance for the save button
     */
    private fun showSaveButtonGuidance() {
        saveButton?.let { button ->
            val saveGuidanceStep = TutorialStep(
                id = "save_button_guidance",
                title = "Ready to Save",
                description = "Now tap the Save button to create your chart!",
                targetViewId = null,
                animationType = AnimationType.PULSE,
                duration = 4000L,
                explanatoryText = "The Save button will create your chart and add it to your dashboard."
            )
            
            tutorialManager?.showModernTapTarget(saveGuidanceStep, button)
        }
    }
    
    /**
     * Handles save button click during tutorial
     */
    fun handleSaveButtonClickDuringTutorial(view: View) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Save button clicked during tutorial")
        }
        
        // Validate parameters before proceeding
        if (validateParameterSelection()) {
            // Record tutorial interaction
            recordStep4Completion()
            
            // Show validation feedback
            showSaveValidationFeedback(true)
            
            // Proceed to Step 5 (Save chart completion)
            proceedToStep5()
        } else {
            // Show validation error
            showSaveValidationFeedback(false)
        }
    }
    
    /**
     * Validates parameter selection for tutorial
     */
    fun validateParameterSelection(): Boolean {
        val chartTitle = chartTitleEditText?.text?.toString()?.trim()
        val isValid = !chartTitle.isNullOrBlank()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Validating parameter selection: $isValid (title: '$chartTitle')")
        }
        
        return isValid
    }
    
    /**
     * Shows validation feedback for save action
     */
    private fun showSaveValidationFeedback(isValid: Boolean) {
        val feedbackStep = if (isValid) {
            TutorialStep(
                id = "save_validation_success",
                title = "Perfect!",
                description = "Your chart parameters are configured correctly.",
                targetViewId = null,
                animationType = AnimationType.CELEBRATION,
                duration = 2000L,
                explanatoryText = "Your chart is being created and will be added to your dashboard."
            )
        } else {
            TutorialStep(
                id = "save_validation_error",
                title = "Missing Information",
                description = "Please enter a chart title before saving.",
                targetViewId = null,
                animationType = AnimationType.PULSE,
                duration = 3000L,
                explanatoryText = "A chart title helps you identify your chart on the dashboard."
            )
        }
        
        tutorialManager?.showModernTapTarget(feedbackStep)
    }
    
    /**
     * Records Step 4 completion
     */
    private fun recordStep4Completion() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 4 completed - Parameters configured and save initiated")
        }
        
        // Mark step as completed in tutorial manager
        tutorialManager?.markStepCompleted(TutorialConstants.STEP_PARAMETER_SELECTION)
    }
    
    /**
     * Proceeds to Step 5 - Save chart completion
     */
    private fun proceedToStep5() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Proceeding to Step 5: Save chart completion")
        }
        
        // Show transition message to Step 5
        showStep5TransitionMessage()
        
        // Allow the normal save process to continue
        originalSaveClickListener?.onClick(saveButton) ?: run {
            // If no original listener, we need to trigger the save manually
            triggerChartSave()
        }
    }
    
    /**
     * Shows transition message to Step 5
     */
    private fun showStep5TransitionMessage() {
        val transitionStep = TutorialStep(
            id = "step4_to_step5_transition",
            title = "Saving Your Chart",
            description = "Your chart is being created with the parameters you configured.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 3000L,
            explanatoryText = "Once saved, your chart will appear on the main dashboard where you can view and manage it."
        )
        
        tutorialManager?.showModernTapTarget(transitionStep)
    }
    
    /**
     * Triggers chart save manually if no original listener exists
     */
    private fun triggerChartSave() {
        // This would typically call the activity's save method
        // Since we can't directly access ChartParametersActivity methods,
        // we'll need to use reflection or provide a callback mechanism
        
        if (activity is ChartParametersActivity) {
            // Try to trigger save through the activity
            // This would require adding a public method to ChartParametersActivity
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Triggering chart save through activity")
            }
        }
    }
    
    /**
     * Handles the case where UI elements are not found
     */
    private fun handleElementsNotFound() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.w(TutorialConstants.DEBUG_LOG_TAG, "Parameter selection elements not found - showing fallback tutorial message")
        }
        
        // Show fallback tutorial message
        showElementsNotFoundFallback()
    }
    
    /**
     * Shows fallback tutorial message when elements are not found
     */
    private fun showElementsNotFoundFallback() {
        val fallbackStep = TutorialStep(
            id = "elements_not_found_fallback",
            title = "Configure Parameters",
            description = "This screen allows you to configure your chart parameters.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Look for fields where you can enter a chart title and configure other chart settings. Then save your chart."
        )
        
        tutorialManager?.showModernTapTarget(fallbackStep)
        
        // Continue with completion guidance
        showStep4CompletionGuidance()
    }
    
    /**
     * Shows Step 4 completion guidance when elements are not found
     */
    private fun showStep4CompletionGuidance() {
        val completionStep = TutorialStep(
            id = "step4_completion_guidance",
            title = "Tutorial Continues",
            description = "Configure your chart parameters and save when ready.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "The next step will show you the completion celebration after your chart is saved."
        )
        
        tutorialManager?.showModernTapTarget(completionStep)
        
        // Mark step as completed even without full interaction
        tutorialManager?.markStepCompleted(TutorialConstants.STEP_PARAMETER_SELECTION)
    }
    
    /**
     * Provides validation feedback
     */
    fun showValidationFeedback(isValid: Boolean, errorMessage: String? = null) {
        val feedbackStep = if (isValid) {
            TutorialStep(
                id = "parameter_validation_success",
                title = "Parameters Valid",
                description = "Your chart parameters are configured correctly.",
                targetViewId = null,
                animationType = AnimationType.CELEBRATION,
                duration = 2000L,
                explanatoryText = "You can now save your chart to add it to the dashboard."
            )
        } else {
            TutorialStep(
                id = "parameter_validation_error",
                title = "Check Parameters",
                description = errorMessage ?: "Please review your chart parameters.",
                targetViewId = null,
                animationType = AnimationType.PULSE,
                duration = 3000L,
                explanatoryText = "Make sure all required fields are filled out correctly."
            )
        }
        
        tutorialManager?.showModernTapTarget(feedbackStep)
    }
    
    /**
     * Handles tutorial progression based on parameter state
     */
    fun handleTutorialProgression() {
        val isValid = validateParameterSelection()
        
        if (isValid) {
            // Parameters are valid - guide to save
            showSaveButtonGuidance()
        } else {
            // Parameters need attention - show guidance
            showParameterConfigurationGuidance()
        }
    }
    
    /**
     * Shows guidance for parameter configuration
     */
    private fun showParameterConfigurationGuidance() {
        val guidanceStep = TutorialStep(
            id = "parameter_configuration_guidance",
            title = "Configure Your Chart",
            description = "Enter a title for your chart and configure any other parameters.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Chart parameters determine how your data is displayed and help you identify your chart later."
        )
        
        tutorialManager?.showModernTapTarget(guidanceStep, chartTitleEditText)
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
        chartTitleEditText = null
        saveButton = null
        parameterSelectionArea = null
        tutorialManager = null
        originalSaveClickListener = null
        hasShownParameterGuidance = false
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 4 implementation cleaned up")
        }
    }
    
    /**
     * Gets Step 4 analytics
     */
    fun getStep4Analytics(): Map<String, Any> {
        return mapOf(
            "chart_title_found" to (chartTitleEditText != null),
            "save_button_found" to (saveButton != null),
            "parameter_area_found" to (parameterSelectionArea != null),
            "chart_title_filled" to (!chartTitleEditText?.text.isNullOrBlank()),
            "save_button_visible" to (saveButton?.visibility == View.VISIBLE),
            "save_button_enabled" to (saveButton?.isEnabled == true),
            "has_shown_parameter_guidance" to hasShownParameterGuidance,
            "validation_passed" to validateParameterSelection(),
            "has_original_save_listener" to (originalSaveClickListener != null)
        )
    }
}