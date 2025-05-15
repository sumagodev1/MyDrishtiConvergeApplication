package com.mydrishti.co.`in`.activities.repositories

import androidx.lifecycle.LiveData
import com.mydrishti.co.`in`.activities.ChartParametersActivity.ParameterInfo
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.dao.ChartDao
import com.mydrishti.co.`in`.activities.dao.ParameterDao
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for chart-related operations
 * Handles data operations between ViewModels and data sources (API and local database)
 */
class ChartRepository(
    private val chartDao: ChartDao,
    private val parameterDao: ParameterDao,
    private val apiService: ApiService
) {
    // Get all chart configurations
    fun getAllChartConfigs(): LiveData<List<ChartConfig>> {
        return chartDao.getAllChartConfigs()
    }

    // Get charts for a specific site
    fun getChartConfigsForSite(siteId: Long): LiveData<List<ChartConfig>> {
        return chartDao.getChartConfigsForSite(siteId)
    }

    // Get specific chart by ID
    suspend fun getChartById(chartId: Long): ChartConfig? {
        return withContext(Dispatchers.IO) {
            chartDao.getChartConfigById(chartId.toString())
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
     * Get available parameters for a specific site and chart type
     */
    suspend fun getParametersForSite(siteId: Long): List<ParameterInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to fetch from API first
                val response = apiService.getSiteParameters(siteId)

                // If API call is successful, save to local DB and return
                val parameters = response.map { paramDto ->
                    ParameterInfo(
                        id = paramDto.id.toLong(),
                        name = paramDto.name,
                        displayName = paramDto.displayName,
                        uomDisplayName = paramDto.uomDisplayName
                    )
                }

                // Save parameters to local cache
                parameterDao.insertParameters(parameters, siteId)

                parameters
            } catch (e: Exception) {
                // On failure, fetch from local cache
                parameterDao.getParametersForSite(siteId).map { param ->
                    ParameterInfo(
                        id = param.parameterId,
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
            val updatedParams = when (chart.chartType) {
                ChartType.BAR_DAILY -> {
                    // Call API to get daily bar chart data
                    val response = apiService.getDailyBarChartData(chart.siteId.toLong())
                    mapDailyBarChartResponseToParams(response)
                }
                ChartType.BAR_HOURLY -> {
                    // Call API to get hourly bar chart data
                    val response = apiService.getHourlyBarChartData(chart.siteId.toLong())
                    mapHourlyBarChartResponseToParams(response)
                }
                ChartType.GAUGE -> {
                    // Call API to get gauge chart data
                    val response = apiService.getGaugeChartData(chart.siteId.toLong())
                    mapGaugeChartResponseToParams(response)
                }
                ChartType.METRIC -> {
                    // Call API to get metric data
                    val response = apiService.getMetricData(chart.siteId.toLong())
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