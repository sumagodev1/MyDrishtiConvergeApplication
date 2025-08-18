package com.mydrishti.co.`in`.activities.tutorial

/**
 * Data class representing analytics configuration for tutorials
 */
data class AnalyticsConfig(
    val enableAnalytics: Boolean = true,
    val trackStepCompletion: Boolean = true,
    val trackSkipEvents: Boolean = true,
    val trackTimeSpent: Boolean = true,
    val trackUserInteractions: Boolean = true,
    val anonymizeData: Boolean = true,
    val batchEvents: Boolean = true,
    val eventBufferSize: Int = 50
)