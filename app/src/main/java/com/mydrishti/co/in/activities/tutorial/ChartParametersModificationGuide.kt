package com.mydrishti.co.`in`.activities.tutorial

/**
 * MODIFICATION GUIDE: ChartParametersActivity Tutorial Integration
 * 
 * This guide provides instructions for integrating tutorial functionality 
 * into the existing ChartParametersActivity.
 * 
 * IMPORTANT: These modifications should be applied to the existing 
 * ChartParametersActivity.kt file to enable tutorial support.
 */

/*
=== STEP 1: Add Tutorial Integration Imports ===

Add these imports to ChartParametersActivity.kt:

import com.mydrishti.co.`in`.activities.tutorial.ChartParametersTutorialIntegration
import com.mydrishti.co.`in`.activities.tutorial.TutorialManager
import com.mydrishti.co.`in`.activities.tutorial.TutorialConstants

=== STEP 2: Add Tutorial Integration Property ===

Add this property to the ChartParametersActivity class:

private var tutorialIntegration: ChartParametersTutorialIntegration? = null

=== STEP 3: Initialize Tutorial Integration in onCreate ===

Add this code after the existing parameter extraction in onCreate():

// Check if tutorial should be active
val tutorialEnabled = intent.getBooleanExtra(
    ChartParametersTutorialIntegration.EXTRA_TUTORIAL_ENABLED, 
    false
)

if (tutorialEnabled || ChartParametersTutorialIntegration.shouldActivateTutorial(this)) {
    tutorialIntegration = ChartParametersTutorialIntegration.create(this)
    
    if (TutorialConstants.DEBUG_MODE_ENABLED) {
        android.util.Log.d(TutorialConstants.DEBUG_LOG_TAG, 
            "Tutorial integration initialized for ChartParametersActivity")
    }
}

=== STEP 4: Add Tutorial Initialization After UI Setup ===

Add this code at the end of onCreate() after setupUI():

// Initialize tutorial if enabled
tutorialIntegration?.onActivityCreated(this)

=== STEP 5: Modify setupViewModelObservers Method ===

Update the chart save result observer to include tutorial handling:

// Observe chart save/update result
viewModel.chartSaveResult.observe(this) { success ->
    if (success) {
        // Handle tutorial progression for successful save
        if (tutorialIntegration?.shouldShowTutorial() == true) {
            tutorialIntegration?.handleChartSaveSuccess()
        }
        
        Toast.makeText(
            this,
            if (chartId.isEmpty()) R.string.chart_added else R.string.chart_updated,
            Toast.LENGTH_SHORT
        ).show()

        // ... existing code for navigation ...
    }
}

// Add error handling for tutorial
viewModel.error.observe(this) { errorMessage ->
    errorMessage?.let {
        // Handle tutorial progression for save error
        if (tutorialIntegration?.shouldShowTutorial() == true) {
            tutorialIntegration?.handleChartSaveError(it)
        }
        
        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        viewModel.clearError()
    }
}

=== STEP 6: Add Tutorial-Aware Save Method ===

Add this method to handle tutorial-aware saving:

private fun handleTutorialAwareSave() {
    // Check if tutorial is active
    if (tutorialIntegration?.shouldShowTutorial() == true) {
        // Validate parameters for tutorial
        val isValid = tutorialIntegration?.validateParameterConfiguration() ?: false
        
        if (isValid) {
            // Proceed with save
            tutorialIntegration?.handleTutorialSaveButtonClick()
            // Continue with normal save process
            saveChart()
        } else {
            // Show validation feedback
            tutorialIntegration?.showValidationFeedback(false, "Please fill in all required fields")
        }
    } else {
        // Normal save process
        saveChart()
    }
}

=== STEP 7: Update Save Button Click Handler ===

If there's a save button click handler, update it to use tutorial-aware saving:

// In setupUI() or wherever save button is configured:
binding.btnSaveChart.setOnClickListener {
    handleTutorialAwareSave()
}

=== STEP 8: Add Chart Title Change Listener ===

Add this code in setupUI() to handle tutorial parameter input:

// Add text change listener for tutorial
binding.etChartTitle.addTextChangedListener { text ->
    if (tutorialIntegration?.shouldShowTutorial() == true && !text.isNullOrBlank()) {
        tutorialIntegration?.handleTutorialParameterConfiguration("chart_title", text.toString())
    }
}

=== STEP 9: Add Lifecycle Methods ===

Add these lifecycle methods to handle tutorial state:

override fun onPause() {
    super.onPause()
    tutorialIntegration?.onActivityPaused()
}

override fun onResume() {
    super.onResume()
    tutorialIntegration?.onActivityResumed()
}

override fun onDestroy() {
    super.onDestroy()
    tutorialIntegration?.cleanup()
    tutorialIntegration = null
}

=== STEP 10: Update SiteSelectionActivity Navigation ===

Update SiteSelectionActivity.kt to use tutorial-aware navigation:

// Replace existing navigateToParameterSelection() method with:
private fun navigateToParameterSelection(device: com.mydrishti.co.`in`.activities.models.Device) {
    // Check if tutorial is active
    val tutorialIntegration = SiteSelectionTutorialIntegration.create(this)
    val isTutorialActive = tutorialIntegration.shouldShowTutorial()
    
    val intent = if (isTutorialActive) {
        // Create tutorial-aware intent
        ChartParametersTutorialIntegration.createTutorialNavigationIntent(
            context = this,
            chartType = chartType!!,
            siteId = device.iotDeviceMapId.toLong(),
            siteName = device.deviceDisplayName
        )
    } else {
        // Create normal intent
        Intent(this, ChartParametersActivity::class.java).apply {
            putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartType?.name)
            putExtra(ChartParametersActivity.EXTRA_SITE_ID, device.iotDeviceMapId)
            putExtra(ChartParametersActivity.EXTRA_SITE_NAME, device.deviceDisplayName)
        }
    }
    
    startActivity(intent)
}

=== STEP 11: Add Public Save Method (Optional) ===

Add this public method to allow tutorial to trigger save:

fun triggerSaveForTutorial() {
    if (TutorialConstants.DEBUG_MODE_ENABLED) {
        android.util.Log.d(TutorialConstants.DEBUG_LOG_TAG, "Save triggered by tutorial")
    }
    saveChart()
}

=== STEP 12: Add Chart Type Specific Guidance ===

Add this code after tutorial initialization to show chart type specific guidance:

// Show chart type specific guidance if tutorial is active
if (tutorialIntegration?.shouldShowTutorial() == true && chartType != null) {
    // Delay to allow UI to fully load
    binding.root.post {
        tutorialIntegration?.showChartTypeSpecificGuidance(chartType!!)
    }
}

=== TESTING CHECKLIST ===

After applying these modifications, test the following:

1. ✅ Normal activity functionality (without tutorial)
2. ✅ Tutorial Step 4 activation when tutorial is active
3. ✅ Parameter selection area highlighting and guidance
4. ✅ Chart title input validation and feedback
5. ✅ Save button interaction during tutorial
6. ✅ Chart type specific guidance display
7. ✅ Tutorial progression to Step 5 after save
8. ✅ Error handling and fallback scenarios
9. ✅ Activity lifecycle handling during tutorial
10. ✅ Analytics and logging functionality

=== NOTES ===

- The tutorial integration is designed to be non-intrusive
- Normal activity functionality is preserved when tutorial is not active
- All tutorial-specific code is conditionally executed
- Error handling ensures the activity works even if tutorial components fail
- The integration supports both guided parameter configuration and flexible input modes
- Chart type specific guidance helps users understand different chart options

*/

/**
 * This class serves as documentation only and should not be instantiated.
 * Apply the modifications above to the actual ChartParametersActivity.kt file.
 */
class ChartParametersModificationGuide private constructor() {
    init {
        throw UnsupportedOperationException("This class is for documentation only")
    }
}