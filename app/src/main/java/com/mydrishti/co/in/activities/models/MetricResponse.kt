package com.mydrishti.co.`in`.activities.models

/**
 * Response DTO for metric data
 */
data class MetricResponse(
    val title: String,
    val value: String,
    val unit: String? = null,
    val trend: TrendInfo? = null,
    val format: String = "integer" // integer, decimal, percentage
)

/**
 * Trend information for metrics
 */
data class TrendInfo(
    val direction: TrendDirection,
    val value: Float,
    val isPositive: Boolean // Whether the trend is considered positive or negative
)

/**
 * Trend direction enum
 */
enum class TrendDirection {
    UP, DOWN, NEUTRAL
}