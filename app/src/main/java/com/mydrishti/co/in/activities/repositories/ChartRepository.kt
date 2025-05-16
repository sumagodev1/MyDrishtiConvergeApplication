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

    /**
     * Get available parameters for a specific device
     */
    suspend fun getParametersForDevice(device: Device): List<ParameterInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // Create request for the API with correct parameters
                val request = DeviceParameterRequest(
                    userEmailId = authManager.getUsername() ?: "", // Use the email from the app context or pass it as parameter
                    type = "parameter" // Provide an appropriate value for 'type' parameter
                )

                // Try to fetch from API first
                val response = apiService.getDeviceParameters(request)

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
                // On failure, fetch from local cache
                parameterDao.getParametersForDevice(device.iotDeviceMapId).map { param ->
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

    // Get all devices from API
    suspend fun getDevices(): List<Device> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDeviceParameters()
                if (response.success) {
                    response.deviceList
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // Internal function to refresh chart data based on type
    private suspend fun refreshChartDataInternal(chart: ChartConfig) {
        try {
            // Convert deviceId from String to Long safely
            val deviceId = chart.deviceId.toLongOrNull() ?: return

            val updatedParams = when (chart.chartType) {
                ChartType.BAR_DAILY -> {
                    // Call API to get daily bar chart data
                    val response = apiService.getDailyBarChartData(deviceId)
                    mapDailyBarChartResponseToParams(response)
                }
                ChartType.BAR_HOURLY -> {
                    // Call API to get hourly bar chart data
                    val response = apiService.getHourlyBarChartData(deviceId)
                    mapHourlyBarChartResponseToParams(response)
                }
                ChartType.GAUGE -> {
                    // Call API to get gauge chart data
                    val response = apiService.getGaugeChartData(deviceId)
                    mapGaugeChartResponseToParams(response)
                }
                ChartType.METRIC -> {
                    // Call API to get metric data
                    val response = apiService.getMetricData(deviceId)
                    mapMetricResponseToParams(response)
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
            throw e
        }
    }

    // Helper methods to map API responses to chart parameters
    // These would be implemented based on your API response formats
    private fun mapDailyBarChartResponseToParams(response: Any): Map<String, String> {
        // Example implementation - replace with actual mapping logic
        return mapOf(
            "labels" to "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
            "values" to "10,15,7,12,8,15,10"
        )
    }

    private fun mapHourlyBarChartResponseToParams(response: Any): Map<String, String> {
        // Example implementation - replace with actual mapping logic
        return mapOf(
            "labels" to "12AM,3AM,6AM,9AM,12PM,3PM,6PM,9PM",
            "values" to "5,3,7,15,12,10,16,8"
        )
    }

    private fun mapGaugeChartResponseToParams(response: Any): Map<String, String> {
        // Example implementation - replace with actual mapping logic
        return mapOf(
            "min" to "0",
            "max" to "100",
            "value" to "65"
        )
    }

    private fun mapMetricResponseToParams(response: Any): Map<String, String> {
        // Example implementation - replace with actual mapping logic
        return mapOf(
            "label" to "Total",
            "value" to "128",
            "format" to "integer"
        )
    }
}