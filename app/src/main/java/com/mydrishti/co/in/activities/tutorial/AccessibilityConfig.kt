package com.mydrishti.co.`in`.activities.tutorial

/**
 * Data class representing accessibility configuration for tutorials
 */
data class AccessibilityConfig(
    val enableScreenReader: Boolean = true,
    val enableHighContrast: Boolean = false,
    val enableLargeText: Boolean = false,
    val enableReducedMotion: Boolean = false,
    val extendedTimeouts: Boolean = false,
    val alternativeNavigation: Boolean = true,
    val audioFeedback: Boolean = false,
    val hapticFeedback: Boolean = true
)