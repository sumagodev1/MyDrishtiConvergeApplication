package com.mydrishti.co.`in`.activities.repositories

import androidx.lifecycle.LiveData
import com.mydrishti.co.`in`.activities.ChartParametersActivity.ParameterInfo
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.dao.ChartDao
import com.mydrishti.co.`in`.activities.dao.ParameterDao
import com.mydrishti.co.`in`.activities.models.*
import com.mydrishti.co.`in`.activities.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.await

/**
 * Repository for chart-related operations
 * Handles data operations between ViewModels and data sources (API and local database)
 */
class ChartRepository(
    private val chartDao: ChartDao,
    private val parameterDao: ParameterDao,
    private val apiService: ApiService,
    private val authManager: SessionManager
) {
    // Get all chart configurations
    fun getAllChartConfigs(): LiveData<List<ChartConfig>> {
        return chartDao.getAllChartConfigs()
    }

    // Get charts for a specific device
    fun getChartConfigsForDevice(deviceId: Int): LiveData<List<ChartConfig>> {
        return chartDao.getChartConfigsForDevice(deviceId.toLong())
    }

    // Get specific chart by ID
    suspend fun getChartById(chartId: String): ChartConfig? {
        return withContext(Dispatchers.IO) {
            chartDao.getChartConfigById(chartId)
        }
    }

    // Insert a chart
    suspend fun insertChart(chartConfig: ChartConfig) {
        withContext(Dispatchers.IO) {
            chartDao.insertChart(chartConfig)
        }
    }

    // Update a chart
    suspend fun updateChart(chartConfig: ChartConfig) {
        withContext(Dispatchers.IO) {
            chartDao.updateChart(chartConfig)
        }
    }

    // Delete a chart
    suspend fun deleteChart(chartConfig: ChartConfig) {
        withContext(Dispatchers.IO) {
            chartDao.deleteChart(chartConfig)
        }
    }

    // Update chart positions
    suspend fun updateChartPositions(charts: List<ChartConfig>) {
        withContext(Dispatchers.IO) {
            charts.forEachIndexed { index, chart ->
                chartDao.updateChartPosition(chart.id, index)
            }
        }
    }

    // Refresh all chart data from API
    suspend fun refreshAllChartData() {
        withContext(Dispatchers.IO) {
            val charts = chartDao.getAllChartConfigs().value ?: return@withContext

            charts.forEach { chart ->
                refreshChartDataInternal(chart)
            }
        }
    }

    // Refresh specific chart data
    suspend fun refreshChartData(chartId: String) {
        withContext(Dispatchers.IO) {
            val chart = chartDao.getChartConfigById(chartId) ?: return@withContext
            refreshChartDataInternal(chart)
        }
    }

    // Get all devices from API
    suspend fun getDevices(): List<Device> {
        return withContext(Dispatchers.IO) {
            try {
                // Log the request for debugging
                println("Fetching devices from API")

                // Get username from session manager or use default
                val username = authManager.getUsername() ?: "lalitvijay@mgumst.org"

                // Call the API endpoint with username parameter
                val response = apiService.getDevices(username)

                if (response.success) {
                    // Log the response for debugging
                    println("Device response received with ${response.deviceList.size} devices")

                    // Return the device list directly from response
                    response.deviceList
                } else {
                    println("Device API request failed: success=false")
                    emptyList()
                }
            } catch (e: Exception) {
                println("Error fetching devices: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }

    /**
     * Get available parameters for a specific device
     */
    suspend fun getParametersForDevice(device: Device, chartType: ChartType? = null): List<ParameterInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // Get the appropriate API type based on chart type or default to "device" type
                val apiType = when (chartType) {
                    ChartType.BAR_DAILY -> "bar-chart"
                    ChartType.BAR_HOURLY -> "hourly-bar-chart"
                    ChartType.GAUGE -> "gauge-chart"
                    ChartType.METRIC -> "metric-chart"
                    else -> "device" // Default to "device" type if no chart type specified
                }

                // Create request for the API with correct parameters
                val request = DeviceParameterRequest(
                    userEmailId = authManager.getUsername() ?: "lalitvijay@mgumst.org",
                    type = apiType
                )

                println("Fetching parameters for device ${device.iotDeviceMapId} with request: $request")

                // Try to fetch from API first
                val response = apiService.getDeviceParameters(request)

                println("Device parameter response received with ${response.deviceParameter.size} device parameters")

                // Process the response to extract parameters
                val parameterList = mutableListOf<ParameterInfo>()

                // Navigate through the DeviceParameter list to get all parameters
                response.deviceParameter.forEach { deviceParam ->
                    val deviceEntity = deviceParam.deviceEntity
                    if (deviceEntity.iotDeviceMapId == device.iotDeviceMapId) {
                        deviceParam.parameterEntityList.forEach { param ->
                            parameterList.add(
                                ParameterInfo(
                                    id = param.parameterId.toLong(),
                                    name = param.parameterName,
                                    displayName = param.parameterDisplayName,
                                    uomDisplayName = param.uomDisplayName
                                )
                            )
                        }
                    }
                }

                // If no parameters found for this device in device-parameter response,
                // try the fallback approach using the general parameter API
                if (parameterList.isEmpty()) {
                    println("No parameters found for device ${device.iotDeviceMapId} in device-parameter response. Trying general parameter API.")
                    try {
                        // Get username from session manager or use default
                        val username = authManager.getUsername() ?: "lalitvijay@mgumst.org"

                        val generalParamResponse = apiService.getParameters(username)
                        if (generalParamResponse.success) {
                            generalParamResponse.userParameterList.forEach { param ->
                                parameterList.add(
                                    ParameterInfo(
                                        id = param.parameterId.toLong(),
                                        name = param.parameterName,
                                        displayName = param.parameterDisplayName,
                                        uomDisplayName = param.uomDisplayName
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        println("Error fetching general parameters: ${e.message}")
                    }
                }

                println("Found ${parameterList.size} parameters for device ${device.iotDeviceMapId}")

                // Save parameters to local DB
                val parameterEntities = parameterList.map { param ->
                    ParameterEntity(
                        parameterId = param.id.toInt(),
                        parameterName = param.name,
                        parameterDisplayName = param.displayName,
                        uomDisplayName = param.uomDisplayName,
                        deviceId = device.iotDeviceMapId
                    )
                }
                parameterDao.insertParameterEntities(parameterEntities)

                parameterList
            } catch (e: Exception) {
                println("Error fetching parameters for device ${device.iotDeviceMapId}: ${e.message}")
                e.printStackTrace()

                // On failure, fetch from local cache
                val cachedParams = parameterDao.getParametersForDevice(device.iotDeviceMapId)
                println("Retrieved ${cachedParams.size} parameters from cache for device ${device.iotDeviceMapId}")

                cachedParams.map { param ->
                    ParameterInfo(
                        id = param.parameterId.toLong(),
                        name = param.parameterName,
                        displayName = param.parameterDisplayName,
                        uomDisplayName = param.uomDisplayName
                    )
                }
            }
        }
    }

    // Internal function to refresh chart data based on type
    private suspend fun refreshChartDataInternal(chart: ChartConfig) {
        try {
            // Convert deviceId from String to Long safely
            val deviceId = chart.deviceId.toIntOrNull() ?: return

            val updatedParams = when (chart.chartType) {
                ChartType.BAR_DAILY -> {
                    // Create request for the bar chart API
                    val request = BarChartRequest(
                        dateRange = DateRange(
                            startDate = "2025-04-30T18:30:00Z",
                            endDate = "2025-05-30T18:29:59Z" +
                                    ""
                        ),
                        deviceDetails = listOf(
                            DeviceDetail(
                                iotDeviceMapId = 52,
                                parameterIdList = listOf<Int>(184)
                            )
                        )
                    )

                    // Call API to get daily bar chart data
                    val response = apiService.getDailyBarChartData(request)
                    mapBarChartResponseToParams(response)
                }
                ChartType.BAR_HOURLY -> {
                    // Create request for the hourly bar chart API
                    val request = BarChartRequest(
                        dateRange = DateRange(
                            startDate = getYesterdayDate(),
                            endDate = getCurrentDate()
                        ),
                        deviceDetails = listOf(
                            DeviceDetail(
                                iotDeviceMapId = deviceId,
                                parameterIdList = getParameterIds(chart)
                            )
                        )
                    )

                    // Call API to get hourly bar chart data
                    val response = apiService.getHourlyBarChartData(request)
                    mapBarChartResponseToParams(response)
                }
                ChartType.GAUGE -> {
                    // Get the first parameter ID from chart config
                    val parameterId = getFirstParameterId(chart)

                    // Create request for the gauge chart API
                    val request = GaugeChartRequest(
                        iotDeviceMapId = deviceId,
                        parameterId = parameterId
                    )

                    // Call API to get gauge chart data
                    val response = apiService.getGaugeChartData(request)
                    mapGaugeChartResponseToParams(response)
                }
                ChartType.METRIC -> {
                    // Create request for the metric chart API
                    val request = MetricChartRequest(
                        iotDeviceMapId = deviceId,
                        parameterIdList = getParameterIds(chart)
                    )

                    // Call API to get metric data
                    val response = apiService.getMetricChartData(request)
                    mapMetricChartResponseToParams(response)
                }
            }

            // Update chart with new data
            val updatedChart = chart.copy(
                parameters = updatedParams,
                lastUpdated = System.currentTimeMillis()
            )
            chartDao.updateChart(updatedChart)
        } catch (e: Exception) {
            // Handle API errors
            println("Error refreshing chart data: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // Helper method to get parameter IDs from chart config
    private fun getParameterIds(chart: ChartConfig): List<Int> {
        // Extract parameter IDs from chart parameters
        // This is a simplified implementation - adapt based on how parameters are stored
        return chart.parameters.values
            .filter { it.toIntOrNull() != null }
            .map { it.toInt() }
            .takeIf { it.isNotEmpty() }
            ?: listOf(184) // Default to Energy parameter ID if none specified
    }

    // Helper method to get the first parameter ID from chart config
    private fun getFirstParameterId(chart: ChartConfig): Int {
        // Get the first parameter ID, or default to 184 (Energy)
        return getParameterIds(chart).firstOrNull() ?: 184
    }

    // Helper method to get current date in ISO format
    private fun getCurrentDate(): String {
        // Return current date in format: 2025-05-13T18:30:00Z
        return java.time.Instant.now().toString()
    }

    // Helper method to get yesterday's date in ISO format
    private fun getYesterdayDate(): String {
        // Return yesterday's date in format: 2025-05-12T18:30:00Z
        return java.time.Instant.now().minus(java.time.Duration.ofDays(1)).toString()
    }

    // Helper method to get last month's date in ISO format
    private fun getLastMonthDate(): String {
        // Return date from 30 days ago in format: 2025-04-13T18:30:00Z
        return java.time.Instant.now().minus(java.time.Duration.ofDays(30)).toString()
    }

    // Helper methods to map API responses to chart parameters
    private fun mapBarChartResponseToParams(response: BarChartResponse): Map<String, String> {
        // Extract data points and map to chart parameter format
        val labels = response.graphData.map { point ->
            // Format timestamp for display (simplified)
            point.timestamp.substringBefore("T")
        }

        val values = response.graphData.map { point ->
            point.value.toString()
        }

        return mapOf(
            "labels" to labels.joinToString(","),
            "values" to values.joinToString(",")
        )
    }

    private fun mapGaugeChartResponseToParams(response: GaugeChartResponse): Map<String, String> {
        return mapOf(
            "min" to response.minValue.toString(),
            "max" to response.maxValue.toString(),
            "value" to response.value.toString(),
            "lowValue" to response.lowValue.toString(),
            "highValue" to response.highValue.toString(),
            "timestamp" to response.timestamp
        )
    }

    private fun mapMetricChartResponseToParams(response: MetricChartResponse): Map<String, String> {
        // Extract the values from each graph data point
        val metrics = response.graphData.associate { point ->
            // Use parameter ID as key and value as value
            point.parameterId.toString() to point.value.toString()
        }

        return metrics.plus("timestamp" to (response.graphData.firstOrNull()?.timestamp ?: ""))
    }
}