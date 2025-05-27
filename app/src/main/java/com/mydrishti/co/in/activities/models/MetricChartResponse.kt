package com.mydrishti.co.`in`.activities.models

/**
 * Response model for metric chart API
 */
data class MetricChartResponse(
    val deviceParameterBoundary: Map<String, Map<String, ParameterBoundary>>,
    val statusText: String,
    val graphData: List<GraphDataPoint>
) 