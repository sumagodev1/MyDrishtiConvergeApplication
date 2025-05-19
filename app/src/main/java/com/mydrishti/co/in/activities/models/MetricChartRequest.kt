package com.mydrishti.co.`in`.activities.models

/**
 * Request model for metric chart API
 */
data class MetricChartRequest(
    val iotDeviceMapId: Int,
    val parameterIdList: List<Int>
) 