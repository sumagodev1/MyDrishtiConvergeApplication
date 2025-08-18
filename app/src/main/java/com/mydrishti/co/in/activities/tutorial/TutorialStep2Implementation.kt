package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.View
import com.google.android.material.card.MaterialCardView
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.dialogs.ChartTypeSelectionDialog
import com.mydrishti.co.`in`.activities.models.ChartType

/**
 * Implementation of Tutorial Step 2: Chart type selection guidance
 * This class handles the second tutorial step - highlighting the gauge chart option
 */
class TutorialStep2Implementation(private val activity: Activity) {
    
    private var chartTypeDialog: ChartTypeSelectionDialog? = null
    private var tutorialManager: TutorialManager? = null
    private var gaugeChartCard: MaterialCardView? = null
    private var originalGaugeClickListener: View.OnClickListener? = null
    
    /**
     * Initializes Step 2 of the tutorial - Chart type selection guidance
     */
    fun initializeStep2(tutorialManager: TutorialManager) {
        this.tutorialManager = tutorialManager
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Initializing Tutorial Step 2: Chart type selection")
        }
        
        // Show the chart type selection dialog with tutorial integration
        showChartTypeDialogWithTutorial()
    }
    
    /**
     * Shows the chart type selection dialog with tutorial integration
     */
    private fun showChartTypeDialogWithTutorial() {
        try {
            // Create the dialog with tutorial-aware callback
            chartTypeDialog = ChartTypeSelectionDialog(activity) { chartType ->
                handleChartTypeSelectionDuringTutorial(chartType)
            }
            
            // Show the dialog
            chartTypeDialog?.show()
            
            // Wait a moment for the dialog to be fully rendered, then start tutorial
            chartTypeDialog?.window?.decorView?.post {
                startStep2Tutorial()
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart type dialog shown with tutorial integration")
            }
            
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error showing chart type dialog", e)
            }
            handleDialogError()
        }
    }
    
    /**
     * Starts the Step 2 tutorial with gauge chart highlighting
     */
    private fun startStep2Tutorial() {
        // Find the gauge chart card in the dialog
        gaugeChartCard = chartTypeDialog?.findViewById(R.id.card_gauge_chart)
        
        if (gaugeChartCard == null) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.w(TutorialConstants.DEBUG_LOG_TAG, "Gauge chart card not found - Step 2 cannot proceed")
            }
            handleGaugeCardNotFound()
            return
        }
        
        // Setup gauge card for tutorial
        setupGaugeCardForTutorial()
        
        // Create tutorial step configuration
        val step2Config = TutorialConfigFactory.createFirstTimeUserTutorial()
            .getStepById(TutorialConstants.STEP_CHART_TYPE_SELECTION)
        
        if (step2Config == null) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Step 2 configuration not found")
            }
            return
        }
        
        // Show tutorial overlay for gauge chart card
        tutorialManager?.showModernTapTarget(step2Config, gaugeChartCard)
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 2 tutorial started with gauge chart highlight")
        }
    }
    
    /**
     * Sets up the gauge chart card for tutorial interaction
     */
    private fun setupGaugeCardForTutorial() {
        gaugeChartCard?.let { card ->
            // Store original click listener if it exists
            originalGaugeClickListener = card.tag as? View.OnClickListener
            
            // Set tutorial-specific click listener
            card.setOnClickListener { view ->
                handleGaugeCardClickDuringTutorial(view)
            }
            
            // Ensure card is visible and enabled
            card.visibility = View.VISIBLE
            card.isEnabled = true
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Gauge chart card setup complete for tutorial")
            }
        }
    }
    
    /**
     * Handles gauge chart card click during tutorial
     */
    fun handleGaugeCardClickDuringTutorial(view: View) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Gauge chart card clicked during tutorial")
        }
        
        // Record tutorial interaction
        recordStep2Completion()
        
        // Proceed with the chart type selection
        proceedWithGaugeChartSelection()
    }
    
    /**
     * Records Step 2 completion
     */
    private fun recordStep2Completion() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 2 completed - Gauge chart selected")
        }
        
        // Mark step as completed in tutorial manager
        tutorialManager?.markStepCompleted(TutorialConstants.STEP_CHART_TYPE_SELECTION)
    }
    
    /**
     * Proceeds with gauge chart selection and moves to Step 3
     */
    private fun proceedWithGaugeChartSelection() {
        // Close the dialog and proceed with gauge chart selection
        chartTypeDialog?.dismiss()
        
        // Proceed to Step 3 (Site Selection)
        proceedToStep3()
    }
    
    /**
     * Proceeds to Step 3 - Site Selection
     */
    private fun proceedToStep3() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Proceeding to Step 3: Site Selection")
        }
        
        // This would typically navigate to SiteSelectionActivity
        // For now, we'll show a transition message
        showStep3TransitionMessage()
    }
    
    /**
     * Shows transition message to Step 3
     */
    private fun showStep3TransitionMessage() {
        val transitionStep = TutorialStep(
            id = "step2_to_step3_transition",
            title = "Great Choice!",
            description = "Gauge charts are perfect for displaying single values. Now let's select a data source.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 3000L,
            explanatoryText = "Next, we'll help you choose where to get your data from."
        )
        
        tutorialManager?.showModernTapTarget(transitionStep)
    }
    
    /**
     * Handles chart type selection during tutorial (callback from dialog)
     */
    private fun handleChartTypeSelectionDuringTutorial(chartType: ChartType) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart type selected during tutorial: $chartType")
        }
        
        when (chartType) {
            ChartType.GAUGE -> {
                // This is the expected selection for the tutorial
                recordStep2Completion()
                proceedToStep3()
            }
            else -> {
                // User selected a different chart type
                handleNonGaugeSelection(chartType)
            }
        }
    }
    
    /**
     * Handles when user selects a non-gauge chart type during tutorial
     */
    private fun handleNonGaugeSelection(chartType: ChartType) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Non-gauge chart selected: $chartType")
        }
        
        // Show a gentle redirect message
        val redirectStep = TutorialStep(
            id = "gauge_chart_redirect",
            title = "Let's Try Gauge Charts",
            description = "For this tutorial, we recommend starting with gauge charts as they're easier to set up.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "You can explore other chart types after completing the tutorial!"
        )
        
        tutorialManager?.showModernTapTarget(redirectStep)
        
        // Continue with the selected chart type anyway
        recordStep2Completion()
        proceedToStep3()
    }
    
    /**
     * Handles the case where gauge chart card is not found
     */
    private fun handleGaugeCardNotFound() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.w(TutorialConstants.DEBUG_LOG_TAG, "Gauge chart card not found - showing fallback tutorial message")
        }
        
        // Show fallback tutorial message
        showFallbackTutorialMessage()
    }
    
    /**
     * Shows fallback tutorial message when gauge card is not found
     */
    private fun showFallbackTutorialMessage() {
        val fallbackStep = TutorialStep(
            id = "fallback_chart_selection",
            title = "Choose Chart Type",
            description = "Look for the Gauge Chart option - it's perfect for beginners!",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Gauge charts show single values in an easy-to-read format"
        )
        
        tutorialManager?.showModernTapTarget(fallbackStep)
    }
    
    /**
     * Handles dialog opening errors
     */
    private fun handleDialogError() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, "Chart Type Selection Dialog failed to open")
        }
        
        // Show error message
        showDialogErrorMessage()
    }
    
    /**
     * Shows error message when dialog fails to open
     */
    private fun showDialogErrorMessage() {
        val errorStep = TutorialStep(
            id = "dialog_error_step2",
            title = "Tutorial Step Unavailable",
            description = "The chart selection step is temporarily unavailable. You can continue exploring the app.",
            targetViewId = null,
            animationType = AnimationType.FADE,
            duration = 3000L
        )
        
        tutorialManager?.showModernTapTarget(errorStep)
    }
    
    /**
     * Validates gauge chart selection
     */
    fun validateGaugeChartSelection(): Boolean {
        // Check if gauge chart was properly selected
        return gaugeChartCard != null && gaugeChartCard?.isEnabled == true
    }
    
    /**
     * Provides validation feedback
     */
    fun showValidationFeedback(isValid: Boolean) {
        val feedbackStep = if (isValid) {
            TutorialStep(
                id = "gauge_selection_success",
                title = "Perfect!",
                description = "You've selected the gauge chart type.",
                targetViewId = null,
                animationType = AnimationType.CELEBRATION,
                duration = 2000L,
                explanatoryText = "Gauge charts are great for displaying single metrics!"
            )
        } else {
            TutorialStep(
                id = "gauge_selection_retry",
                title = "Try Again",
                description = "Please select the Gauge Chart option to continue.",
                targetViewId = null,
                animationType = AnimationType.PULSE,
                duration = 3000L,
                explanatoryText = "Look for the gauge chart card and tap on it."
            )
        }
        
        tutorialManager?.showModernTapTarget(feedbackStep)
    }
    
    /**
     * Restores original gauge card functionality after tutorial
     */
    fun restoreOriginalGaugeCardFunctionality() {
        gaugeChartCard?.let { card ->
            // Restore original click listener if it existed
            originalGaugeClickListener?.let { originalListener ->
                card.setOnClickListener(originalListener)
            } ?: run {
                // Set default gauge card functionality if no original listener
                card.setOnClickListener { view ->
                    handleDefaultGaugeCardClick(view)
                }
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Original gauge card functionality restored")
            }
        }
    }
    
    /**
     * Handles default gauge card click (when no original listener exists)
     */
    private fun handleDefaultGaugeCardClick(view: View) {
        // Default behavior: select gauge chart and dismiss dialog
        chartTypeDialog?.dismiss()
        // This would typically trigger the chart creation flow
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        restoreOriginalGaugeCardFunctionality()
        chartTypeDialog?.dismiss()
        chartTypeDialog = null
        gaugeChartCard = null
        tutorialManager = null
        originalGaugeClickListener = null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 2 implementation cleaned up")
        }
    }
    
    /**
     * Gets Step 2 analytics
     */
    fun getStep2Analytics(): Map<String, Any> {
        return mapOf(
            "dialog_shown" to (chartTypeDialog != null),
            "gauge_card_found" to (gaugeChartCard != null),
            "gauge_card_visible" to (gaugeChartCard?.visibility == View.VISIBLE),
            "gauge_card_enabled" to (gaugeChartCard?.isEnabled == true),
            "has_original_listener" to (originalGaugeClickListener != null),
            "validation_passed" to validateGaugeChartSelection()
        )
    }
}