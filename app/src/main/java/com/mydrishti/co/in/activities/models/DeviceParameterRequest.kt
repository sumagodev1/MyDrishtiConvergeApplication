package com.mydrishti.co.`in`.activities.models

/**
 * Request model for fetching device parameters
 */
data class DeviceParameterRequest(
    val userEmailId: String,
    val type: String
) {
    companion object {
        // Valid parameter types as per API documentation
        val VALID_TYPES = listOf("bar-chart", "hourly-bar-chart", "gauge-chart", "metric-chart", "device", "parameter")
    }
}