package com.mydrishti.co.`in`.activities.models

/**
 * Response model for gauge chart API
 */
data class GaugeChartResponse(
    val lowValue: Double,
    val minValue: Double,
    val highHighValue: Double,
    val highValue: Double,
    val multipliedValue: Double,
    val maxValue: Double,
    val multiplier: Double,
    val statusText: String,
    val lowLowValue: Double,
    val value: Double,
    val timestamp: String
)

data class Threshold(
    val value: Float,
    val color: String,
    val label: String? = null
)