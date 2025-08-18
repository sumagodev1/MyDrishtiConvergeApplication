package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.util.Log
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.dialogs.ChartTypeSelectionDialog

/**
 * Implementation of Tutorial Step 1: FAB highlight in MainActivity
 * This class handles the first tutorial step - highlighting the FAB and guiding users to tap it
 */
class TutorialStep1Implementation(private val activity: Activity) {
    
    private var fabAddChart: FloatingActionButton? = null
    private var tutorialManager: TutorialManager? = null
    private var originalFabClickListener: View.OnClickListener? = null
    
    /**
     * Initializes Step 1 of the tutorial - FAB highlight
     */
    fun initializeStep1(tutorialManager: TutorialManager) {
        this.tutorialManager = tutorialManager
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Initializing Tutorial Step 1: FAB highlight")
        }
        
        // Find the FAB
        fabAddChart = activity.findViewById(R.id.fab_add_chart)
        
        if (fabAddChart == null) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.w(TutorialConstants.DEBUG_LOG_TAG, "FAB not found - Step 1 cannot proceed")
            }
            handleFabNotFound()
            return
        }
        
        // Setup FAB for tutorial
        setupFabForTutorial()
        
        // Start the tutorial step
        startStep1Tutorial()
    }
    
    /**
     * Sets up the FAB for tutorial interaction
     */
    private fun setupFabForTutorial() {
        fabAddChart?.let { fab ->
            // Store original click listener if it exists
            originalFabClickListener = fab.tag as? View.OnClickListener
            
            // Set tutorial-specific click listener
            fab.setOnClickListener { view ->
                handleFabClickDuringTutorial(view)
            }
            
            // Ensure FAB is visible and enabled
            fab.visibility = View.VISIBLE
            fab.isEnabled = true
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "FAB setup complete for tutorial")
            }
        }
    }
    
    /**
     * Starts the Step 1 tutorial with FAB highlighting
     */
    private fun startStep1Tutorial() {
        fabAddChart?.let { fab ->
            // Create tutorial step configuration
            val step1Config = TutorialConfigFactory.createFirstTimeUserTutorial()
                .getStepById(TutorialConstants.STEP_WELCOME_FAB)
            
            if (step1Config == null) {
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.e(TutorialConstants.DEBUG_LOG_TAG, "Step 1 configuration not found")
                }
                return
            }
            
            // Show tutorial overlay for FAB
            tutorialManager?.showModernTapTarget(step1Config)
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 1 tutorial started with FAB highlight")
            }
        }
    }
    
    /**
     * Handles FAB click during tutorial
     */
    fun handleFabClickDuringTutorial(view: View) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "FAB clicked during tutorial")
        }
        
        // Record tutorial interaction
        recordStep1Completion()
        
        // Proceed to next tutorial step (Chart Type Selection)
        proceedToStep2()
    }
    
    /**
     * Records Step 1 completion
     */
    private fun recordStep1Completion() {
        // This would typically be handled by TutorialManager
        // but we can add specific Step 1 analytics here
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 1 completed - FAB tapped")
        }
    }
    
    /**
     * Proceeds to Step 2 - Chart Type Selection
     */
    private fun proceedToStep2() {
        // Notify tutorial manager that Step 1 is complete
        tutorialManager?.markStepCompleted(TutorialConstants.STEP_WELCOME_FAB)
        
        // Start Step 2 with tutorial integration
        startStep2WithTutorial()
    }
    
    /**
     * Starts Step 2 with tutorial integration
     */
    private fun startStep2WithTutorial() {
        try {
            // Create Step 2 implementation
            val step2Implementation = TutorialStep2Implementation(activity)
            
            // Initialize Step 2 with tutorial manager
            tutorialManager?.let { manager ->
                step2Implementation.initializeStep2(manager)
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 2 tutorial started with integration")
            }
            
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error starting Step 2 tutorial", e)
            }
            // Fallback to normal dialog
            showChartTypeSelectionDialog()
        }
    }
    
    /**
     * Shows the Chart Type Selection Dialog (fallback method)
     */
    private fun showChartTypeSelectionDialog() {
        try {
            val dialog = ChartTypeSelectionDialog(activity) { chartType ->
                // Handle chart type selection
                handleChartTypeSelection(chartType)
            }
            dialog.show()
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Fallback Chart Type Selection Dialog opened")
            }
            
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error opening Chart Type Selection Dialog", e)
            }
            handleDialogError()
        }
    }
    
    /**
     * Handles chart type selection from the dialog
     */
    private fun handleChartTypeSelection(chartType: Any) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Chart type selected: $chartType")
        }
        
        // This would continue to Step 3 (Site Selection)
        // For now, we'll just log the completion
        recordChartTypeSelection(chartType)
    }
    
    /**
     * Records chart type selection for analytics
     */
    private fun recordChartTypeSelection(chartType: Any) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Recording chart type selection: $chartType")
        }
    }
    
    /**
     * Handles the case where FAB is not found
     */
    private fun handleFabNotFound() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.w(TutorialConstants.DEBUG_LOG_TAG, "FAB not found - showing fallback tutorial message")
        }
        
        // Show fallback tutorial message
        showFallbackTutorialMessage()
    }
    
    /**
     * Shows fallback tutorial message when FAB is not found
     */
    private fun showFallbackTutorialMessage() {
        // Create a generic tutorial overlay without specific target
        val fallbackStep = TutorialStep(
            id = "fallback_welcome",
            title = "Welcome to MyDrishti!",
            description = "Look for the + button to create your first chart",
            targetViewId = null,
            animationType = AnimationType.FADE_SCALE,
            duration = 4000L,
            explanatoryText = "The + button is usually located at the bottom right of the screen"
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
        
        // Show error message or continue tutorial with fallback
        showDialogErrorMessage()
    }
    
    /**
     * Shows error message when dialog fails to open
     */
    private fun showDialogErrorMessage() {
        val errorStep = TutorialStep(
            id = "dialog_error",
            title = "Tutorial Step Unavailable",
            description = "The next tutorial step is temporarily unavailable. You can continue exploring the app.",
            targetViewId = null,
            animationType = AnimationType.FADE,
            duration = 3000L
        )
        
        tutorialManager?.showModernTapTarget(errorStep)
    }
    
    /**
     * Restores original FAB functionality after tutorial
     */
    fun restoreOriginalFabFunctionality() {
        fabAddChart?.let { fab ->
            // Restore original click listener if it existed
            originalFabClickListener?.let { originalListener ->
                fab.setOnClickListener(originalListener)
            } ?: run {
                // Set default FAB functionality if no original listener
                fab.setOnClickListener { view ->
                    handleDefaultFabClick(view)
                }
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Original FAB functionality restored")
            }
        }
    }
    
    /**
     * Handles default FAB click (when no original listener exists)
     */
    private fun handleDefaultFabClick(view: View) {
        // Default behavior: open chart type selection
        showChartTypeSelectionDialog()
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        restoreOriginalFabFunctionality()
        fabAddChart = null
        tutorialManager = null
        originalFabClickListener = null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Step 1 implementation cleaned up")
        }
    }
    
    /**
     * Gets Step 1 analytics
     */
    fun getStep1Analytics(): Map<String, Any> {
        return mapOf(
            "fab_found" to (fabAddChart != null),
            "fab_visible" to (fabAddChart?.visibility == View.VISIBLE),
            "fab_enabled" to (fabAddChart?.isEnabled == true),
            "has_original_listener" to (originalFabClickListener != null)
        )
    }
}