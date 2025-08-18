package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.content.Intent
import android.util.Log
import com.mydrishti.co.`in`.activities.SiteSelectionActivity
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.models.Device

/**
 * Integration helper for connecting tutorial system with SiteSelectionActivity
 * This class provides methods to integrate tutorial functionality into the site selection process
 */
class SiteSelectionTutorialIntegration(private val context: Context) {
    
    private var tutorialManager: TutorialManager? = null
    private var step3Implementation: TutorialStep3Implementation? = null
    
    /**
     * Creates a tutorial-aware Intent for SiteSelectionActivity
     */
    fun createTutorialAwareIntent(
        chartType: ChartType,
        enableTutorial: Boolean = false
    ): Intent {
        
        val intent = Intent(context, SiteSelectionActivity::class.java).apply {
            putExtra(SiteSelectionActivity.EXTRA_CHART_TYPE, chartType.name)
            
            if (enableTutorial) {
                putExtra(EXTRA_TUTORIAL_ENABLED, true)
                putExtra(EXTRA_TUTORIAL_STEP, TutorialConstants.STEP_SITE_SELECTION)
            }
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Created tutorial-aware intent for SiteSelectionActivity")
        }
        
        return intent
    }
    
    /**
     * Starts Step 3 tutorial for site selection
     */
    fun startStep3Tutorial(activity: SiteSelectionActivity) {
        if (tutorialManager == null) {
            tutorialManager = TutorialManager.create(activity)
        }
        
        if (step3Implementation == null) {
            step3Implementation = TutorialStep3Implementation(activity)
        }
        
        // Initialize Step 3 with the activity
        tutorialManager?.let { manager ->
            step3Implementation?.initializeStep3(manager)
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 3 tutorial started for site selection")
        }
    }
    
    /**
     * Checks if tutorial should be shown for site selection
     */
    fun shouldShowTutorial(): Boolean {
        if (tutorialManager == null) {
            tutorialManager = TutorialManager.create(context)
        }
        
        return tutorialManager?.isActive() == true && 
               tutorialManager?.getCurrentStep()?.id == TutorialConstants.STEP_SITE_SELECTION
    }
    
    /**
     * Handles site selection during tutorial
     */
    fun handleTutorialSiteSelection(selectedDevice: Device) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Site selected during tutorial: ${selectedDevice.deviceDisplayName}")
        }
        
        // Let Step 3 implementation handle the tutorial logic
        step3Implementation?.handleSiteSelectionDuringTutorial(selectedDevice)
    }
    
    /**
     * Validates site selection for tutorial
     */
    fun validateSiteSelection(selectedDevice: Device?): Boolean {
        val isValid = selectedDevice != null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Validating site selection: $isValid")
        }
        
        // Provide feedback through Step 3 implementation
        step3Implementation?.showValidationFeedback(isValid, selectedDevice)
        
        return isValid
    }
    
    /**
     * Handles tutorial progression after site selection
     */
    fun handleTutorialProgression(selectedDevice: Device?) {
        when {
            selectedDevice != null -> {
                // Valid selection for tutorial
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial progressing with site selection: ${selectedDevice.deviceDisplayName}")
                }
                // Step 3 implementation will handle progression to Step 4
            }
            else -> {
                // No selection during tutorial
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "No site selected during tutorial")
                }
                // Show guidance for site selection
                showSiteSelectionGuidance()
            }
        }
    }
    
    /**
     * Shows guidance for site selection during tutorial
     */
    private fun showSiteSelectionGuidance() {
        val guidanceStep = TutorialStep(
            id = "site_selection_guidance",
            title = "Select a Data Source",
            description = "Choose one of the available monitoring devices or data sources from the list.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Each item in the list represents a device that can provide data for your chart. Tap on one to continue."
        )
        
        tutorialManager?.showModernTapTarget(guidanceStep)
    }
    
    /**
     * Handles the case when no sites are available
     */
    fun handleNoSitesAvailable() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "No sites available for tutorial")
        }
        
        step3Implementation?.handleNoSitesAvailable()
    }
    
    /**
     * Gets tutorial analytics for site selection
     */
    fun getTutorialAnalytics(): Map<String, Any> {
        val step3Analytics = step3Implementation?.getStep3Analytics() ?: emptyMap()
        val integrationAnalytics = mapOf(
            "tutorial_manager_active" to (tutorialManager?.isActive() == true),
            "step3_implementation_initialized" to (step3Implementation != null),
            "should_show_tutorial" to shouldShowTutorial()
        )
        
        return step3Analytics + integrationAnalytics
    }
    
    /**
     * Handles activity lifecycle events
     */
    fun onActivityCreated(activity: SiteSelectionActivity) {
        // Check if tutorial should be started
        if (shouldShowTutorial()) {
            // Delay tutorial start to allow activity to fully initialize
            activity.window.decorView.post {
                startStep3Tutorial(activity)
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
     * Cleanup method
     */
    fun cleanup() {
        step3Implementation?.cleanup()
        step3Implementation = null
        tutorialManager = null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "SiteSelectionTutorialIntegration cleaned up")
        }
    }
    
    companion object {
        const val EXTRA_TUTORIAL_ENABLED = "extra_tutorial_enabled"
        const val EXTRA_TUTORIAL_STEP = "extra_tutorial_step"
        
        /**
         * Creates integration instance
         */
        fun create(context: Context): SiteSelectionTutorialIntegration {
            return SiteSelectionTutorialIntegration(context)
        }
        
        /**
         * Quick check if site selection tutorial should be active
         */
        fun shouldActivateTutorial(context: Context): Boolean {
            val tutorialManager = TutorialManager.create(context)
            return tutorialManager.isActive() && 
                   tutorialManager.getCurrentStep()?.id == TutorialConstants.STEP_SITE_SELECTION
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
         * Provides tutorial guidance text for site selection
         */
        fun getTutorialGuidanceText(): String {
            return "Select a data source from the list below. Each item represents a monitoring device that can provide data for your chart."
        }
        
        /**
         * Creates a tutorial-aware navigation intent from MainActivity
         */
        fun createTutorialNavigationIntent(
            context: Context,
            chartType: ChartType
        ): Intent {
            return SiteSelectionTutorialIntegration(context)
                .createTutorialAwareIntent(chartType, enableTutorial = true)
        }
    }
}