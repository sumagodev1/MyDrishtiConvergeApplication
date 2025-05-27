package com.mydrishti.co.`in`.activities.models

/**
 * Chart types supported by the application
 */
enum class ChartType {
    BAR_DAILY,
    BAR_HOURLY,
    GAUGE,
    METRIC
}

/**
 * Class representing chart data fetched from API
 * This data is not stored in the database and is fetched dynamically
 */
data class ChartData(
    val chartId: String,
    val chartType: ChartType,
    
    /**
     * Map of parameter values and other chart configuration
     * This contains the dynamically fetched data from API
     */
    val parameters: Map<String, String>,
    
    val timestamp: Long = System.currentTimeMillis()
)