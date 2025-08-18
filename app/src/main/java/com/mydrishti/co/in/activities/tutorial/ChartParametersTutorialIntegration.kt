package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.content.Intent
import android.util.Log
import com.mydrishti.co.`in`.activities.ChartParametersActivity
import com.mydrishti.co.`in`.activities.models.ChartType

/**
 * Integration helper for connecting tutorial system with ChartParametersActivity
 * This class provides methods to integrate tutorial functionality into the chart parameters process
 */
class ChartParametersTutorialIntegration(private val context: Context) {
    
    private var tutorialManager: TutorialManager? = null
    private var step4Implementation: TutorialStep4Implementation? = null
    
    /**
     * Creates a tutorial-aware Intent for ChartParametersActivity
     */
    fun createTutorialAwareIntent(
        chartType: ChartType,
        siteId: Long,
        siteName: String,
        enableTutorial: Boolean = false
    ): Intent {
        
        val intent = Intent(context, ChartParametersActivity::class.java).apply {
            putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartType.name)
            putExtra(ChartParametersActivity.EXTRA_SITE_ID, siteId)
            putExtra(ChartParametersActivity.EXTRA_SITE_NAME, siteName)
            
            if (enableTutorial) {
                putExtra(EXTRA_TUTORIAL_ENABLED, true)
                putExtra(EXTRA_TUTORIAL_STEP, TutorialConstants.STEP_PARAMETER_SELECTION)
            }
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Created tutorial-aware intent for ChartParametersActivity")
        }
        
        return intent
    }
    
    /**
     * Starts Step 4 tutorial for parameter selection
     */
    fun startStep4Tutorial(activity: ChartParametersActivity) {
        if (tutorialManager == null) {
            tutorialManager = TutorialManager.create(activity)
        }
        
        if (step4Implementation == null) {
            step4Implementation = TutorialStep4Implementation(activity)
        }
        
        // Initialize Step 4 with the activity
        tutorialManager?.let { manager ->
            step4Implementation?.initializeStep4(manager)
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 4 tutorial started for parameter selection")
        }
    }
    
    /**
     * Checks if tutorial should be shown for parameter selection
     */
    fun shouldShowTutorial(): Boolean {
        if (tutorialManager == null) {
            tutorialManager = TutorialManager.create(context)
        }
        
        return tutorialManager?.isActive() == true && 
               tutorialManager?.getCurrentStep()?.id == TutorialConstants.STEP_PARAMETER_SELECTION
    }
    
    /**
     * Handles parameter configuration during tutorial
     */
    fun handleTutorialParameterConfiguration(parameterName: String, parameterValue: String) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Parameter configured during tutorial: $parameterName = $parameterValue")
        }
        
        // Let Step 4 implementation handle the tutorial logic
        step4Implementation?.handleTutorialProgression()
    }
    
    /**
     * Validates parameter configuration for tutorial
     */
    fun validateParameterConfiguration(): Boolean {
        val isValid = step4Implementation?.validateParameterSelection() ?: false
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Validating parameter configuration: $isValid")
        }
        
        return isValid
    }
    
    /**
     * Handles save button click during tutorial
     */
    fun handleTutorialSaveButtonClick() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Save button clicked during tutorial")
        }
        
        // Validate parameters before proceeding
        val isValid = validateParameterConfiguration()
        
        // Provide feedback through Step 4 implementation
        step4Implementation?.showValidationFeedback(isValid)
        
        if (isValid) {
            // Proceed with tutorial progression
            handleTutorialProgression()
        }
    }
    
    /**
     * Handles tutorial progression after parameter configuration
     */
    fun handleTutorialProgression() {
        val isValid = validateParameterConfiguration()
        
        when {
            isValid -> {
                // Valid configuration for tutorial
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial progressing with valid parameter configuration")
                }
                // Step 4 implementation will handle progression to Step 5
            }
            else -> {
                // Invalid configuration during tutorial
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Invalid parameter configuration during tutorial")
                }
                // Show guidance for parameter configuration
                showParameterConfigurationGuidance()
            }
        }
    }
    
    /**
     * Shows guidance for parameter configuration during tutorial
     */
    private fun showParameterConfigurationGuidance() {
        val guidanceStep = TutorialStep(
            id = "parameter_configuration_guidance",
            title = "Configure Chart Parameters",
            description = "Please fill in the required chart parameters to continue.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Chart parameters like title and settings help customize how your data is displayed."
        )
        
        tutorialManager?.showModernTapTarget(guidanceStep)
    }
    
    /**
     * Handles chart type specific guidance
     */
    fun showChartTypeSpecificGuidance(chartType: ChartType) {
        val guidanceText = when (chartType) {
            ChartType.GAUGE -> "For gauge charts, configure the title and any threshold values."
            ChartType.BAR_DAILY -> "For daily bar charts, set the title and date range parameters."
            ChartType.BAR_HOURLY -> "For hourly bar charts, configure the title and time range settings."
            ChartType.METRIC -> "For metric charts, set the title and select which metrics to display."
        }
        
        val guidanceStep = TutorialStep(
            id = "chart_type_specific_guidance",
            title = "Chart Configuration",
            description = guidanceText,
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Different chart types have different configuration options to help you display your data effectively."
        )
        
        tutorialManager?.showModernTapTarget(guidanceStep)
    }
    
    /**
     * Gets tutorial analytics for parameter selection
     */
    fun getTutorialAnalytics(): Map<String, Any> {
        val step4Analytics = step4Implementation?.getStep4Analytics() ?: emptyMap()
        val integrationAnalytics = mapOf(
            "tutorial_manager_active" to (tutorialManager?.isActive() == true),
            "step4_implementation_initialized" to (step4Implementation != null),
            "should_show_tutorial" to shouldShowTutorial()
        )
        
        return step4Analytics + integrationAnalytics
    }
    
    /**
     * Handles activity lifecycle events
     */
    fun onActivityCreated(activity: ChartParametersActivity) {
        // Check if tutorial should be started
        if (shouldShowTutorial()) {
            // Delay tutorial start to allow activity to fully initialize
            activity.window.decorView.post {
                startStep4Tutorial(activity)
            }
        }
    }
    
    /**
     * Handles activity pause
     */
    fun onActivityPaused() {
        tutorialManager?.pauseTutorial()
    }
    
    /**
     * Handles activity resume
     */
    fun onActivityResumed() {
        tutorialManager?.resumeTutorial()
    }
    
    /**
     * Handles successful chart save during tutorial
     */
    fun handleChartSaveSuccess() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart saved successfully during tutorial")
        }
        
        // This will trigger Step 5 (completion celebration)
        showChartSaveSuccessMessage()
    }
    
    /**
     * Shows success message for chart save
     */
    private fun showChartSaveSuccessMessage() {
        val successStep = TutorialStep(
            id = "chart_save_success",
            title = "Chart Saved!",
            description = "Your chart has been successfully created and saved to the dashboard.",
            targetViewId = null,
            animationType = AnimationType.CELEBRATION,
            duration = 3000L,
            explanatoryText = "You can now view and manage your chart from the main dashboard."
        )
        
        tutorialManager?.showModernTapTarget(successStep)
    }
    
    /**
     * Handles chart save error during tutorial
     */
    fun handleChartSaveError(errorMessage: String) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart save error during tutorial: $errorMessage")
        }
        
        val errorStep = TutorialStep(
            id = "chart_save_error",
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
     * Cleanup method
     */
    fun cleanup() {
        step4Implementation?.cleanup()
        step4Implementation = null
        tutorialManager = null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "ChartParametersTutorialIntegration cleaned up")
        }
    }
    
    companion object {
        const val EXTRA_TUTORIAL_ENABLED = "extra_tutorial_enabled"
        const val EXTRA_TUTORIAL_STEP = "extra_tutorial_step"
        
        /**
         * Creates integration instance
         */
        fun create(context: Context): ChartParametersTutorialIntegration {
            return ChartParametersTutorialIntegration(context)
        }
        
        /**
         * Quick check if parameter selection tutorial should be active
         */
        fun shouldActivateTutorial(context: Context): Boolean {
            val tutorialManager = TutorialManager.create(context)
            return tutorialManager.isActive() && 
                   tutorialManager.getCurrentStep()?.id == TutorialConstants.STEP_PARAMETER_SELECTION
        }
        
        /**
         * Checks if intent contains tutorial information
         */
        fun hasTutorialIntent(intent: Intent): Boolean {
            return intent.getBooleanExtra(EXTRA_TUTORIAL_ENABLED, false)
        }
        
        /**
         * Gets tutorial step from intent
         */
        fun getTutorialStepFromIntent(intent: Intent): String? {
            return intent.getStringExtra(EXTRA_TUTORIAL_STEP)
        }
        
        /**
         * Provides tutorial guidance text for parameter selection
         */
        fun getTutorialGuidanceText(): String {
            return "Configure your chart parameters such as title and settings. These help customize how your data is displayed."
        }
        
        /**
         * Creates a tutorial-aware navigation intent from SiteSelectionActivity
         */
        fun createTutorialNavigationIntent(
            context: Context,
            chartType: ChartType,
            siteId: Long,
            siteName: String
        ): Intent {
            return ChartParametersTutorialIntegration(context)
                .createTutorialAwareIntent(chartType, siteId, siteName, enableTutorial = true)
        }
    }
}