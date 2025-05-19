package com.mydrishti.co.`in`.activities.models

/**
 * Request model for bar chart and hourly bar chart API
 */
data class BarChartRequest(
    val dateRange: DateRange,
    val deviceDetails: List<DeviceDetail>
)

data class DateRange(
    val startDate: String,
    val endDate: String
)

data class DeviceDetail(
    val iotDeviceMapId: Int,
    val parameterIdList: List<Int>
) 