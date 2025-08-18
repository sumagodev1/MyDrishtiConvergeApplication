package com.mydrishti.co.`in`.activities.tutorial

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

/**
 * Manages accessibility support for the tutorial system
 */
class TutorialAccessibilityManager(private val context: Context) {
    
    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    
    /**
     * Checks if accessibility services are enabled
     */
    fun isAccessibilityEnabled(): Boolean {
        return accessibilityManager.isEnabled
    }
    
    /**
     * Checks if screen reader is active
     */
    fun isScreenReaderActive(): Boolean {
        if (!isAccessibilityEnabled()) return false
        
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_SPOKEN
        )
        
        return enabledServices.isNotEmpty()
    }
    
    /**
     * Configures accessibility for tutorial overlay
     */
    fun configureTutorialOverlayAccessibility(overlayView: View, step: TutorialStep) {
        // Set content description
        val contentDescription = buildAccessibilityDescription(step)
        overlayView.contentDescription = contentDescription
        
        // Make view focusable for accessibility
        ViewCompat.setImportantForAccessibility(overlayView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)
        
        // Set accessibility actions
        ViewCompat.setAccessibilityDelegate(overlayView, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                
                // Add custom actions
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK,
                        "Continue tutorial"
                    )
                )
                
                if (step.isSkippable) {
                    info.addAction(
                        AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                            AccessibilityNodeInfoCompat.ACTION_LONG_CLICK,
                            "Skip tutorial step"
                        )
                    )
                }
                
                // Set role description
                info.roleDescription = "Tutorial step"
                
                // Set state description
                info.stateDescription = "Step ${getStepNumber(step.id)} of 6"
            }
            
            override fun performAccessibilityAction(host: View, action: Int, args: android.os.Bundle?): Boolean {
                return when (action) {
                    AccessibilityNodeInfoCompat.ACTION_CLICK -> {
                        handleAccessibilityClick(step)
                        true
                    }
                    AccessibilityNodeInfoCompat.ACTION_LONG_CLICK -> {
                        if (step.isSkippable) {
                            handleAccessibilitySkip(step)
                        }
                        true
                    }
                    else -> super.performAccessibilityAction(host, action, args)
                }
            }
        })
        
        // Request accessibility focus
        if (isScreenReaderActive()) {
            overlayView.post {
                overlayView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Accessibility configured for step: ${step.id}")
        }
    }
    
    /**
     * Builds accessibility description for tutorial step
     */
    private fun buildAccessibilityDescription(step: TutorialStep): String {
        return buildString {
            append("Tutorial step: ${step.title}. ")
            append(step.description)
            if (!step.explanatoryText.isNullOrBlank()) {
                append(" ")
                append(step.explanatoryText)
            }
            if (step.isSkippable) {
                append(" This step can be skipped by long pressing.")
            }
            append(" Tap to continue.")
        }
    }
    
    /**
     * Gets step number from step ID
     */
    private fun getStepNumber(stepId: String): Int {
        return when (stepId) {
            TutorialConstants.STEP_WELCOME_FAB -> 1
            TutorialConstants.STEP_CHART_TYPE_SELECTION -> 2
            TutorialConstants.STEP_SITE_SELECTION -> 3
            TutorialConstants.STEP_PARAMETER_SELECTION -> 4
            TutorialConstants.STEP_SAVE_CHART -> 5
            TutorialConstants.STEP_COMPLETION_CELEBRATION -> 6
            else -> 0
        }
    }
    
    /**
     * Handles accessibility click action
     */
    private fun handleAccessibilityClick(step: TutorialStep) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Accessibility click for step: ${step.id}")
        }
        
        // Announce step completion
        announceToScreenReader("Tutorial step completed. Moving to next step.")
    }
    
    /**
     * Handles accessibility skip action
     */
    private fun handleAccessibilitySkip(step: TutorialStep) {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Accessibility skip for step: ${step.id}")
        }
        
        // Announce skip action
        announceToScreenReader("Tutorial step skipped.")
    }
    
    /**
     * Configures accessibility for target view highlighting
     */
    fun configureTargetViewAccessibility(targetView: View, step: TutorialStep) {
        // Enhance target view accessibility
        val originalDescription = targetView.contentDescription
        val enhancedDescription = buildString {
            if (!originalDescription.isNullOrBlank()) {
                append(originalDescription)
                append(". ")
            }
            append("Tutorial highlight: ${step.title}. ${step.description}")
        }
        
        targetView.contentDescription = enhancedDescription
        
        // Ensure view is accessible
        ViewCompat.setImportantForAccessibility(targetView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)
        
        // Request focus for screen readers
        if (isScreenReaderActive()) {
            targetView.post {
                targetView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
            }
        }
    }
    
    /**
     * Provides alternative navigation methods for accessibility
     */
    fun setupAlternativeNavigation(overlayView: View, step: TutorialStep, onNext: () -> Unit, onSkip: () -> Unit) {
        // Add keyboard navigation support
        overlayView.isFocusable = true
        overlayView.isFocusableInTouchMode = true
        
        // Handle key events for navigation
        overlayView.setOnKeyListener { _, keyCode, event ->
            when (keyCode) {
                android.view.KeyEvent.KEYCODE_ENTER,
                android.view.KeyEvent.KEYCODE_SPACE -> {
                    if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                        onNext()
                        announceToScreenReader("Moving to next tutorial step")
                    }
                    true
                }
                android.view.KeyEvent.KEYCODE_ESCAPE -> {
                    if (event.action == android.view.KeyEvent.ACTION_DOWN && step.isSkippable) {
                        onSkip()
                        announceToScreenReader("Tutorial step skipped")
                    }
                    true
                }
                else -> false
            }
        }
        
        // Add gesture support for accessibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setupAccessibilityGestures(overlayView, step, onNext, onSkip)
        }
    }
    
    /**
     * Sets up accessibility gestures (API 23+)
     */
    private fun setupAccessibilityGestures(overlayView: View, step: TutorialStep, onNext: () -> Unit, onSkip: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // This would require more complex gesture handling
            // For now, we'll just ensure basic touch accessibility
            overlayView.isClickable = true
            overlayView.isFocusable = true
        }
    }
    
    /**
     * Announces message to screen reader
     */
    fun announceToScreenReader(message: String) {
        if (!isScreenReaderActive()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
            event.text.add(message)
            accessibilityManager.sendAccessibilityEvent(event)
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Screen reader announcement: $message")
        }
    }
    
    /**
     * Manages focus during tutorial steps
     */
    fun manageFocusDuringTutorial(currentView: View?, nextView: View?) {
        if (!isScreenReaderActive()) return
        
        // Clear focus from current view
        currentView?.clearFocus()
        
        // Set focus to next view
        nextView?.let { view ->
            view.requestFocus()
            view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Focus managed for accessibility")
        }
    }
    
    /**
     * Provides audio cues for tutorial steps
     */
    fun provideAudioCues(step: TutorialStep) {
        if (!isScreenReaderActive()) return
        
        val audioDescription = buildString {
            append("Tutorial step ${getStepNumber(step.id)}: ")
            append(step.title)
            append(". ")
            append(step.description)
            if (!step.explanatoryText.isNullOrBlank()) {
                append(" ")
                append(step.explanatoryText)
            }
        }
        
        announceToScreenReader(audioDescription)
    }
    
    /**
     * Tests accessibility with screen reader services
     */
    fun testAccessibilityWithScreenReader(): Map<String, Any> {
        val isEnabled = isAccessibilityEnabled()
        val isScreenReaderActive = isScreenReaderActive()
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        
        return mapOf(
            "accessibility_enabled" to isEnabled,
            "screen_reader_active" to isScreenReaderActive,
            "enabled_services_count" to enabledServices.size,
            "touch_exploration_enabled" to accessibilityManager.isTouchExplorationEnabled,
            "accessibility_support_available" to true
        )
    }
    
    /**
     * Gets accessibility configuration
     */
    fun getAccessibilityConfiguration(): Map<String, Any> {
        return mapOf(
            "accessibility_enabled" to isAccessibilityEnabled(),
            "screen_reader_active" to isScreenReaderActive(),
            "touch_exploration_enabled" to accessibilityManager.isTouchExplorationEnabled,
            "high_text_contrast_enabled" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                accessibilityManager.isHighTextContrastEnabled
            } else false,
            "accessibility_support_level" to getAccessibilitySupportLevel()
        )
    }
    
    /**
     * Gets accessibility support level
     */
    private fun getAccessibilitySupportLevel(): String {
        return when {
            !isAccessibilityEnabled() -> "none"
            isScreenReaderActive() -> "full"
            accessibilityManager.isTouchExplorationEnabled -> "touch_exploration"
            else -> "basic"
        }
    }
    
    /**
     * Validates accessibility implementation
     */
    fun validateAccessibilityImplementation(): List<String> {
        val issues = mutableListOf<String>()
        
        // Check if accessibility is properly configured
        if (!isAccessibilityEnabled()) {
            issues.add("Accessibility services not enabled")
        }
        
        // Check API level support
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            issues.add("Limited accessibility support on API level ${Build.VERSION.SDK_INT}")
        }
        
        // Check for screen reader support
        if (isAccessibilityEnabled() && !isScreenReaderActive()) {
            issues.add("Screen reader not active but accessibility is enabled")
        }
        
        return issues
    }
    
    /**
     * Provides accessibility recommendations
     */
    fun getAccessibilityRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (!isAccessibilityEnabled()) {
            recommendations.add("Enable accessibility services for better tutorial experience")
        }
        
        if (isAccessibilityEnabled() && !isScreenReaderActive()) {
            recommendations.add("Enable screen reader for audio guidance during tutorial")
        }
        
        if (!accessibilityManager.isTouchExplorationEnabled) {
            recommendations.add("Enable touch exploration for better navigation")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !accessibilityManager.isHighTextContrastEnabled) {
            recommendations.add("Consider enabling high contrast text for better visibility")
        }
        
        return recommendations
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "TutorialAccessibilityManager cleaned up")
        }
    }
}