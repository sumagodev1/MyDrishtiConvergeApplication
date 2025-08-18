package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.util.Log
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.dialogs.ChartTypeSelectionDialog

/**
 * Integration code for MainActivity to support Tutorial Step 1
 * This class provides the missing FAB setup and tutorial integration
 */
class MainActivityStep1Integration(private val activity: Activity) {
    
    private var fabAddChart: FloatingActionButton? = null
    private var tutorialIntegration: MainActivityIntegration? = null
    private var step1Implementation: TutorialStep1Implementation? = null
    
    /**
     * Sets up the FAB functionality that appears to be missing from MainActivity
     * This method should be called in MainActivity's onCreate() after other setup methods
     */
    fun setupAddChartButton() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Setting up Add Chart button (FAB)")
        }
        
        // Find the FAB
        fabAddChart = activity.findViewById(R.id.fab_add_chart)
        
        if (fabAddChart == null) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.w(TutorialConstants.DEBUG_LOG_TAG, "FAB not found in layout")
            }
            return
        }
        
        // Set up normal FAB click listener
        fabAddChart?.setOnClickListener { view ->
            handleFabClick(view)
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "FAB setup complete")
        }
    }
    
    /**
     * Handles FAB click - opens chart type selection dialog
     */
    private fun handleFabClick(view: View) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "FAB clicked - opening chart type selection")
        }
        
        try {
            // Check if tutorial is active and should handle this click
            if (isTutorialActive()) {
                handleTutorialFabClick(view)
            } else {
                handleNormalFabClick(view)
            }
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error handling FAB click", e)
            }
            // Fallback to normal behavior
            handleNormalFabClick(view)
        }
    }
    
    /**
     * Handles FAB click during tutorial
     */
    private fun handleTutorialFabClick(view: View) {
        step1Implementation?.let { step1 ->
            // Let Step 1 implementation handle the tutorial click
            step1.handleFabClickDuringTutorial(view)
        } ?: run {
            // Fallback to normal click if Step 1 not initialized
            handleNormalFabClick(view)
        }
    }
    
    /**
     * Handles normal FAB click (not during tutorial)
     */
    private fun handleNormalFabClick(view: View) {
        showChartTypeSelectionDialog()
    }
    
    /**
     * Shows the Chart Type Selection Dialog
     */
    private fun showChartTypeSelectionDialog() {
        try {
            val dialog = ChartTypeSelectionDialog(activity) { chartType ->
                handleChartTypeSelection(chartType)
            }
            dialog.show()
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart Type Selection Dialog opened")
            }
            
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error opening Chart Type Selection Dialog", e)
            }
            // Could show error message to user here
        }
    }
    
    /**
     * Handles chart type selection
     */
    private fun handleChartTypeSelection(chartType: Any) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart type selected: $chartType")
        }
        
        // This would typically navigate to site selection
        // For now, just log the selection
        // In a real implementation, this would continue the chart creation flow
    }
    
    /**
     * Initializes tutorial integration
     */
    fun initializeTutorialIntegration() {
        tutorialIntegration = MainActivityIntegration.create(activity)
        step1Implementation = TutorialStep1Implementation(activity)
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Tutorial integration initialized")
        }
    }
    
    /**
     * Starts Step 1 tutorial if conditions are met
     */
    fun startStep1TutorialIfNeeded() {
        tutorialIntegration?.let { integration ->
            if (integration.isTutorialActive()) {
                val tutorialManager = TutorialManager.create(activity)
                step1Implementation?.initializeStep1(tutorialManager)
                
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 1 tutorial started")
                }
            }
        }
    }
    
    /**
     * Checks if tutorial is currently active
     */
    private fun isTutorialActive(): Boolean {
        return tutorialIntegration?.isTutorialActive() ?: false
    }
    
    /**
     * Handles tutorial completion for Step 1
     */
    fun handleStep1Completion() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 1 tutorial completed")
        }
        
        // Restore normal FAB functionality
        step1Implementation?.restoreOriginalFabFunctionality()
    }
    
    /**
     * Gets FAB reference for tutorial highlighting
     */
    fun getFabForTutorial(): FloatingActionButton? {
        return fabAddChart
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        step1Implementation?.cleanup()
        step1Implementation = null
        tutorialIntegration = null
        fabAddChart = null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "MainActivity Step 1 integration cleaned up")
        }
    }
    
    /**
     * Gets Step 1 analytics
     */
    fun getStep1Analytics(): Map<String, Any> {
        val step1Analytics = step1Implementation?.getStep1Analytics() ?: emptyMap()
        val integrationAnalytics = mapOf(
            "tutorial_integration_initialized" to (tutorialIntegration != null),
            "step1_implementation_initialized" to (step1Implementation != null),
            "is_tutorial_active" to isTutorialActive()
        )
        
        return step1Analytics + integrationAnalytics
    }
}