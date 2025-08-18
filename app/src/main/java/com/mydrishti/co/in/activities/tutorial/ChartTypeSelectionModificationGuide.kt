package com.mydrishti.co.`in`.activities.tutorial

/**
 * MODIFICATION GUIDE: ChartTypeSelectionDialog Tutorial Integration
 * 
 * This guide provides instructions for integrating tutorial functionality 
 * into the existing ChartTypeSelectionDialog.
 * 
 * IMPORTANT: These modifications should be applied to the existing 
 * ChartTypeSelectionDialog.kt file to enable tutorial support.
 */

/*
=== STEP 1: Add Tutorial Integration Import ===

Add these imports to ChartTypeSelectionDialog.kt:

import com.mydrishti.co.`in`.activities.tutorial.ChartTypeSelectionTutorialIntegration
import com.mydrishti.co.`in`.activities.tutorial.TutorialManager
import com.mydrishti.co.`in`.activities.tutorial.TutorialConstants

=== STEP 2: Add Tutorial Integration Property ===

Add this property to the ChartTypeSelectionDialog class:

private var tutorialIntegration: ChartTypeSelectionTutorialIntegration? = null

=== STEP 3: Modify Constructor ===

Update the constructor to support tutorial integration:

class ChartTypeSelectionDialog(
    context: Context,
    private val onChartTypeSelected: (ChartType) -> Unit,
    private val enableTutorial: Boolean = false  // Add this parameter
) : Dialog(context) {

=== STEP 4: Initialize Tutorial Integration ===

Add this method to initialize tutorial integration:

private fun initializeTutorialIntegration() {
    if (enableTutorial) {
        tutorialIntegration = ChartTypeSelectionTutorialIntegration.create(context)
        
        // Check if tutorial should be active
        if (tutorialIntegration?.shouldShowTutorial() == true) {
            // Start tutorial after dialog is fully rendered
            window?.decorView?.post {
                tutorialIntegration?.startStep2Tutorial(this)
            }
        }
    }
}

=== STEP 5: Modify setupUI Method ===

Update the setupUI method to include tutorial integration:

private fun setupUI() {
    // ... existing code ...
    
    // Set up chart type selection buttons with tutorial awareness
    cardBarChartDaily.setOnClickListener {
        handleChartTypeSelection(ChartType.BAR_DAILY)
    }
    
    cardBarChartHourly.setOnClickListener {
        handleChartTypeSelection(ChartType.BAR_HOURLY)
    }
    
    cardGaugeChart.setOnClickListener {
        handleChartTypeSelection(ChartType.GAUGE)
    }
    
    cardMetricChart.setOnClickListener {
        handleChartTypeSelection(ChartType.METRIC)
    }
    
    // ... existing close button code ...
    
    // Initialize tutorial integration after UI setup
    initializeTutorialIntegration()
}

=== STEP 6: Add Chart Type Selection Handler ===

Add this method to handle chart type selection with tutorial support:

private fun handleChartTypeSelection(chartType: ChartType) {
    // Handle tutorial validation if tutorial is active
    if (enableTutorial && tutorialIntegration?.shouldShowTutorial() == true) {
        // Validate selection for tutorial
        val isValidSelection = tutorialIntegration?.validateGaugeChartSelection(chartType) ?: false
        
        // Handle tutorial progression
        tutorialIntegration?.handleTutorialProgression(chartType)
        
        // Log tutorial interaction
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            android.util.Log.d(TutorialConstants.DEBUG_LOG_TAG, 
                "Chart type selected during tutorial: $chartType, valid: $isValidSelection")
        }
    }
    
    // Always proceed with normal selection
    onChartTypeSelected(chartType)
    dismiss()
}

=== STEP 7: Add Tutorial-Aware Factory Method ===

Add this companion object method for creating tutorial-aware dialogs:

companion object {
    // ... existing showAsAlertDialog method ...
    
    /**
     * Creates a tutorial-aware ChartTypeSelectionDialog
     */
    fun createWithTutorialSupport(
        context: Context, 
        onChartTypeSelected: (ChartType) -> Unit,
        enableTutorial: Boolean = false
    ): ChartTypeSelectionDialog {
        return ChartTypeSelectionDialog(context, onChartTypeSelected, enableTutorial)
    }
    
    /**
     * Creates dialog specifically for tutorial Step 2
     */
    fun createForTutorialStep2(
        context: Context,
        onChartTypeSelected: (ChartType) -> Unit
    ): ChartTypeSelectionDialog {
        return ChartTypeSelectionDialog(context, onChartTypeSelected, enableTutorial = true)
    }
}

=== STEP 8: Add Cleanup in onDetachedFromWindow ===

Add this method to handle cleanup:

override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    tutorialIntegration?.cleanup()
    tutorialIntegration = null
}

=== STEP 9: Update MainActivity Integration ===

Update MainActivity.kt to use tutorial-aware dialog creation:

// Replace existing showChartTypeSelectionDialog() method with:
private fun showChartTypeSelectionDialog() {
    // Check if tutorial is active
    val tutorialManager = TutorialManager.create(this)
    val isTutorialActive = tutorialManager.isActive()
    
    // Create tutorial-aware dialog
    val dialog = ChartTypeSelectionDialog.createWithTutorialSupport(
        context = this,
        onChartTypeSelected = { chartType ->
            // Navigate to site selection
            navigateToSiteSelection(chartType)
        },
        enableTutorial = isTutorialActive
    )
    
    dialog.show()
}

=== STEP 10: Update Step 1 Integration ===

Update TutorialStep1Implementation.kt to use tutorial-aware dialog:

// Replace showChartTypeSelectionDialog() method with:
private fun showChartTypeSelectionDialog() {
    try {
        val dialog = ChartTypeSelectionDialog.createForTutorialStep2(activity) { chartType ->
            handleChartTypeSelection(chartType)
        }
        dialog.show()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial-aware Chart Type Selection Dialog opened")
        }
        
    } catch (e: Exception) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error opening tutorial-aware dialog", e)
        }
        handleDialogError()
    }
}

=== TESTING CHECKLIST ===

After applying these modifications, test the following:

1. ✅ Normal dialog functionality (without tutorial)
2. ✅ Tutorial Step 2 activation when tutorial is active
3. ✅ Gauge chart highlighting and selection validation
4. ✅ Non-gauge chart selection handling during tutorial
5. ✅ Tutorial progression to Step 3 after selection
6. ✅ Error handling and fallback scenarios
7. ✅ Dialog cleanup and memory management
8. ✅ Analytics and logging functionality

=== NOTES ===

- The tutorial integration is designed to be non-intrusive
- Normal dialog functionality is preserved when tutorial is not active
- All tutorial-specific code is conditionally executed
- Error handling ensures the dialog works even if tutorial components fail
- The integration supports both guided (gauge chart) and flexible selection modes

*/

/**
 * This class serves as documentation only and should not be instantiated.
 * Apply the modifications above to the actual ChartTypeSelectionDialog.kt file.
 */
class ChartTypeSelectionModificationGuide private constructor() {
    init {
        throw UnsupportedOperationException("This class is for documentation only")
    }
}