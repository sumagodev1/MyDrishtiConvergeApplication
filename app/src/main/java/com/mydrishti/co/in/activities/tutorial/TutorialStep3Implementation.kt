package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.SiteSelectionActivity
import com.mydrishti.co.`in`.activities.adapters.SiteAdapter
import com.mydrishti.co.`in`.activities.models.Device

/**
 * Implementation of Tutorial Step 3: Site selection guidance
 * This class handles the third tutorial step - highlighting the site list and guiding site selection
 */
class TutorialStep3Implementation(private val activity: Activity) {
    
    private var tutorialManager: TutorialManager? = null
    private var recyclerViewSites: RecyclerView? = null
    private var siteAdapter: SiteAdapter? = null
    private var originalItemClickListener: ((Device) -> Unit)? = null
    private var hasShownNoSitesFallback = false
    
    /**
     * Initializes Step 3 of the tutorial - Site selection guidance
     */
    fun initializeStep3(tutorialManager: TutorialManager) {
        this.tutorialManager = tutorialManager
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Initializing Tutorial Step 3: Site selection")
        }
        
        // Find the RecyclerView for sites
        findSiteRecyclerView()
        
        if (recyclerViewSites == null) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.w(TutorialConstants.DEBUG_LOG_TAG, "Site RecyclerView not found - Step 3 cannot proceed")
            }
            handleRecyclerViewNotFound()
            return
        }
        
        // Setup RecyclerView for tutorial
        setupRecyclerViewForTutorial()
        
        // Start the tutorial step
        startStep3Tutorial()
    }
    
    /**
     * Finds the site RecyclerView in the activity
     */
    private fun findSiteRecyclerView() {
        recyclerViewSites = activity.findViewById(R.id.recyclerViewSites)
        
        // Try to get the adapter if RecyclerView exists
        recyclerViewSites?.let { recyclerView ->
            siteAdapter = recyclerView.adapter as? SiteAdapter
        }
    }
    
    /**
     * Sets up the RecyclerView for tutorial interaction
     */
    private fun setupRecyclerViewForTutorial() {
        recyclerViewSites?.let { recyclerView ->
            // Store original adapter if it exists
            siteAdapter?.let { adapter ->
                // We'll need to intercept item clicks for tutorial
                // This would require modifying the SiteAdapter to support tutorial mode
                setupAdapterForTutorial(adapter)
            }
            
            // Ensure RecyclerView is visible
            recyclerView.visibility = View.VISIBLE
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Site RecyclerView setup complete for tutorial")
            }
        }
    }
    
    /**
     * Sets up the adapter for tutorial interaction
     */
    private fun setupAdapterForTutorial(adapter: SiteAdapter) {
        // This would ideally require modifying SiteAdapter to support tutorial mode
        // For now, we'll work with what we have and provide guidance
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Site adapter configured for tutorial")
        }
    }
    
    /**
     * Starts the Step 3 tutorial with site list highlighting
     */
    private fun startStep3Tutorial() {
        recyclerViewSites?.let { recyclerView ->
            // Check if there are any sites available
            val itemCount = recyclerView.adapter?.itemCount ?: 0
            
            if (itemCount == 0) {
                // No sites available - show fallback guidance
                handleNoSitesAvailable()
                return
            }
            
            // Create tutorial step configuration
            val step3Config = TutorialConfigFactory.createFirstTimeUserTutorial()
                .getStepById(TutorialConstants.STEP_SITE_SELECTION)
            
            if (step3Config == null) {
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.e(TutorialConstants.DEBUG_LOG_TAG, "Step 3 configuration not found")
                }
                return
            }
            
            // Show tutorial overlay for site list
            tutorialManager?.showModernTapTarget(step3Config, recyclerView)
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 3 tutorial started with site list highlight")
            }
        }
    }
    
    /**
     * Handles the case when no sites are available
     */
    private fun handleNoSitesAvailable() {
        if (hasShownNoSitesFallback) {
            return // Avoid showing multiple fallback messages
        }
        
        hasShownNoSitesFallback = true
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.w(TutorialConstants.DEBUG_LOG_TAG, "No sites available - showing fallback guidance")
        }
        
        // Show fallback tutorial message
        showNoSitesFallbackMessage()
    }
    
    /**
     * Shows fallback tutorial message when no sites are available
     */
    private fun showNoSitesFallbackMessage() {
        val fallbackStep = TutorialStep(
            id = "no_sites_fallback",
            title = "No Data Sources Available",
            description = "It looks like there are no data sources configured yet. This is normal for new accounts.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 5000L,
            explanatoryText = "In a real scenario, you would see a list of monitoring devices or data sources here. You can continue exploring the app or contact support to set up data sources."
        )
        
        tutorialManager?.showModernTapTarget(fallbackStep)
        
        // After showing the fallback, we can either:
        // 1. Skip to the next step
        // 2. End the tutorial
        // 3. Show alternative guidance
        
        // For now, let's proceed to show completion guidance
        showStep3CompletionGuidance()
    }
    
    /**
     * Handles site selection during tutorial
     */
    fun handleSiteSelectionDuringTutorial(selectedDevice: Device) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Site selected during tutorial: ${selectedDevice.deviceDisplayName}")
        }
        
        // Record tutorial interaction
        recordStep3Completion(selectedDevice)
        
        // Show validation feedback
        showSiteSelectionValidationFeedback(selectedDevice)
        
        // Proceed to Step 4 (Parameter Selection)
        proceedToStep4(selectedDevice)
    }
    
    /**
     * Records Step 3 completion
     */
    private fun recordStep3Completion(selectedDevice: Device) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 3 completed - Site selected: ${selectedDevice.deviceDisplayName}")
        }
        
        // Mark step as completed in tutorial manager
        tutorialManager?.markStepCompleted(TutorialConstants.STEP_SITE_SELECTION)
    }
    
    /**
     * Shows validation feedback for site selection
     */
    private fun showSiteSelectionValidationFeedback(selectedDevice: Device) {
        val feedbackStep = TutorialStep(
            id = "site_selection_success",
            title = "Great Choice!",
            description = "You've selected '${selectedDevice.deviceDisplayName}' as your data source.",
            targetViewId = null,
            animationType = AnimationType.CELEBRATION,
            duration = 2500L,
            explanatoryText = "This data source will provide the information for your chart."
        )
        
        tutorialManager?.showModernTapTarget(feedbackStep)
    }
    
    /**
     * Proceeds to Step 4 - Parameter Selection
     */
    private fun proceedToStep4(selectedDevice: Device) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Proceeding to Step 4: Parameter Selection")
        }
        
        // Show transition message to Step 4
        showStep4TransitionMessage(selectedDevice)
    }
    
    /**
     * Shows transition message to Step 4
     */
    private fun showStep4TransitionMessage(selectedDevice: Device) {
        val transitionStep = TutorialStep(
            id = "step3_to_step4_transition",
            title = "Next: Configure Parameters",
            description = "Now let's set up the specific parameters for your chart from '${selectedDevice.deviceDisplayName}'.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 3000L,
            explanatoryText = "Parameters determine what specific data points your chart will display."
        )
        
        tutorialManager?.showModernTapTarget(transitionStep)
    }
    
    /**
     * Shows Step 3 completion guidance when no sites are available
     */
    private fun showStep3CompletionGuidance() {
        val completionStep = TutorialStep(
            id = "step3_completion_guidance",
            title = "Tutorial Continues",
            description = "Since no data sources are available, we'll show you what the next steps would look like.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "In the next step, you would normally configure chart parameters. Let's continue to see how that works."
        )
        
        tutorialManager?.showModernTapTarget(completionStep)
        
        // Mark step as completed even without actual site selection
        tutorialManager?.markStepCompleted(TutorialConstants.STEP_SITE_SELECTION)
        
        // Proceed to Step 4 with mock data
        proceedToStep4WithMockData()
    }
    
    /**
     * Proceeds to Step 4 with mock data when no real sites are available
     */
    private fun proceedToStep4WithMockData() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Proceeding to Step 4 with mock data")
        }
        
        // Create a mock device for tutorial purposes
        val mockDevice = Device(
            iotDeviceMapId = "tutorial_mock_device",
            deviceDisplayName = "Tutorial Demo Device",
            deviceType = "Demo",
            isActive = true
        )
        
        showStep4TransitionMessage(mockDevice)
    }
    
    /**
     * Validates site selection for tutorial
     */
    fun validateSiteSelection(selectedDevice: Device?): Boolean {
        val isValid = selectedDevice != null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Validating site selection: $isValid")
        }
        
        return isValid
    }
    
    /**
     * Provides validation feedback
     */
    fun showValidationFeedback(isValid: Boolean, selectedDevice: Device? = null) {
        val feedbackStep = if (isValid && selectedDevice != null) {
            TutorialStep(
                id = "site_selection_valid",
                title = "Perfect!",
                description = "You've selected a data source: ${selectedDevice.deviceDisplayName}",
                targetViewId = null,
                animationType = AnimationType.CELEBRATION,
                duration = 2000L,
                explanatoryText = "This device will provide data for your chart."
            )
        } else {
            TutorialStep(
                id = "site_selection_guidance",
                title = "Select a Data Source",
                description = "Please tap on one of the available data sources to continue.",
                targetViewId = null,
                animationType = AnimationType.PULSE,
                duration = 3000L,
                explanatoryText = "Each data source represents a monitoring device or system that can provide data for your charts."
            )
        }
        
        tutorialManager?.showModernTapTarget(feedbackStep)
    }
    
    /**
     * Handles the case where RecyclerView is not found
     */
    private fun handleRecyclerViewNotFound() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.w(TutorialConstants.DEBUG_LOG_TAG, "Site RecyclerView not found - showing fallback tutorial message")
        }
        
        // Show fallback tutorial message
        showRecyclerViewNotFoundFallback()
    }
    
    /**
     * Shows fallback tutorial message when RecyclerView is not found
     */
    private fun showRecyclerViewNotFoundFallback() {
        val fallbackStep = TutorialStep(
            id = "recyclerview_not_found_fallback",
            title = "Site Selection",
            description = "This screen would normally show a list of available data sources.",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "Look for a list of monitoring devices or data sources that you can select from."
        )
        
        tutorialManager?.showModernTapTarget(fallbackStep)
        
        // Continue with completion guidance
        showStep3CompletionGuidance()
    }
    
    /**
     * Handles tutorial progression after site selection
     */
    fun handleTutorialProgression(selectedDevice: Device?) {
        if (selectedDevice != null) {
            // Valid selection - proceed normally
            handleSiteSelectionDuringTutorial(selectedDevice)
        } else {
            // No selection - show guidance
            showValidationFeedback(false)
        }
    }
    
    /**
     * Gets the number of available sites
     */
    fun getAvailableSitesCount(): Int {
        return recyclerViewSites?.adapter?.itemCount ?: 0
    }
    
    /**
     * Checks if sites are currently loading
     */
    fun areSitesLoading(): Boolean {
        // This would require integration with the SiteViewModel
        // For now, we'll return false
        return false
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        recyclerViewSites = null
        siteAdapter = null
        tutorialManager = null
        originalItemClickListener = null
        hasShownNoSitesFallback = false
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 3 implementation cleaned up")
        }
    }
    
    /**
     * Gets Step 3 analytics
     */
    fun getStep3Analytics(): Map<String, Any> {
        return mapOf(
            "recyclerview_found" to (recyclerViewSites != null),
            "recyclerview_visible" to (recyclerViewSites?.visibility == View.VISIBLE),
            "adapter_attached" to (recyclerViewSites?.adapter != null),
            "available_sites_count" to getAvailableSitesCount(),
            "sites_loading" to areSitesLoading(),
            "has_shown_no_sites_fallback" to hasShownNoSitesFallback,
            "validation_passed" to (getAvailableSitesCount() > 0)
        )
    }
}