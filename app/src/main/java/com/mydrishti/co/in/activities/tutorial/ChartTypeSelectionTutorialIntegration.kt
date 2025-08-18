package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.util.Log
import com.mydrishti.co.`in`.activities.dialogs.ChartTypeSelectionDialog
import com.mydrishti.co.`in`.activities.models.ChartType

/**
 * Integration helper for connecting tutorial system with ChartTypeSelectionDialog
 * This class provides methods to integrate tutorial functionality into the chart type selection process
 */
class ChartTypeSelectionTutorialIntegration(private val context: Context) {
    
    private var tutorialManager: TutorialManager? = null
    private var step2Implementation: TutorialStep2Implementation? = null
    
    /**
     * Creates a tutorial-aware ChartTypeSelectionDialog
     * This method should be used instead of directly creating ChartTypeSelectionDialog during tutorials
     */
    fun createTutorialAwareDialog(
        onChartTypeSelected: (ChartType) -> Unit,
        isTutorialActive: Boolean = false
    ): ChartTypeSelectionDialog {
        
        return if (isTutorialActive) {
            // Create dialog with tutorial integration
            createTutorialIntegratedDialog(onChartTypeSelected)
        } else {
            // Create normal dialog
            ChartTypeSelectionDialog(context, onChartTypeSelected)
        }
    }
    
    /**
     * Creates a ChartTypeSelectionDialog with tutorial integration
     */
    private fun createTutorialIntegratedDialog(
        onChartTypeSelected: (ChartType) -> Unit
    ): ChartTypeSelectionDialog {
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Creating tutorial-integrated chart type dialog")
        }
        
        // Create dialog with tutorial-aware callback
        return ChartTypeSelectionDialog(context) { chartType ->
            handleTutorialChartTypeSelection(chartType, onChartTypeSelected)
        }
    }
    
    /**
     * Handles chart type selection during tutorial
     */
    private fun handleTutorialChartTypeSelection(
        chartType: ChartType,
        originalCallback: (ChartType) -> Unit
    ) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart type selected during tutorial: $chartType")
        }
        
        // Let Step 2 implementation handle the tutorial logic
        step2Implementation?.let { step2 ->
            // This will be handled by the Step 2 implementation
            // The original callback will be called after tutorial validation
        }
        
        // Always call the original callback to maintain normal app flow
        originalCallback(chartType)
    }
    
    /**
     * Starts Step 2 tutorial for chart type selection
     */
    fun startStep2Tutorial(dialog: ChartTypeSelectionDialog) {
        if (tutorialManager == null) {
            tutorialManager = TutorialManager.create(context)
        }
        
        if (step2Implementation == null) {
            step2Implementation = TutorialStep2Implementation(context as android.app.Activity)
        }
        
        // Initialize Step 2 with the dialog
        tutorialManager?.let { manager ->
            step2Implementation?.initializeStep2(manager)
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 2 tutorial started for chart type selection")
        }
    }
    
    /**
     * Checks if tutorial should be shown for chart type selection
     */
    fun shouldShowTutorial(): Boolean {
        if (tutorialManager == null) {
            tutorialManager = TutorialManager.create(context)
        }
        
        return tutorialManager?.isActive() == true && 
               tutorialManager?.getCurrentStep()?.id == TutorialConstants.STEP_CHART_TYPE_SELECTION
    }
    
    /**
     * Validates gauge chart selection for tutorial
     */
    fun validateGaugeChartSelection(selectedType: ChartType): Boolean {
        val isGaugeSelected = selectedType == ChartType.GAUGE
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Validating gauge chart selection: $isGaugeSelected")
        }
        
        // Provide feedback through Step 2 implementation
        step2Implementation?.showValidationFeedback(isGaugeSelected)
        
        return isGaugeSelected
    }
    
    /**
     * Handles tutorial progression after chart type selection
     */
    fun handleTutorialProgression(selectedType: ChartType) {
        when (selectedType) {
            ChartType.GAUGE -> {
                // Expected selection for tutorial
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial progressing with gauge chart selection")
                }
                // Step 2 implementation will handle progression to Step 3
            }
            else -> {
                // Non-gauge selection during tutorial
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Non-gauge chart selected during tutorial: $selectedType")
                }
                // Show guidance but allow progression
                showNonGaugeSelectionGuidance(selectedType)
            }
        }
    }
    
    /**
     * Shows guidance when non-gauge chart is selected during tutorial
     */
    private fun showNonGaugeSelectionGuidance(selectedType: ChartType) {
        val guidanceMessage = when (selectedType) {
            ChartType.BAR_DAILY -> "Bar charts are great for trends over time. For this tutorial, gauge charts are simpler to set up."
            ChartType.BAR_HOURLY -> "Hourly bar charts show detailed time patterns. Gauge charts are recommended for beginners."
            ChartType.METRIC -> "Metric charts display raw numbers. Gauge charts provide better visual representation."
            else -> "You can explore different chart types after completing the tutorial."
        }
        
        val guidanceStep = TutorialStep(
            id = "non_gauge_guidance",
            title = "Chart Type Note",
            description = guidanceMessage,
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "We'll continue with your selection, but gauge charts are recommended for first-time users."
        )
        
        tutorialManager?.showModernTapTarget(guidanceStep)
    }
    
    /**
     * Gets tutorial analytics for chart type selection
     */
    fun getTutorialAnalytics(): Map<String, Any> {
        val step2Analytics = step2Implementation?.getStep2Analytics() ?: emptyMap()
        val integrationAnalytics = mapOf(
            "tutorial_manager_active" to (tutorialManager?.isActive() == true),
            "step2_implementation_initialized" to (step2Implementation != null),
            "should_show_tutorial" to shouldShowTutorial()
        )
        
        return step2Analytics + integrationAnalytics
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        step2Implementation?.cleanup()
        step2Implementation = null
        tutorialManager = null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "ChartTypeSelectionTutorialIntegration cleaned up")
        }
    }
    
    companion object {
        /**
         * Creates integration instance
         */
        fun create(context: Context): ChartTypeSelectionTutorialIntegration {
            return ChartTypeSelectionTutorialIntegration(context)
        }
        
        /**
         * Quick check if chart type selection tutorial should be active
         */
        fun shouldActivateTutorial(context: Context): Boolean {
            val tutorialManager = TutorialManager.create(context)
            return tutorialManager.isActive() && 
                   tutorialManager.getCurrentStep()?.id == TutorialConstants.STEP_CHART_TYPE_SELECTION
        }
        
        /**
         * Provides tutorial guidance text for chart type selection
         */
        fun getTutorialGuidanceText(): String {
            return "Choose Gauge Chart - perfect for beginners! Gauge charts provide clear visual representation of single values."
        }
        
        /**
         * Gets the recommended chart type for tutorial
         */
        fun getRecommendedChartType(): ChartType {
            return ChartType.GAUGE
        }
    }
}