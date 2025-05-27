package com.mydrishti.co.`in`.activities.models

/**
 * Response DTO for hourly bar chart data
 */
data class HourlyBarChartResponse(
    val labels: List<String>,
    val values: List<Float>,
    val title: String,
    val yAxisLabel: String,
    val parameterName: String,
    val parameterUnit: String
)