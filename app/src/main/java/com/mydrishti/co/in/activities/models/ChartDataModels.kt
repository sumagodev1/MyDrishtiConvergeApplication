package com.mydrishti.co.`in`.activities.models

/**
 * Data classes for API responses
 */

data class ChartDataPoint(
    val iotDeviceMapId: Long,
    val parameterId: Long,
    val value: Double,
    val timestamp: String
)

data class DeviceParameterBoundary(
    val deviceParameterBoundary: Map<String, Map<String, ParameterBoundary>>,
    val statusText: String,
    val graphData: List<ChartDataPoint>
)

data class ChartDataRequest(
    val dateRange: DateRange,
    val deviceDetails: List<DeviceDetail>
)



