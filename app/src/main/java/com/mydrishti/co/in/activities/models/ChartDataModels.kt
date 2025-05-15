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

data class ParameterBoundary(
    val minValue: Double,
    val lowLowValue: Double,
    val lowValue: Double,
    val highValue: Double,
    val highHighValue: Double,
    val maxValue: Double
)

data class DeviceParameterBoundary(
    val deviceParameterBoundary: Map<String, Map<String, ParameterBoundary>>,
    val statusText: String,
    val graphData: List<ChartDataPoint>
)

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

data class DateRange(
    val startDate: String,
    val endDate: String
)

data class DeviceDetail(
    val iotDeviceMapId: Long,
    val parameterIdList: List<Long>
)

data class ChartDataRequest(
    val dateRange: DateRange,
    val deviceDetails: List<DeviceDetail>
)

data class MetricChartRequest(
    val iotDeviceMapId: Long,
    val parameterIdList: List<Long>
)

data class GaugeChartRequest(
    val iotDeviceMapId: Long,
    val parameterId: Long
)

data class DeviceParameterRequest(
    val userEmailId: String,
    val type: String
)