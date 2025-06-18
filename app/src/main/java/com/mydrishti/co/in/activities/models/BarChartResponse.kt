package com.mydrishti.co.`in`.activities.models

/**
 * Response model for bar chart and hourly bar chart API
 */
data class BarChartResponse(
    val deviceParameterBoundary: Map<String, Map<String, ParameterBoundary>>,
    val statusText: String,
    val graphData: List<GraphDataPoint>
)

data class ParameterBoundary(
    val minValue: Double,
    val lowLowValue: Double,
    val lowValue: Double,
    val highValue: Double,
    val highHighValue: Double,
    val maxValue: Double
)

data class GraphDataPoint(
    val iotDeviceMapId: Int,
    val parameterId: Int,
    val value: Any, // Changed from Double to Any to support both numeric and string values
    val timestamp: String
) 