package com.mydrishti.co.`in`.activities.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mydrishti.co.`in`.activities.ChartParametersActivity.ParameterInfo
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.dao.ChartDao
import com.mydrishti.co.`in`.activities.dao.ParameterDao
import com.mydrishti.co.`in`.activities.models.*
import com.mydrishti.co.`in`.activities.models.DateRange
import com.mydrishti.co.`in`.activities.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.await
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.text.SimpleDateFormat

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
    // In-memory cache for chart data
    private val chartDataCache = mutableMapOf<String, ChartData>()
    
    // Track charts currently being refreshed to prevent duplicate refreshes
    private val currentlyRefreshing = mutableSetOf<String>()

    // LiveData for chart data updates
    private val _chartDataUpdates = MutableLiveData<List<ChartData>>()
    val chartDataUpdates: LiveData<List<ChartData>> = _chartDataUpdates

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
        println("ChartRepository: Getting chart by ID: $chartId")
        return withContext(Dispatchers.IO) {
            try {
                val chart = chartDao.getChartConfigById(chartId)
                println("ChartRepository: Retrieved chart: $chart")
                println("ChartRepository: Chart title: ${chart?.title}")
                println("ChartRepository: Chart deviceId: ${chart?.deviceId}")
                println("ChartRepository: Chart deviceName: ${chart?.deviceName}")
                return@withContext chart
            } catch (e: Exception) {
                println("ChartRepository: Error retrieving chart by ID: ${e.message}")
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    // Insert a chart
    suspend fun insertChart(chartConfig: ChartConfig) {
        withContext(Dispatchers.IO) {
            chartDao.insertChart(chartConfig)
            // Fetch initial data for the new chart
            refreshChartData(chartConfig)
        }
    }

    // Update a chart
    suspend fun updateChart(chartConfig: ChartConfig) {
        withContext(Dispatchers.IO) {
            println("ChartRepository: Updating chart with ID: ${chartConfig.id}, Title: ${chartConfig.title}")
            
            try {
                // Get existing chart to verify it exists
                val existingChart = chartDao.getChartConfigById(chartConfig.id)
                if (existingChart == null) {
                    println("ChartRepository: WARNING - Chart with ID ${chartConfig.id} not found when updating")
                    println("ChartRepository: Will attempt fallback with direct SQL update")
                    
                    // Try a direct SQL update as fallback
                    val updatedRows = chartDao.updateChartWithQuery(
                        chartConfig.id,
                        chartConfig.title,
                        chartConfig.deviceId,
                        chartConfig.deviceName
                    )
                    
                    println("ChartRepository: Direct SQL update affected $updatedRows rows")
                    
                    if (updatedRows <= 0) {
                        // If SQL update failed too, resort to insert
                        println("ChartRepository: Direct SQL update failed, falling back to insert")
                        chartDao.insertChart(chartConfig)
                        println("ChartRepository: Inserted chart as fallback")
                    }
                } else {
                    println("ChartRepository: Found existing chart: ${existingChart.title}")
                    println("ChartRepository: Existing chart ID: ${existingChart.id}")
                    
                    // Log chart config equality
                    val sameId = existingChart.id == chartConfig.id
                    val sameTitle = existingChart.title == chartConfig.title
                    val sameDeviceId = existingChart.deviceId == chartConfig.deviceId
                    val sameDeviceName = existingChart.deviceName == chartConfig.deviceName
                    println("ChartRepository: Chart field comparison - " +
                            "Same ID: $sameId, " +
                            "Same Title: $sameTitle, " +
                            "Same DeviceId: $sameDeviceId, " +
                            "Same DeviceName: $sameDeviceName")
                    
                    // Update the chart in database
                    val rowsUpdated = chartDao.updateChart(chartConfig)
                    println("ChartRepository: Updated chart via Room @Update, rows affected: $rowsUpdated")
                    
                    if (rowsUpdated <= 0) {
                        // If Room update failed, try direct SQL
                        println("ChartRepository: Room update returned 0 rows, trying direct SQL")
                        val sqlRows = chartDao.updateChartWithQuery(
                            chartConfig.id,
                            chartConfig.title,
                            chartConfig.deviceId,
                            chartConfig.deviceName
                        )
                        println("ChartRepository: Direct SQL update affected $sqlRows rows")
                    }
                    
                    // Verify the update took effect by fetching again
                    val updatedChart = chartDao.getChartConfigById(chartConfig.id)
                    println("ChartRepository: After update - chart title: ${updatedChart?.title}")
                }
                
                // Clear chart data cache for this chart to force refresh
                chartDataCache.remove(chartConfig.id)
                
            // Refresh data since configuration changed
                refreshChartData(chartConfig)
            } catch (e: Exception) {
                println("ChartRepository: Error updating chart: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    // Delete a chart
    suspend fun deleteChart(chartConfig: ChartConfig) {
        withContext(Dispatchers.IO) {
            chartDao.deleteChart(chartConfig)
            // Remove from cache
            chartDataCache.remove(chartConfig.id)
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

    // Get chart data from cache or fetch it if needed
    fun getChartData(chartId: String): ChartData? {
        return chartDataCache[chartId]
    }

    // Get all chart data from cache
    fun getAllChartData(): List<ChartData> {
        return chartDataCache.values.toList()
    }

    // Refresh all chart data from API
    suspend fun refreshAllChartData() {
        withContext(Dispatchers.IO) {
            try {
            val charts = chartDao.getAllChartConfigsSync()

                // Limit to a reasonable number of concurrent API calls to avoid overwhelming the network
                val batchSize = 3
                val batches = charts.chunked(batchSize)
                
                for (batch in batches) {
                    // Process charts in smaller batches
                    batch.forEach { chart ->
                        try {
                            // Set a timeout for each chart refresh
                            withTimeout(5000) { // 5-second timeout
                refreshChartDataInternal(chart)
                            }
                        } catch (e: Exception) {
                            // Log error but continue with other charts
                            println("Error refreshing chart ${chart.id}: ${e.message}")
                        }
                    }
            }

            // Notify observers of updated data
            _chartDataUpdates.postValue(chartDataCache.values.toList())
            } catch (e: Exception) {
                println("Error in refreshAllChartData: ${e.message}")
            }
        }
    }

    // Refresh specific chart data with timeout
    suspend fun refreshChartData(chartId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Check if this is a refresh in progress
                if (chartId in currentlyRefreshing) {
                    println("Already refreshing chart $chartId - skipping duplicate refresh")
                    return@withContext
                }
                
                // Mark this chart as being refreshed
                currentlyRefreshing.add(chartId)
                
                // Get the chart config
                // For month-specific chart IDs (format: originalId_YYYY_MM), get the base chart ID
                val baseChartId = chartId.split("_")[0]
                val chart = chartDao.getChartConfigById(baseChartId) ?: return@withContext
                
                try {
                    // Apply a timeout to the operation
                    withTimeout(5000) { // 5-second timeout
                        // Check if this is a month-specific chart ID
                        if (chart.chartType == ChartType.BAR_DAILY && "_" in chartId) {
                            // Extract year and month from the ID (format: originalId_YYYY_MM)
                            val parts = chartId.split("_")
                            if (parts.size >= 3) {
                                try {
                                    val year = parts[parts.size - 2].toInt()
                                    val month = parts[parts.size - 1].toInt()
                                    println("Processing month-specific chart: Year=$year, Month=$month")

                                    // Create a modified chart with the specific month's date range
                                    val modifiedChart = chart.copy(
                                        id = chartId,
                                        customDateRange = getMonthDateRangeForAPI(year, month)
                                    )
                                    
                                    // Clear cache for this chart ID to force API call
                                    chartDataCache.remove(chartId)
                                    
                                    try {
                                        // Call API with the specific month data
                                        println("Calling API for historical month data: $year-$month")
                                        val response = refreshChartDataInternal(modifiedChart)
                                        
                                        // Check if there's actual data in the response
                                        val hasRealData = checkIfHasRealData(response)
                                        
                                        // Also verify the data is for the correct month
                                        val isCorrectMonth = verifyDataTimeframe(response, expectedMonth = month)
                                        
                                        if (!hasRealData || !isCorrectMonth || response.isEmpty()) {
                                            println("No data available for month $year-$month, creating empty result")
                                            println("Has real data: $hasRealData, Is correct month: $isCorrectMonth")
                                            val emptyData = ChartData(
                                                chartId = chartId,
                                                chartType = chart.chartType,
                                                parameters = mapOf("no_data" to "true"),
                                                timestamp = System.currentTimeMillis()
                                            )
                                            chartDataCache[chartId] = emptyData
            _chartDataUpdates.postValue(chartDataCache.values.toList())
                                        } else {
                                            // Create a chart data object with the response
                                            val chartData = ChartData(
                                                chartId = chartId,
                                                chartType = chart.chartType,
                                                parameters = response,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            
                                            // Store in cache and notify observers
                                            chartDataCache[chartId] = chartData
                                            _chartDataUpdates.postValue(chartDataCache.values.toList())
                                            
                                            println("Successfully stored data for month $year-$month with ${response["values"]?.split(",")?.size ?: 0} values")
                                        }
                                    } catch (e: Exception) {
                                        println("Error fetching data for month $year-$month: ${e.message}")
                                        // Create empty data on error
                                        val emptyData = ChartData(
                                            chartId = chartId,
                                            chartType = chart.chartType,
                                            parameters = mapOf("no_data" to "true"),
                                            timestamp = System.currentTimeMillis()
                                        )
                                        chartDataCache[chartId] = emptyData
                                        _chartDataUpdates.postValue(chartDataCache.values.toList())
                                    }
                                    
                                    return@withTimeout
                                } catch (e: Exception) {
                                    println("Error parsing month-specific chart ID: ${e.message}")
                                }
                            }
                        }
                        
                        // Check if this is a day-specific chart ID for hourly charts (format: originalId_YYYY_MM_DD)
                        if (chart.chartType == ChartType.BAR_HOURLY && "_" in chartId) {
                            // Extract year, month, day from the ID (format: originalId_YYYY_MM_DD)
                            val parts = chartId.split("_")
                            if (parts.size >= 4) {
                                try {
                                    val year = parts[parts.size - 3].toInt()
                                    val month = parts[parts.size - 2].toInt() 
                                    val day = parts[parts.size - 1].toInt()
                                    println("CRITICAL DEBUG: Processing day-specific hourly chart: Year=$year, Month=${month+1}, Day=$day")
                                    
                                    // Create a modified chart with the specific day's date range
                                    val dateRange = getDayDateRangeForAPI(year, month, day)
                                    println("CRITICAL DEBUG: Date range for API request: ${dateRange.startDate} to ${dateRange.endDate}")
                                    
                                    val modifiedChart = chart.copy(
                                        id = chartId,
                                        customDateRange = dateRange
                                    )
                                    
                                    // Always clear cache for this chart ID to force API call
                                    chartDataCache.remove(chartId)
                                    println("CRITICAL DEBUG: Cleared cache for day-specific chart ID: $chartId")
                                    
                                    try {
                                        // Call API with the specific day data
                                        println("CRITICAL DEBUG: Calling API for hourly data: $year-${month+1}-$day")
                                        val response = refreshChartDataInternal(modifiedChart)
                                        println("CRITICAL DEBUG: API response received with ${response.size} parameters")
                                        
                                        // Log timestamps in response
                                        val timestamps = response["timestamps"]?.split(",") ?: emptyList()
                                        if (timestamps.isNotEmpty()) {
                                            println("CRITICAL DEBUG: First timestamp: ${timestamps.first()}")
                                            println("CRITICAL DEBUG: Last timestamp: ${timestamps.last()}")
                                        }
                                        
                                        // Check if there's actual data in the response
                                        val hasRealData = checkIfHasRealData(response)
                                        
                                        // Also verify the data is for the correct day
                                        val isCorrectDay = verifyDataTimeframe(response, expectedDay = day)
                                        println("CRITICAL DEBUG: Data validation: hasRealData=$hasRealData, isCorrectDay=$isCorrectDay")
                                        
                                        if (!hasRealData || !isCorrectDay || response.isEmpty()) {
                                            println("No hourly data available for day $year-${month+1}-$day, creating empty result")
                                            println("Has real data: $hasRealData, Is correct day: $isCorrectDay")
                                            val emptyData = ChartData(
                                                chartId = chartId,
                                                chartType = chart.chartType,
                                                parameters = mapOf("no_data" to "true"),
                                                timestamp = System.currentTimeMillis()
                                            )
                                            chartDataCache[chartId] = emptyData
                                            _chartDataUpdates.postValue(chartDataCache.values.toList())
                                        } else {
                                            // Force add the day as parameter to ensure filtering in UI
                                            val enhancedParams = response.toMutableMap()
                                            enhancedParams["selectedDay"] = day.toString()
                                            enhancedParams["selectedMonth"] = month.toString()
                                            enhancedParams["selectedYear"] = year.toString()
                                            
                                            // Create a chart data object with the response
                                            val chartData = ChartData(
                                                chartId = chartId,
                                                chartType = chart.chartType,
                                                parameters = enhancedParams,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            
                                            // Store in cache and notify observers
                                            chartDataCache[chartId] = chartData
                                            _chartDataUpdates.postValue(chartDataCache.values.toList())
                                            
                                            println("CRITICAL DEBUG: Successfully stored hourly data for day $year-${month+1}-$day with ${response["values"]?.split(",")?.size ?: 0} values")
                                        }
                                    } catch (e: Exception) {
                                        println("Error fetching hourly data for day $year-${month+1}-$day: ${e.message}")
                                        // Create empty data on error
                                        val emptyData = ChartData(
                                            chartId = chartId,
                                            chartType = chart.chartType,
                                            parameters = mapOf("no_data" to "true"),
                                            timestamp = System.currentTimeMillis()
                                        )
                                        chartDataCache[chartId] = emptyData
                                        _chartDataUpdates.postValue(chartDataCache.values.toList())
                                    }
                                    
                                    return@withTimeout
                                } catch (e: Exception) {
                                    println("Error parsing day-specific chart ID: ${e.message}")
                                }
                            }
                        }
                        
                        // For standard charts, refresh normally with the chart config
                        refreshChartData(chart)
                    }
                } finally {
                    // Always remove from currently refreshing list
                    currentlyRefreshing.remove(chartId)
                }
            } catch (e: Exception) {
                println("Error in refreshChartData with timeout: ${e.message}")
                // Make sure to clear refreshing status even on error
                currentlyRefreshing.remove(chartId)
            }
        }
    }

    // Add helper method to get date range for a specific month in API format
    private fun getMonthDateRangeForAPI(year: Int, month: Int): DateRange {
        // Get system default timezone
        val zoneId = java.time.ZoneId.systemDefault()
        
        // First day of the specified month at 00:00:00 in local timezone
        val localStart = java.time.LocalDate.of(year, month + 1, 1) // Month is 0-based in Calendar, but 1-based in LocalDate
            .atStartOfDay(zoneId)
            
        // Convert to UTC
        val utcStart = localStart.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()
        
        // Format as ISO 8601 UTC
        val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT
        
        val startIso = formatter.format(utcStart)
        
        println("Month date for API request: $startIso")
        
        // Match website format - for monthly data it also uses the same start/end date
        return DateRange(
            startDate = startIso,
            endDate = startIso
        )
    }
    
    // Add helper method to get date range for a specific day in API format
    private fun getDayDateRangeForAPI(year: Int, month: Int, day: Int): DateRange {
        // Get system default timezone
        val zoneId = java.time.ZoneId.systemDefault()
        // The selected day at 00:00:00 in local timezone
        val localDay = java.time.LocalDate.of(year, month + 1, day) // Month is 0-based in Calendar, but 1-based in LocalDate
            .atStartOfDay(zoneId)
        // Convert to UTC - use same time for both start and end as the web app does
        val utcTime = localDay.withZoneSameInstant(java.time.ZoneOffset.UTC)
        // Format as ISO 8601 UTC (with 'Z' at the end)
        val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT
        val timeIso = formatter.format(utcTime.toInstant())
        // Log for debugging
        println("CRITICAL FIX: getDayDateRangeForAPI: Local $year-${month+1}-$day 00:00:00 $zoneId -> UTC $timeIso")
        // Return the same UTC time for both start and end
        return DateRange(
            startDate = timeIso,
            endDate = timeIso
        )
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

                println("Fetching parameters for device ${device.iotDeviceMapId} with request type: $apiType")

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
                            println("General parameter API returned ${generalParamResponse.userParameterList.size} parameters")

                            // Add all parameters from the general list - these should match across all devices
                            generalParamResponse.userParameterList.forEach { param ->
                                println("Adding parameter: ${param.parameterDisplayName} (${param.uomDisplayName}) with ID ${param.parameterId}")
                                parameterList.add(
                                    ParameterInfo(
                                        id = param.parameterId.toLong(),
                                        name = param.parameterName,
                                        displayName = param.parameterDisplayName,
                                        uomDisplayName = param.uomDisplayName
                                    )
                                )
                            }
                        } else {
                            println("General parameter API request failed: success=false")
                        }
                    } catch (e: Exception) {
                        println("Error fetching general parameters: ${e.message}")
                        e.printStackTrace()
                    }
                }

                // If we STILL don't have parameters, add the default ones from the API docs
                if (parameterList.isEmpty()) {
                    println("No parameters found in any API response. Adding default parameters from API docs.")

                    // Add the Energy parameter (184)
                    parameterList.add(
                        ParameterInfo(
                            id = 184L,
                            name = "Energy",
                            displayName = "Energy",
                            uomDisplayName = "KWh"
                        )
                    )

                    // Add the Power parameter (182)
                    parameterList.add(
                        ParameterInfo(
                            id = 182L,
                            name = "Power",
                            displayName = "Power",
                            uomDisplayName = "KW"
                        )
                    )
                }

                println("Found ${parameterList.size} parameters for device ${device.iotDeviceMapId}")

                // Save parameters to local DB for configuration purposes
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
    private suspend fun refreshChartDataInternal(chart: ChartConfig): Map<String, String> {
        try {
            // Convert deviceId from String to Long safely
            val deviceId = chart.deviceId.toIntOrNull() ?: return mapOf()
            
            // Log that an API call is about to be made
            println("API CALL: Refreshing chart data for chart ID: ${chart.id}, Type: ${chart.chartType}")
            
            // Clear the cache for this chart to ensure fresh data
            chartDataCache.remove(chart.id)
            
            // Fetch parameter information from API first - this will help with display names
            val username = authManager.getUsername() ?: "lalitvijay@mgumst.org"
            val paramResponse = try {
                // Try to get parameter info from API
                apiService.getParameters(username)
            } catch (e: Exception) {
                println("Failed to fetch parameter info from API: ${e.message}")
                null
            }
            
            // Create parameter map for easy lookup
            val parameterInfoMap = mutableMapOf<Int, ParameterItem>()
            if (paramResponse?.success == true) {
                paramResponse.userParameterList.forEach { param ->
                    parameterInfoMap[param.parameterId] = param
                    println("Cached parameter info: ${param.parameterId} = ${param.parameterDisplayName}")
                }
            }

            val updatedParams = when (chart.chartType) {
                ChartType.BAR_DAILY -> {
                    // Create request for the bar chart API with timezone-aware date range
                    val dateRange = if (chart.customDateRange != null) {
                        // Use custom date range if specified
                        println("API CALL: Using custom date range for chart ${chart.id}: ${chart.customDateRange.startDate} to ${chart.customDateRange.endDate}")
                        
                        // Check if this is a month-specific chart ID (format: originalId_YYYY_MM)
                        if ("_" in chart.id) {
                            val parts = chart.id.split("_")
                            if (parts.size >= 3) {
                                try {
                                    val year = parts[parts.size - 2].toInt()
                                    val month = parts[parts.size - 1].toInt()
                                    println("CRITICAL FIX: Detected month-specific chart ID: ${chart.id}")
                                    println("CRITICAL FIX: Using year=$year, month=$month for date range")
                                    
                                    // Website format expects specific date format
                                    // For example: {"dateRange":{"startDate":"2023-05-01T00:00:00Z","endDate":"2023-05-31T23:59:59Z"}}
                                    val fixedDateRange = getMonthDateRangeForAPI(year, month)
                                    println("CRITICAL FIX: Fixed date range: ${fixedDateRange.startDate} to ${fixedDateRange.endDate}")
                                    fixedDateRange
                                } catch (e: Exception) {
                                    println("CRITICAL FIX: Error parsing month-specific ID, using original date range")
                                    chart.customDateRange
                                }
                            } else {
                                chart.customDateRange
                            }
                        } else {
                            chart.customDateRange
                        }
                    } else {
                        // Use last 30 days by default
                        val defaultRange = DateRange(
                            startDate = getLastMonthDate(),
                            endDate = getCurrentDate()
                        )
                        println("API CALL: Using default date range for chart ${chart.id}: ${defaultRange.startDate} to ${defaultRange.endDate}")
                        defaultRange
                    }

                    val request = BarChartRequest(
                        dateRange = dateRange,
                        deviceDetails = listOf(
                            DeviceDetail(
                                iotDeviceMapId = deviceId,
                                parameterIdList = getParameterIds(chart)
                            )
                        )
                    )

                    // Log API request
                    println("API CALL: Daily bar chart request - deviceId: $deviceId, parameters: ${getParameterIds(chart)}")
                    println("API CALL: Date range: ${dateRange.startDate} to ${dateRange.endDate}")
                    println("CRITICAL DEBUG: EXACT REQUEST FORMAT: {\"dateRange\":{\"startDate\":\"${dateRange.startDate}\",\"endDate\":\"${dateRange.endDate}\"},\"deviceDetails\":[{\"iotDeviceMapId\":$deviceId,\"parameterIdList\":${getParameterIds(chart)}}]}")

                    // Call API to get daily bar chart data
                    println("API CALL: Executing network request for daily bar chart data...")
                    val response = apiService.getDailyBarChartData(request)
                    println("API CALL: Received response with ${response.graphData.size} data points")
                    
                    // Check if we have any data
                    if (response.graphData.isEmpty()) {
                        println("API CALL: No data returned for the selected month")
                        // Return empty map to indicate no data
                        return mapOf("no_data" to "true")
                    }
                    
                    // Add parameter display names to the response from our parameter info map
                    if (response.graphData.isNotEmpty()) {
                        val firstPoint = response.graphData.first()
                        val paramInfo = parameterInfoMap[firstPoint.parameterId]
                        if (paramInfo != null) {
                            println("Adding parameter display name from API: ${paramInfo.parameterDisplayName}")
                        }
                    }
                    
                    // Process the data to handle duplicates caused by timezone conversions
                    val processedParams = processDailyBarChartResponse(response, parameterInfoMap)
                    
                    // Return the processed parameters
                    processedParams
                }
                ChartType.BAR_HOURLY -> {
                    // Use custom date range if specified, otherwise get today's range
                    val dateRange = if (chart.customDateRange != null) {
                        // Use custom date range if specified
                        println("API CALL: Using custom date range for hourly chart ${chart.id}: ${chart.customDateRange.startDate} to ${chart.customDateRange.endDate}")
                        
                        // Check if this is a day-specific chart ID (format: originalId_YYYY_MM_DD)
                        if ("_" in chart.id) {
                            val parts = chart.id.split("_")
                            if (parts.size >= 4) {
                                try {
                                    val year = parts[parts.size - 3].toInt()
                                    val month = parts[parts.size - 2].toInt()
                                    val day = parts[parts.size - 1].toInt()
                                    println("CRITICAL FIX: Detected day-specific hourly chart ID: ${chart.id}")
                                    println("CRITICAL FIX: Using year=$year, month=${month+1}, day=$day for date range")
                                    
                                    // Website format expects specific date format
                                    // For example: {"dateRange":{"startDate":"2025-05-22T18:30:00Z","endDate":"2025-05-22T18:30:00Z"}}
                                    val fixedDateRange = getDayDateRangeForAPI(year, month, day)
                                    println("CRITICAL FIX: Fixed date range: ${fixedDateRange.startDate} to ${fixedDateRange.endDate}")
                                    fixedDateRange
                                } catch (e: Exception) {
                                    println("CRITICAL FIX: Error parsing day-specific ID, using original date range")
                                    chart.customDateRange
                                }
                            } else {
                                chart.customDateRange
                            }
                        } else {
                            chart.customDateRange
                        }
                    } else {
                        // Get timezone-aware date range for today
                        val dayRange = getDayStartAndEndInUTC()
                        DateRange(
                            startDate = dayRange["startDate"] ?: (getCurrentDateISOOnly() + "T00:00:00Z"),
                            endDate = dayRange["endDate"] ?: (getCurrentDateISOOnly() + "T23:59:59Z")
                        )
                    }

                    // Create request for the hourly bar chart API
                    val request = BarChartRequest(
                        dateRange = dateRange,
                        deviceDetails = listOf(
                            DeviceDetail(
                                iotDeviceMapId = deviceId,
                                parameterIdList = getParameterIds(chart)
                            )
                        )
                    )

                    // Log API request
                    println("CRITICAL DEBUG: Hourly bar chart request - deviceId: $deviceId, parameters: ${getParameterIds(chart)}")
                    println("CRITICAL DEBUG: Date range for API call: ${request.dateRange.startDate} to ${request.dateRange.endDate}")
                    println("CRITICAL DEBUG: EXACT REQUEST FORMAT: {\"dateRange\":{\"startDate\":\"${request.dateRange.startDate}\",\"endDate\":\"${request.dateRange.endDate}\"},\"deviceDetails\":[{\"iotDeviceMapId\":$deviceId,\"parameterIdList\":${getParameterIds(chart)}}]}")

                    // Call API to get hourly bar chart data
                    val response = apiService.getHourlyBarChartData(request)
                    mapBarChartResponseToParams(response, chart.chartType, parameterInfoMap)
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
                    
                    // Get parameter info from our map
                    val paramInfo = parameterInfoMap[parameterId]
                    val paramDisplayName = paramInfo?.parameterDisplayName ?: ""
                    println("Gauge chart parameter $parameterId display name from API: $paramDisplayName")
                    
                    mapGaugeChartResponseToParams(response, parameterId, paramDisplayName)
                }
                ChartType.METRIC -> {
                    // Create request for the metric chart API
                    val request = MetricChartRequest(
                        iotDeviceMapId = deviceId,
                        parameterIdList = getParameterIds(chart)
                    )

                    // Call API to get metric data
                    val response = apiService.getMetricChartData(request)
                    mapMetricChartResponseToParams(response, parameterInfoMap)
                }
            }

            // Update chart data in cache
            val chartData = ChartData(
                chartId = chart.id,
                chartType = chart.chartType,
                parameters = updatedParams
            )

            // Store in cache
            chartDataCache[chart.id] = chartData

            // Update last updated timestamp in database
            chartDao.updateChartLastUpdated(chart.id, System.currentTimeMillis())

            return updatedParams
        } catch (e: Exception) {
            // Handle API errors
            println("Error refreshing chart data: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // New function to process daily bar chart response with better handling of duplicates
    private fun processDailyBarChartResponse(response: BarChartResponse, parameterInfoMap: Map<Int, ParameterItem>): Map<String, String> {
        // Start with the standard processing
        val standardParams = mapBarChartResponseToParams(response, ChartType.BAR_DAILY, parameterInfoMap)
        
        // Since we've removed synthetic data generation, the data should be more accurate
        // Let's get the original timestamps, values, and labels
        val timestamps = standardParams["timestamps"]?.split(",") ?: listOf()
        val values = standardParams["values"]?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: listOf()
        val labels = standardParams["labels"]?.split(",") ?: listOf()
        
        // Only proceed if we have matching data
        if (timestamps.isEmpty() || values.isEmpty() || labels.isEmpty() || 
            timestamps.size != values.size || values.size != labels.size) {
            return standardParams
        }
        
        // Create a map to track data by date in local time zone
        val dateToValueMap = mutableMapOf<String, Double>()
        val dateToTimestampMap = mutableMapOf<String, String>()
        
        // Process each timestamp and convert to local date
        timestamps.forEachIndexed { index, timestamp ->
            if (index >= values.size) return@forEachIndexed
            
            try {
                // Parse the timestamp using multiple formats
                val date = parseTimestamp(timestamp)
                if (date != null) {
                    // Convert to local date in YYYY-MM-DD format
                    val localDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    localDateFormat.timeZone = TimeZone.getDefault()
                    val localDateStr = localDateFormat.format(date)
                    
                    // Add or update the value for this date
                    val currentValue = dateToValueMap.getOrDefault(localDateStr, 0.0)
                    dateToValueMap[localDateStr] = currentValue + values[index]
                    
                    // Keep the latest timestamp for each date
                    if (!dateToTimestampMap.containsKey(localDateStr) || 
                        timestamp > dateToTimestampMap[localDateStr]!!) {
                        dateToTimestampMap[localDateStr] = timestamp
                    }
                    
                    println("Mapped API data: $timestamp -> $localDateStr = ${values[index]}")
                }
            } catch (e: Exception) {
                println("Error processing timestamp $timestamp: ${e.message}")
            }
        }
        
        // Create new consolidated lists
        val consolidatedDates = dateToValueMap.keys.sorted()
        val consolidatedValues = consolidatedDates.map { dateToValueMap[it]!!.toFloat() }
        val consolidatedTimestamps = consolidatedDates.map { dateToTimestampMap[it]!! }
        
        // Format dates for display (DD MMM)
        val consolidatedLabels = consolidatedDates.map { dateStr ->
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                formatter.format(date).let {
                    // Ensure month part is capitalized
                    it.split(" ").let { parts ->
                        if (parts.size >= 2) "${parts[0]} ${parts[1].capitalize()}" else it
                    }
                }
            } catch (e: Exception) {
                dateStr
            }
        }
        
        println("Consolidated ${timestamps.size} timestamps into ${consolidatedDates.size} unique dates")
        println("Final dates: ${consolidatedDates.joinToString(", ")}")
        println("Final values: ${consolidatedValues.joinToString(", ")}")
        
        // Create updated parameters
        val updatedParams = standardParams.toMutableMap()
        updatedParams["timestamps"] = consolidatedTimestamps.joinToString(",")
        updatedParams["values"] = consolidatedValues.map { it.toString() }.joinToString(",")
        updatedParams["labels"] = consolidatedLabels.joinToString(",")
        updatedParams["consolidatedData"] = "true"
        
        return updatedParams
    }
    
    // Helper function to parse timestamps with multiple formats
    private fun parseTimestamp(timestamp: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )
        
        for (formatStr in formats) {
            try {
                val format = SimpleDateFormat(formatStr, Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                
                // Special handling for +00:00 format
                val parsableTimestamp = if (timestamp.contains("+00:00")) {
                    timestamp.replace("+00:00", "Z")
                } else {
                    timestamp
                }
                
                return format.parse(parsableTimestamp)
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        return null
    }

    // Helper method to get parameter IDs from chart config
    private fun getParameterIds(chart: ChartConfig): List<Int> {
        println("Getting parameter IDs from chart: ${chart.title}")

        // Return the parameter IDs directly from the chart config
        if (chart.parameterIds.isNotEmpty()) {
            println("Found parameter IDs from chart config: ${chart.parameterIds}")
            return chart.parameterIds
        }

        // Default to Energy parameter ID if none specified
        println("No parameter IDs found, defaulting to Energy parameter (184)")
        return listOf(184)
    }

    // Helper method to get the first parameter ID from chart config
    private fun getFirstParameterId(chart: ChartConfig): Int {
        // Get the first parameter ID from the chart config
        val firstParam = chart.parameterIds.firstOrNull() ?: 184
        println("Using first parameter ID: $firstParam")
        return firstParam
    }

    // Helper method to get current date in ISO format
    private fun getCurrentDate(): String {
        // Use the timezone-aware date calculation
        return getCurrentMonthStartAndEndInUTC()["endDate"] ?: run {
            // Fallback to old method if the new one fails
            val now = java.time.LocalDateTime.now()
            val endOfDay = now.withHour(23).withMinute(59).withSecond(59)

            endOfDay.atZone(java.time.ZoneId.systemDefault())
                .withZoneSameInstant(java.time.ZoneOffset.UTC)
                .format(java.time.format.DateTimeFormatter.ISO_INSTANT)
        }
    }

    // Helper method to get current date in YYYY-MM-DD format
    private fun getCurrentDateISOOnly(): String {
        return java.time.LocalDate.now().toString() // Format: YYYY-MM-DD
    }

    // Helper method to get yesterday's date in ISO format
    private fun getYesterdayDate(): String {
        // Return yesterday's date in format: 2023-05-24T00:00:00Z
        val zoneId = java.time.ZoneId.systemDefault()

        // Get yesterday at start of day in local timezone
        val yesterday = java.time.ZonedDateTime.now(zoneId)
            .minusDays(1)
            .toLocalDate()
            .atStartOfDay(zoneId)

        // Convert to UTC
        val utcYesterday = yesterday.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()

        // Format output as ISO 8601 UTC
        val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT
        val formattedDate = formatter.format(utcYesterday)

        println("API Request using yesterday's date: $formattedDate")
        return formattedDate
    }

    // Helper method to get last month's date in ISO format
    private fun getLastMonthDate(): String {
        // Return date from 30 days ago in format: 2023-04-25T00:00:00Z
        val zoneId = java.time.ZoneId.systemDefault()

        // Get date 30 days ago at start of day in local timezone
        val oneMonthAgo = java.time.ZonedDateTime.now(zoneId)
            .minusDays(30)
            .toLocalDate()
            .atStartOfDay(zoneId)

        // Convert to UTC
        val utcOneMonthAgo = oneMonthAgo.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()

        // Format output as ISO 8601 UTC
        val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT
        val formattedDate = formatter.format(utcOneMonthAgo)

        println("API Request using one month ago date: $formattedDate")
        return formattedDate
    }

    // New helper method to get current month's start and end dates in UTC format
    private fun getCurrentMonthStartAndEndInUTC(): Map<String, String> {
        // Get system default timezone (dynamic: IST, Dubai, etc.)
        val zoneId = java.time.ZoneId.systemDefault()

        // First day of the current month at 00:00:00 in local timezone
        val localStart = java.time.ZonedDateTime.now(zoneId)
            .withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(zoneId)

        // Last day of the current month at 23:59:59 in local timezone
        val localEnd = java.time.ZonedDateTime.now(zoneId)
            .with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
            .toLocalDate()
            .atTime(23, 59, 59)
            .atZone(zoneId)

        // Convert to UTC
        val utcStart = localStart.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()
        val utcEnd = localEnd.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()

        // Format output as ISO 8601 UTC (Z at end)
        val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT

        return mapOf(
            "startDate" to formatter.format(utcStart),
            "endDate" to formatter.format(utcEnd)
        )
    }

    // Helper method to get specific month's start and end dates in UTC format
    private fun getMonthStartAndEndInUTC(year: Int, month: Int): Map<String, String> {
        // Get system default timezone
        val zoneId = java.time.ZoneId.systemDefault()

        // First day of the specified month at 00:00:00 in local timezone
        val localStart = java.time.LocalDate.of(year, month, 1)
            .atStartOfDay(zoneId)

        // Last day of the specified month at 23:59:59 in local timezone
        val localEnd = java.time.LocalDate.of(year, month, 1)
            .with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
            .atTime(23, 59, 59)
            .atZone(zoneId)

        // Convert to UTC
        val utcStart = localStart.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()
        val utcEnd = localEnd.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()

        // Format output as ISO 8601 UTC
        val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT

        return mapOf(
            "startDate" to formatter.format(utcStart),
            "endDate" to formatter.format(utcEnd)
        )
    }

    // Helper method to get date range for a specific day in UTC format
    private fun getDayStartAndEndInUTC(date: java.time.LocalDate = java.time.LocalDate.now()): Map<String, String> {
        // Get system default timezone
        val zoneId = java.time.ZoneId.systemDefault()

        // Start of the specified day at 00:00:00 in local timezone
        val localStart = date.atStartOfDay(zoneId)

        // End of the specified day at 23:59:59 in local timezone
        val localEnd = date.atTime(23, 59, 59).atZone(zoneId)

        // Convert to UTC
        val utcStart = localStart.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()
        val utcEnd = localEnd.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant()

        // Format output as ISO 8601 UTC
        val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT

        return mapOf(
            "startDate" to formatter.format(utcStart),
            "endDate" to formatter.format(utcEnd)
        )
    }

    // Helper methods to map API responses to chart parameters
    private fun mapBarChartResponseToParams(response: BarChartResponse, chartType: ChartType, parameterInfoMap: Map<Int, ParameterItem>): Map<String, String> {
        val graphData = response.graphData.toMutableList()

        println("CRITICAL DEBUG: CHART DATA (${chartType}): Received ${graphData.size} data points from API.")
        
        // If graphData is empty, immediately return no_data true
        if (graphData.isEmpty()) {
            println("CRITICAL DEBUG: API returned empty graphData array - no data available")
            return mapOf("no_data" to "true")
        }
        
        if (graphData.isNotEmpty()) {
            println("CRITICAL DEBUG: First point: ${graphData.first().timestamp}, Last point: ${graphData.last().timestamp}")
            
            // Log all data points for debugging purposes
            graphData.forEachIndexed { index, point ->
                println("API data point $index: ${point.timestamp} = ${point.value} ${point.iotDeviceMapId}/${point.parameterId}")
            }
        }

        // Sort data by timestamp to ensure chronological order - use original UTC timestamps
        graphData.sortBy { it.timestamp }

        // Store original timestamps without adjusting them - handle timezone conversion in UI
        val originalTimestamps = graphData.map { it.timestamp }
        
        // Log timestamp information for debugging
        originalTimestamps.forEach { timestamp ->
            try {
                val dateTime = java.time.ZonedDateTime.parse(timestamp)
                println("API timestamp: $timestamp (${dateTime.toLocalDate()} at ${dateTime.toLocalTime()})")
            } catch (e: Exception) {
                println("Could not parse timestamp: $timestamp")
            }
        }
        
        // Create adjusted data points for sorting and date calculation
        val adjustedGraphData = graphData.map { dataPoint ->
            try {
                // Just parse to validate, but keep original timestamp for UI conversion
                val originalTimestamp = dataPoint.timestamp
                val zonedDateTime = java.time.ZonedDateTime.parse(originalTimestamp)
                dataPoint
            } catch (e: Exception) {
                println("Error parsing timestamp ${dataPoint.timestamp}: ${e.message}")
                dataPoint // Return original if parsing fails
            }
        }.toMutableList()

        val params = mutableMapOf<String, String>()

        // Store original timestamps for proper timezone conversion in UI
        params["timestamps"] = originalTimestamps.joinToString(",")
        
        // Get the parameter IDs to fetch units
        val parameterId = if (graphData.isNotEmpty()) graphData.first().parameterId else 184 // Default to Energy
        
        // Get unit information from the parameter cache
        val unit = getUnitForParameter(parameterId)
        println("Using unit for parameter $parameterId: $unit")
        params["unit"] = unit
        
        // Store parameter ID for reference
        params["parameterId"] = parameterId.toString()
        
        // Get parameter display name from the parameter info map
        val paramInfo = parameterInfoMap[parameterId]
        if (paramInfo != null) {
            val displayName = paramInfo.parameterDisplayName
            params["parameterDisplayName"] = displayName
            println("Using display name from API for bar chart parameter $parameterId: $displayName")
        }

        if (chartType == ChartType.BAR_DAILY) {
            // Get today's date in the LOCAL timezone (not UTC)
            val today = java.time.LocalDate.now()
            val todayStr = today.toString() // Format: YYYY-MM-DD
            println("Today in local timezone: $todayStr")
            
            // Create a set of unique dates in the data to check for duplicates
            val datesInData = mutableSetOf<String>()
            
            // Process all timestamps and extract their dates in local time
            val localDates = adjustedGraphData.mapNotNull { dataPoint ->
                try {
                    val utcDateTime = java.time.ZonedDateTime.parse(dataPoint.timestamp)
                    // Convert to local date
                    val localDate = utcDateTime.withZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDate()
                    val dateStr = localDate.toString()
                    // Add to set of unique dates
                    datesInData.add(dateStr)
                    Pair(dateStr, dataPoint)
                } catch (e: Exception) {
                    null
                }
            }
            
            // Log all the unique dates found in the data
            println("Unique dates in data: ${datesInData.joinToString(", ")}")
            
            // Check if today's data exists in the local dates
            val containsToday = datesInData.contains(todayStr)
            println("Data contains today's date ($todayStr): $containsToday")

            // Calculate local date to UTC offsets - helps debug date shift issues
            val localToUtc = java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
            println("Local to UTC offset: $localToUtc (${localToUtc.totalSeconds / 3600.0} hours)")

            // Extract values keeping the original order from the API
            // This ensures we don't reorder or modify the data unnecessarily
            val values = adjustedGraphData.map { it.value.toString() }

            // Generate simple labels for daily chart (DD MMM format)
            // We'll convert to local time in the UI correctly
            val labels = adjustedGraphData.map { point ->
                val timestampDatePart = point.timestamp.substringBefore("T")
                try {
                    val date = java.time.LocalDate.parse(timestampDatePart)
                    // Format as "DD MMM" with capitalized month abbreviation
                    val day = date.dayOfMonth.toString().padStart(2, '0')
                    val month = date.month.toString().take(3).capitalize()
                    "$day $month" // Example: "25 May"
                } catch (e: Exception) {
                    // Fallback if parsing fails
                    println("Error parsing date: $timestampDatePart - ${e.message}")
                    timestampDatePart
                }
            }

            // Store all raw data for debugging
            params["raw_data"] = "true"

            // Store the formatted data in params
            params["labels"] = labels.joinToString(",")
            params["values"] = values.joinToString(",")
            params["isDaily"] = "true"
            params["dateFormat"] = "dd MMM"
            
            // Store parameter ID for reference
            params["parameterId"] = parameterId.toString()

            // Mark if we're looking at the current month
            val isCurrentMonth = adjustedGraphData.any {
                try {
                    val date = java.time.ZonedDateTime.parse(it.timestamp).toLocalDate()
                    date.year == today.year && date.month == today.month
                } catch (e: Exception) {
                    it.timestamp.startsWith(today.withDayOfMonth(1).toString())
                }
            }
            params["isCurrentMonth"] = isCurrentMonth.toString()

            // Format today's date for highlighting
            val todayFormatted = "${today.dayOfMonth.toString().padStart(2, '0')} ${today.month.toString().take(3).capitalize()}"
            params["todayDate"] = todayFormatted

            println("PROCESSED DAILY CHART: ${labels.size} labels with format ${params["dateFormat"]}")
            println("Today's date formatted as: ${params["todayDate"]}")
            println("Full labels list: ${labels.joinToString(", ")}")
            println("Full values list: ${values.joinToString(", ")}")

        } else if (chartType == ChartType.BAR_HOURLY) {
            // Generate simple hour labels (00:00 format) for the UI to convert
            val labels = adjustedGraphData.map { point ->
                try {
                    val timestamp = point.timestamp
                    val hourPart = timestamp.substringAfter("T").substringBefore(":")
                    "${hourPart}:00"
                } catch (e: Exception) {
                    // Fallback in case of parsing errors
                    println("Error parsing time: ${point.timestamp} - ${e.message}")
                    val hourPart = point.timestamp.substringAfter("T").substringBefore(":")
                    "${hourPart}:00"
                }
            }

            // Extract values
            val values = adjustedGraphData.map { it.value.toString() }

            // Store the formatted data in params
            params["labels"] = labels.joinToString(",")
            params["values"] = values.joinToString(",")
            params["isHourly"] = "true"
            params["isDaily"] = "false"
            params["labelType"] = "time"
            
            // Store parameter ID for reference
            params["parameterId"] = parameterId.toString()

            // Store information about which day this hourly data is for
            if (adjustedGraphData.isNotEmpty()) {
                try {
                    val firstTimestamp = adjustedGraphData.first().timestamp
                    val zonedDateTime = java.time.ZonedDateTime.parse(firstTimestamp)
                    val localDate = zonedDateTime.toLocalDate()

                    // Store ISO format date (YYYY-MM-DD)
                    params["dataForDay"] = localDate.toString()

                    // Store formatted date (DD MMM YYYY)
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                    params["dataForDayFormatted"] = localDate.format(formatter)

                    // Check if this is today's data
                    val today = java.time.LocalDate.now()
                    params["isToday"] = (localDate.equals(today)).toString()

                } catch (e: Exception) {
                    // Fallback to current date if we can't parse
                    params["dataForDay"] = getCurrentDateISOOnly()
                    params["isToday"] = "false"
                }
            } else {
                // Default values if no data
                params["dataForDay"] = getCurrentDateISOOnly()
                params["isToday"] = "false"
            }

            println("PROCESSED HOURLY CHART: ${labels.size} labels for day ${params["dataForDay"]}")
            println("Full hourly labels: ${labels.joinToString(", ")}")
        }

        return params
    }

    private fun mapGaugeChartResponseToParams(response: GaugeChartResponse, parameterId: Int, paramDisplayName: String): Map<String, String> {
        // Log the complete response for debugging
        println("Gauge chart API response: ${response.toString()}")
        
        // Apply multiplier to all threshold values as done in the website
        // Match exactly what the website does in fetchGaugeData()
        val multiplier = response.multiplier
        val minValue = response.minValue * multiplier
        val maxValue = response.maxValue * multiplier
        val lowValue = response.lowValue * multiplier
        val lowLowValue = response.lowLowValue * multiplier
        val highValue = response.highValue * multiplier
        val highHighValue = response.highHighValue * multiplier
        val value = response.value // Value already includes multiplier according to API docs

        // Log the converted values
        println("Gauge values after applying multiplier $multiplier:")
        println("min: $minValue, max: $maxValue, value: $value")
        println("lowLow: $lowLowValue, low: $lowValue, high: $highValue, highHigh: $highHighValue")
        
        // Get unit information from the parameter cache
        val unit = getUnitForParameter(parameterId)
        println("Unit for parameter ${parameterId}: $unit")
        
        // Format all values with EXACTLY 2 decimal places for consistent display
        // Use Locale.US to ensure decimal point is used, not comma
        return mapOf(
            "min" to String.format(Locale.US, "%.2f", minValue),
            "max" to String.format(Locale.US, "%.2f", maxValue), 
            "value" to String.format(Locale.US, "%.2f", value),
            "lowValue" to String.format(Locale.US, "%.2f", lowValue),
            "highValue" to String.format(Locale.US, "%.2f", highValue),
            "highHighValue" to String.format(Locale.US, "%.2f", highHighValue),
            "lowLowValue" to String.format(Locale.US, "%.2f", lowLowValue),
            "multiplier" to multiplier.toString(),
            "timestamp" to response.timestamp,
            "raw_value" to value.toString(), // Keep raw value for debugging
            "unit" to unit, // Use dynamic unit instead of hardcoded value
            "parameterId" to parameterId.toString(), // Store parameter ID for reference
            "parameterDisplayName" to paramDisplayName
        )
    }
    
    /**
     * Get the unit (uomDisplayName) for the specified parameter ID
     * Uses only the parameter data from the API response
     */
    private fun getUnitForParameter(parameterId: Int): String {
        // Try to find the parameter in our API response map
        return try {
            // Instead of checking the database, use the parameterInfoMap that already contains API response
            val username = authManager.getUsername() ?: "lalitvijay@mgumst.org"
            val paramResponse = kotlinx.coroutines.runBlocking {
                try {
                    apiService.getParameters(username)
                } catch (e: Exception) {
                    null
                }
            }
            
            if (paramResponse?.success == true) {
                // Look for this parameter in the API response
                val param = paramResponse.userParameterList.find { it.parameterId == parameterId }
                if (param != null) {
                    println("Found unit for parameter $parameterId from API: ${param.uomDisplayName}")
                    return param.uomDisplayName
                }
            }
            
            // No unit found in API response
            ""
        } catch (e: Exception) {
            println("Error getting unit for parameter $parameterId: ${e.message}")
            // Return empty string if API lookup fails
            ""
        }
    }

    private fun mapMetricChartResponseToParams(response: MetricChartResponse, parameterInfoMap: Map<Int, ParameterItem>): Map<String, String> {
        // Store all parameters and values with their corresponding units
        val params = mutableMapOf<String, String>()
        
        // Store parameter IDs for reference
        val parameterIds = response.graphData.map { it.parameterId.toString() }
        params["parameterIds"] = parameterIds.joinToString(",")
        
        // Add timestamp information
        params["timestamp"] = response.graphData.firstOrNull()?.timestamp ?: System.currentTimeMillis().toString()
        
        // Process each data point
        response.graphData.forEach { point ->
            // Store the value by parameter ID
            val parameterId = point.parameterId.toString()
            params[parameterId] = point.value.toString()
            
            // Get the unit for this parameter - only use API data
            // We'll keep this for now since it's used for display purposes
            val unit = getUnitForParameter(point.parameterId)
            params["unit_$parameterId"] = unit
            
            // Get the display name from the parameter info map
            val displayName = parameterInfoMap[point.parameterId]?.parameterDisplayName ?: ""
            if (displayName.isNotEmpty()) {
                params["displayName_$parameterId"] = displayName
                println("Using display name from API for parameter ${point.parameterId}: $displayName")
            }
            
            // Log the metric value, unit
            println("Metric chart data - Parameter $parameterId: ${point.value} $unit")
        }
        
        return params
    }

    /**
     * Clear chart data cache for a specific chart config
     * This ensures we always get fresh data from the API
     */
    suspend fun clearCacheForChart(chartId: String) {
        println("Clearing cache for chart $chartId")
        withContext(Dispatchers.IO) {
            try {
                // Remove the chart data from our in-memory cache
                chartDataCache.remove(chartId)
                
                // Also remove any entries that might have this chartId as a prefix
                // This handles our unique IDs for monthly and daily data
                val keysToRemove = chartDataCache.keys.filter { it.startsWith(chartId) }
                keysToRemove.forEach { key ->
                    chartDataCache.remove(key)
                    println("Removed cache entry with key: $key")
                }
                
                println("Successfully cleared cache for chart $chartId and related entries")
            } catch (e: Exception) {
                println("Error clearing chart cache: ${e.message}")
            }
        }
    }

    /**
     * Refresh chart data from the server
     * This method automatically handles different chart types and API calls
     */
    suspend fun refreshChartData(chartConfig: ChartConfig): ChartData? {
        println("Refreshing chart data for ${chartConfig.id}")
        
        // First clear the cache for this chart
        clearCacheForChart(chartConfig.id)
        
        try {
            // Get updated parameters from the API
            val updatedParams = refreshChartDataInternal(chartConfig)
            
            // Create new chart data object with updated parameters
            val chartData = ChartData(
                chartId = chartConfig.id,
                chartType = chartConfig.chartType,
                parameters = updatedParams,
                timestamp = System.currentTimeMillis()
            )
            
            // Store in the in-memory cache
            chartDataCache[chartConfig.id] = chartData
            
            // Notify listeners that data has been updated
            _chartDataUpdates.postValue(chartDataCache.values.toList())
            
            // Return the updated chart data
            return chartData
        } catch (e: Exception) {
            println("Error refreshing chart data: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    /**
     * Helper method to check if a parameter map contains real chart data
     * This handles detection of empty API responses or junk data
     */
    private fun checkIfHasRealData(params: Map<String, String>): Boolean {
        // First check if we already know there's no data
        if (params["no_data"] == "true") {
            return false
        }
        
        // Check if we have values
        val valuesStr = params["values"] ?: return false
        
        // Split and parse the values
        val values = valuesStr.split(",").mapNotNull { it.toFloatOrNull() }
        
        // Check if we have at least one non-zero value
        if (values.isEmpty()) {
            return false
        }
        
        // Check if all values are just zeros (which often indicates no real data)
        val allZeros = values.all { it == 0f }
        if (allZeros) {
            println("API returned all zeros, treating as no data")
            return false
        }
        
        // Check if we also have labels
        val labels = params["labels"]?.split(",") ?: return false
        
        // Make sure values and labels match in length
        if (values.size != labels.size) {
            println("Value and label counts don't match: ${values.size} values, ${labels.size} labels")
            return false
        }
        
        // Looks like we have real data
        println("Found real data: ${values.size} values with sum ${values.sum()}")
        return true
    }

    /**
     * Verify that the data returned is for the correct time period
     * Checks timestamps against the expected month/day, accounting for UTC to local timezone conversion
     */
    private fun verifyDataTimeframe(params: Map<String, String>, expectedMonth: Int? = null, expectedDay: Int? = null): Boolean {
        // Get timestamps from the data
        val timestamps = params["timestamps"]?.split(",") ?: return true // No timestamps, assume correct
        
        if (timestamps.isEmpty()) {
            return true // No timestamps, assume correct
        }
        
        // For hourly data specifically, don't strictly verify day boundaries
        // Since the API always returns data in UTC, a day request in local time may return data
        // that spans two UTC days, especially for timezones far from UTC
        if (params["isHourly"] == "true" && expectedDay != null) {
            // Instead of verifying exact day match, just check if we have any data
            return timestamps.isNotEmpty()
        }
        
        println("CRITICAL DEBUG: Verifying ${timestamps.size} timestamps for expectedMonth=${expectedMonth}, expectedDay=${expectedDay}")
        
        // If we don't have any constraints, don't do verification
        if (expectedMonth == null && expectedDay == null) {
            return true
        }
        
        // If expectedDay is provided, we need to adjust for timezone differences
        // The API returns UTC timestamps, but the user selects dates in local time
        val adjustedExpectedDay = if (expectedDay != null) {
            // For UTC date+time that corresponds to the start of expected day in local time zone
            val localDate = java.time.LocalDate.of(
                expectedDay?.let { java.time.LocalDate.now().year } ?: 2025,
                expectedMonth?.let { it + 1 } ?: 5,  // Month is 0-based in Calendar
                expectedDay ?: 1
            )
            
            // Convert the start of this day to UTC
            val startOfDayUTC = localDate.atStartOfDay(java.time.ZoneId.systemDefault())
                .withZoneSameInstant(java.time.ZoneOffset.UTC)
            
            // Get the actual UTC day we should use for comparison
            val utcDay = startOfDayUTC.dayOfMonth
            println("CRITICAL DEBUG: Adjusted expected day from local $expectedDay to UTC $utcDay")
            utcDay
        } else {
            expectedDay
        }
        
        val adjustedExpectedMonth = if (expectedMonth != null) {
            // Similar adjustment for month
            val localDate = java.time.LocalDate.of(
                2025,
                expectedMonth + 1,  // Month is 0-based in Calendar
                15  // Use middle of month to avoid edge cases
            )
            
            // Convert to UTC
            val middleOfMonthUTC = localDate.atStartOfDay(java.time.ZoneId.systemDefault())
                .withZoneSameInstant(java.time.ZoneOffset.UTC)
            
            // Get UTC month (0-based)
            val utcMonth = middleOfMonthUTC.monthValue - 1
            println("CRITICAL DEBUG: Adjusted expected month from local ${expectedMonth+1} to UTC ${utcMonth+1}")
            utcMonth
        } else {
            expectedMonth
        }
        
        // Keep track of matching vs non-matching timestamps
        var matchingTimestamps = 0
        var nonMatchingTimestamps = 0
        
        // Parse a few timestamps to check their dates - directly in UTC
        try {
            val checkedTimestamps = if (timestamps.size > 10) {
                // If many timestamps, check the first few, middle few, and last few
                val firstFew = timestamps.take(3)
                val middle = timestamps.drop(timestamps.size / 2 - 1).take(3)
                val lastFew = timestamps.takeLast(3)
                (firstFew + middle + lastFew).distinct()
            } else {
                timestamps
            }
            
            println("CRITICAL DEBUG: Checking ${checkedTimestamps.size} representative timestamps")
            
            // For each timestamp, parse as UTC and check the month/day directly
            for (timestampStr in checkedTimestamps) {
                try {
                    // Parse timestamp directly as UTC - don't convert to local time for comparison
                    val utcTimestamp = java.time.ZonedDateTime.parse(timestampStr)
                    val utcYear = utcTimestamp.year
                    val utcMonth = utcTimestamp.monthValue - 1  // Convert to 0-based for comparison
                    val utcDay = utcTimestamp.dayOfMonth
                    
                    println("CRITICAL DEBUG: Parsed UTC timestamp $timestampStr directly to $utcYear-${utcMonth+1}-$utcDay")
                    
                    // If expecting a specific month, check against adjusted UTC month
                    if (adjustedExpectedMonth != null) {
                        if (utcMonth != adjustedExpectedMonth) {
                            println("CRITICAL DEBUG: UTC timestamp month ${utcMonth+1} doesn't match adjusted expected month ${adjustedExpectedMonth+1}")
                            nonMatchingTimestamps++
                        } else {
                            matchingTimestamps++
                        }
                    }
                    
                    // If expecting a specific day, check against adjusted UTC day
                    if (adjustedExpectedDay != null) {
                        if (utcDay != adjustedExpectedDay) {
                            println("CRITICAL DEBUG: UTC timestamp day $utcDay doesn't match adjusted expected day $adjustedExpectedDay")
                            nonMatchingTimestamps++
                        } else {
                            matchingTimestamps++
                        }
                    }
                } catch (e: Exception) {
                    println("CRITICAL DEBUG: Could not parse timestamp directly as UTC: $timestampStr - ${e.message}")
                    // Fallback to previous method if direct parsing fails
                    val timestamp = parseTimestampToLocalDate(timestampStr)
                    if (timestamp == null) {
                        println("CRITICAL DEBUG: Could not parse timestamp with fallback method: $timestampStr")
                        continue
                    }
                    
                    // Log the parsed date
                    val cal = timestamp
                    val parsedYear = cal.get(Calendar.YEAR)
                    val parsedMonth = cal.get(Calendar.MONTH)
                    val parsedDay = cal.get(Calendar.DAY_OF_MONTH)
                    println("CRITICAL DEBUG: Fallback parsed timestamp $timestampStr to $parsedYear-${parsedMonth+1}-$parsedDay")
                    
                    // Use original expected values for fallback comparison
                    if (expectedMonth != null && parsedMonth != expectedMonth) {
                        nonMatchingTimestamps++
                    } else if (expectedMonth != null) {
                        matchingTimestamps++
                    }
                    
                    if (expectedDay != null && parsedDay != expectedDay) {
                        nonMatchingTimestamps++
                    } else if (expectedDay != null) {
                        matchingTimestamps++
                    }
                }
            }
            
            // Accept data if at least one timestamp matches the expected date
            // This is more lenient than before since we're near timezone boundaries
            val result = matchingTimestamps > 0
            println("CRITICAL DEBUG: Timestamp verification result: $result (matching: $matchingTimestamps, non-matching: $nonMatchingTimestamps)")
            return result
            
        } catch (e: Exception) {
            println("CRITICAL DEBUG: Error verifying timestamps: ${e.message}")
            return false // On error, assume data is incorrect
        }
    }
    
    /**
     * Parse a timestamp string to a Calendar in local timezone
     */
    private fun parseTimestampToLocalDate(timestamp: String): Calendar? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )
        
        for (formatStr in formats) {
            try {
                val format = SimpleDateFormat(formatStr, Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                
                // Special handling for +00:00 format
                val parsableTimestamp = if (timestamp.contains("+00:00")) {
                    timestamp.replace("+00:00", "Z")
                } else {
                    timestamp
                }
                
                val date = format.parse(parsableTimestamp)
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    return calendar
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        return null
    }
}