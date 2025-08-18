package com.mydrishti.co.`in`.activities.tutorial

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.*

/**
 * Debug tools and development utilities for the tutorial system
 */
class TutorialDebugTools(private val context: Context) {
    
    private var tutorialManager: TutorialManager? = null
    private var debugDialog: AlertDialog? = null
    
    /**
     * Shows the debug UI for testing tutorial steps individually
     */
    fun showDebugUI(activity: Activity) {
        if (!TutorialConstants.DEBUG_MODE_ENABLED) {
            Toast.makeText(context, "Debug mode is not enabled", Toast.LENGTH_SHORT).show()
            return
        }
        
        tutorialManager = TutorialManager.create(activity)
        
        val dialogView = createDebugDialogView()
        
        debugDialog = AlertDialog.Builder(context)
            .setTitle("Tutorial Debug Tools")
            .setView(dialogView)
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        
        debugDialog?.show()
    }
    
    /**
     * Creates the debug dialog view
     */
    private fun createDebugDialogView(): View {
        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }
        
        // Debug mode toggle
        addDebugModeToggle(dialogView)
        
        // Tutorial step testing
        addStepTestingSection(dialogView)
        
        // State management tools
        addStateManagementSection(dialogView)
        
        // Analytics and logging
        addAnalyticsSection(dialogView)
        
        // Configuration testing
        addConfigurationTestingSection(dialogView)
        
        return dialogView
    }
    
    /**
     * Adds debug mode toggle
     */
    private fun addDebugModeToggle(parent: LinearLayout) {
        val toggleLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
        }
        
        val label = TextView(context).apply {
            text = "Debug Mode: "
            textSize = 16f
        }
        
        val toggle = Switch(context).apply {
            isChecked = TutorialConstants.DEBUG_MODE_ENABLED
            setOnCheckedChangeListener { _, isChecked ->
                // This would require modifying TutorialConstants to be mutable
                // For now, just show the current state
                Toast.makeText(context, "Debug mode: $isChecked", Toast.LENGTH_SHORT).show()
            }
        }
        
        toggleLayout.addView(label)
        toggleLayout.addView(toggle)
        parent.addView(toggleLayout)
        
        // Add separator
        parent.addView(createSeparator())
    }
    
    /**
     * Adds step testing section
     */
    private fun addStepTestingSection(parent: LinearLayout) {
        val sectionLabel = TextView(context).apply {
            text = "Test Individual Steps"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }
        parent.addView(sectionLabel)
        
        val steps = listOf(
            "Step 1: FAB Highlight" to TutorialConstants.STEP_WELCOME_FAB,
            "Step 2: Chart Type Selection" to TutorialConstants.STEP_CHART_TYPE_SELECTION,
            "Step 3: Site Selection" to TutorialConstants.STEP_SITE_SELECTION,
            "Step 4: Parameter Selection" to TutorialConstants.STEP_PARAMETER_SELECTION,
            "Step 5: Save Chart" to TutorialConstants.STEP_SAVE_CHART,
            "Step 6: Completion" to TutorialConstants.STEP_COMPLETION_CELEBRATION
        )
        
        steps.forEach { (stepName, stepId) ->
            val button = Button(context).apply {
                text = stepName
                setOnClickListener {
                    testIndividualStep(stepId)
                }
            }
            parent.addView(button)
        }
        
        parent.addView(createSeparator())
    }
    
    /**
     * Adds state management section
     */
    private fun addStateManagementSection(parent: LinearLayout) {
        val sectionLabel = TextView(context).apply {
            text = "State Management"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }
        parent.addView(sectionLabel)
        
        // Show current state button
        val showStateButton = Button(context).apply {
            text = "Show Current State"
            setOnClickListener {
                showCurrentTutorialState()
            }
        }
        parent.addView(showStateButton)
        
        // Reset state button
        val resetStateButton = Button(context).apply {
            text = "Reset Tutorial State"
            setOnClickListener {
                resetTutorialState()
            }
        }
        parent.addView(resetStateButton)
        
        // Force first-time user button
        val forceFirstTimeButton = Button(context).apply {
            text = "Force First-Time User"
            setOnClickListener {
                forceFirstTimeUser()
            }
        }
        parent.addView(forceFirstTimeButton)
        
        parent.addView(createSeparator())
    }
    
    /**
     * Adds analytics section
     */
    private fun addAnalyticsSection(parent: LinearLayout) {
        val sectionLabel = TextView(context).apply {
            text = "Analytics & Logging"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }
        parent.addView(sectionLabel)
        
        // Show analytics button
        val showAnalyticsButton = Button(context).apply {
            text = "Show Analytics"
            setOnClickListener {
                showTutorialAnalytics()
            }
        }
        parent.addView(showAnalyticsButton)
        
        // Export logs button
        val exportLogsButton = Button(context).apply {
            text = "Export Debug Logs"
            setOnClickListener {
                exportDebugLogs()
            }
        }
        parent.addView(exportLogsButton)
        
        parent.addView(createSeparator())
    }
    
    /**
     * Adds configuration testing section
     */
    private fun addConfigurationTestingSection(parent: LinearLayout) {
        val sectionLabel = TextView(context).apply {
            text = "Configuration Testing"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }
        parent.addView(sectionLabel)
        
        // Validate configuration button
        val validateConfigButton = Button(context).apply {
            text = "Validate Configuration"
            setOnClickListener {
                validateTutorialConfiguration()
            }
        }
        parent.addView(validateConfigButton)
        
        // Test animations button
        val testAnimationsButton = Button(context).apply {
            text = "Test Animations"
            setOnClickListener {
                testTutorialAnimations()
            }
        }
        parent.addView(testAnimationsButton)
    }
    
    /**
     * Creates a separator view
     */
    private fun createSeparator(): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            setBackgroundColor(android.graphics.Color.GRAY)
        }
    }
    
    /**
     * Tests an individual tutorial step
     */
    private fun testIndividualStep(stepId: String) {
        Log.d(TutorialConstants.DEBUG_LOG_TAG, "Testing individual step: $stepId")
        
        val config = TutorialConfigFactory.createFirstTimeUserTutorial()
        val step = config.getStepById(stepId)
        
        if (step != null) {
            tutorialManager?.showModernTapTarget(step)
            Toast.makeText(context, "Testing step: ${step.title}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Step not found: $stepId", Toast.LENGTH_SHORT).show()
        }
        
        debugDialog?.dismiss()
    }
    
    /**
     * Shows current tutorial state
     */
    private fun showCurrentTutorialState() {
        val stateManager = TutorialStateManager(context)
        val currentState = stateManager.getCurrentTutorialState()
        
        val stateInfo = buildString {
            appendLine("Tutorial State Information:")
            appendLine("Active: ${currentState.isActive}")
            appendLine("Current Step: ${currentState.currentStepId ?: "None"}")
            appendLine("Completion Status: ${currentState.completionStatus}")
            appendLine("Completed Steps: ${currentState.completedSteps.size}")
            appendLine("Skip Count: ${currentState.skipCount}")
            appendLine("Onboarding Completed: ${currentState.onboardingCompleted}")
            appendLine("Start Time: ${currentState.startTime}")
            appendLine("Last Interaction: ${currentState.lastInteractionTime}")
        }
        
        AlertDialog.Builder(context)
            .setTitle("Current Tutorial State")
            .setMessage(stateInfo)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Resets tutorial state
     */
    private fun resetTutorialState() {
        AlertDialog.Builder(context)
            .setTitle("Reset Tutorial State")
            .setMessage("This will reset all tutorial progress. Are you sure?")
            .setPositiveButton("Reset") { _, _ ->
                val stateManager = TutorialStateManager(context)
                stateManager.resetTutorialState()
                Toast.makeText(context, "Tutorial state reset", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Forces first-time user state
     */
    private fun forceFirstTimeUser() {
        val detector = FirstTimeUserDetector(context)
        detector.resetFirstTimeUserData()
        
        val stateManager = TutorialStateManager(context)
        stateManager.resetTutorialState()
        
        Toast.makeText(context, "Forced first-time user state", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Shows tutorial analytics
     */
    private fun showTutorialAnalytics() {
        val stateManager = TutorialStateManager(context)
        val analytics = stateManager.getTutorialAnalytics()
        
        val analyticsInfo = buildString {
            appendLine("Tutorial Analytics:")
            analytics.forEach { (key, value) ->
                appendLine("$key: $value")
            }
        }
        
        AlertDialog.Builder(context)
            .setTitle("Tutorial Analytics")
            .setMessage(analyticsInfo)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Exports debug logs
     */
    private fun exportDebugLogs() {
        val logs = buildString {
            appendLine("=== Tutorial Debug Logs ===")
            appendLine("Export Time: ${java.util.Date()}")
            appendLine()
            
            // Add current state
            val stateManager = TutorialStateManager(context)
            val currentState = stateManager.getCurrentTutorialState()
            appendLine("Current State:")
            appendLine("- Active: ${currentState.isActive}")
            appendLine("- Current Step: ${currentState.currentStepId}")
            appendLine("- Completion Status: ${currentState.completionStatus}")
            appendLine("- Completed Steps: ${currentState.completedSteps}")
            appendLine()
            
            // Add analytics
            val analytics = stateManager.getTutorialAnalytics()
            appendLine("Analytics:")
            analytics.forEach { (key, value) ->
                appendLine("- $key: $value")
            }
            appendLine()
            
            // Add configuration info
            val config = TutorialConfigFactory.createFirstTimeUserTutorial()
            appendLine("Configuration:")
            appendLine("- Tutorial ID: ${config.tutorialId}")
            appendLine("- Version: ${config.version}")
            appendLine("- Total Steps: ${config.steps.size}")
            appendLine("- Debug Mode: ${config.debugMode}")
        }
        
        // For now, just show in dialog. In real implementation, could save to file
        AlertDialog.Builder(context)
            .setTitle("Debug Logs")
            .setMessage(logs)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Validates tutorial configuration
     */
    private fun validateTutorialConfiguration() {
        val config = TutorialConfigFactory.createFirstTimeUserTutorial()
        val validationResults = mutableListOf<String>()
        
        // Validate overall configuration
        if (config.isValid()) {
            validationResults.add("✓ Configuration is valid")
        } else {
            validationResults.add("✗ Configuration is invalid")
        }
        
        // Validate individual steps
        config.steps.forEach { step ->
            if (step.isValid()) {
                validationResults.add("✓ Step ${step.id}: Valid")
            } else {
                validationResults.add("✗ Step ${step.id}: Invalid")
            }
        }
        
        // Check for missing steps
        val expectedSteps = listOf(
            TutorialConstants.STEP_WELCOME_FAB,
            TutorialConstants.STEP_CHART_TYPE_SELECTION,
            TutorialConstants.STEP_SITE_SELECTION,
            TutorialConstants.STEP_PARAMETER_SELECTION,
            TutorialConstants.STEP_SAVE_CHART,
            TutorialConstants.STEP_COMPLETION_CELEBRATION
        )
        
        val actualStepIds = config.steps.map { it.id }
        expectedSteps.forEach { expectedId ->
            if (actualStepIds.contains(expectedId)) {
                validationResults.add("✓ Step $expectedId: Present")
            } else {
                validationResults.add("✗ Step $expectedId: Missing")
            }
        }
        
        val validationInfo = validationResults.joinToString("\n")
        
        AlertDialog.Builder(context)
            .setTitle("Configuration Validation")
            .setMessage(validationInfo)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Tests tutorial animations
     */
    private fun testTutorialAnimations() {
        val animationTypes = AnimationType.values()
        val animationNames = animationTypes.map { it.name }
        
        AlertDialog.Builder(context)
            .setTitle("Test Animations")
            .setItems(animationNames.toTypedArray()) { _, which ->
                val selectedAnimation = animationTypes[which]
                testAnimation(selectedAnimation)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Tests a specific animation
     */
    private fun testAnimation(animationType: AnimationType) {
        if (context is Activity) {
            val rootView = context.findViewById<android.view.ViewGroup>(android.R.id.content)
            val animationController = TutorialAnimationController(context)
            
            val animation = animationController.createAnimationForType(
                animationType,
                rootView,
                2000L
            )
            
            animationController.startAnimation(animation)
            Toast.makeText(context, "Testing ${animationType.name} animation", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Gets debug information
     */
    fun getDebugInfo(): Map<String, Any> {
        val stateManager = TutorialStateManager(context)
        val detector = FirstTimeUserDetector(context)
        
        return mapOf(
            "debug_mode_enabled" to TutorialConstants.DEBUG_MODE_ENABLED,
            "current_state" to stateManager.getCurrentTutorialState(),
            "user_analytics" to detector.getUserAnalytics(),
            "tutorial_analytics" to stateManager.getTutorialAnalytics(),
            "configuration_valid" to TutorialConfigFactory.createFirstTimeUserTutorial().isValid(),
            "debug_tools_available" to true
        )
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        debugDialog?.dismiss()
        debugDialog = null
        tutorialManager = null
    }
}