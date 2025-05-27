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
import kotlinx.coroutines.withContext
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
            refreshChartData(chartConfig.id)
        }
    }

    // Update a chart
    suspend fun updateChart(chartConfig: ChartConfig) {
        withContext(Dispatchers.IO) {
            chartDao.updateChart(chartConfig)
            // Refresh data since configuration changed
            refreshChartData(chartConfig.id)
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
            val charts = chartDao.getAllChartConfigsSync()

            charts.forEach { chart ->
                refreshChartDataInternal(chart)
            }

            // Notify observers of updated data
            _chartDataUpdates.postValue(chartDataCache.values.toList())
        }
    }

    // Refresh specific chart data
    suspend fun refreshChartData(chartId: String) {
        withContext(Dispatchers.IO) {
            val chart = chartDao.getChartConfigById(chartId) ?: return@withContext
            refreshChartDataInternal(chart)

            // Notify observers of updated data
            _chartDataUpdates.postValue(chartDataCache.values.toList())
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
    private suspend fun refreshChartDataInternal(chart: ChartConfig) {
        try {
            // Convert deviceId from String to Long safely
            val deviceId = chart.deviceId.toIntOrNull() ?: return

            val updatedParams = when (chart.chartType) {
                ChartType.BAR_DAILY -> {
                    // Create request for the bar chart API with timezone-aware date range
                    val dateRange = if (chart.customDateRange != null) {
                        // Use custom date range if specified
                        chart.customDateRange
                    } else {
                        // Use last 30 days by default
                        DateRange(
                            startDate = getLastMonthDate(),
                            endDate = getCurrentDate()
                        )
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
                    println("Daily bar chart request - deviceId: $deviceId, parameters: ${getParameterIds(chart)}")
                    println("Date range: ${dateRange.startDate} to ${dateRange.endDate}")

                    // Call API to get daily bar chart data
                    val response = apiService.getDailyBarChartData(request)
                    
                    // Process the data to handle duplicates caused by timezone conversions
                    val processedParams = processDailyBarChartResponse(response)
                    
                    // Return the processed parameters
                    processedParams
                }
                ChartType.BAR_HOURLY -> {
                    // Get timezone-aware date range for today
                    val dayRange = getDayStartAndEndInUTC()

                    // Create request for the hourly bar chart API
                    val request = BarChartRequest(
                        dateRange = DateRange(
                            startDate = dayRange["startDate"] ?: (getCurrentDateISOOnly() + "T00:00:00Z"),
                            endDate = dayRange["endDate"] ?: (getCurrentDateISOOnly() + "T23:59:59Z")
                        ),
                        deviceDetails = listOf(
                            DeviceDetail(
                                iotDeviceMapId = deviceId,
                                parameterIdList = getParameterIds(chart)
                            )
                        )
                    )

                    // Log API request
                    println("Hourly bar chart request - deviceId: $deviceId, parameters: ${getParameterIds(chart)}")
                    println("Date range: ${request.dateRange.startDate} to ${request.dateRange.endDate}")

                    // Call API to get hourly bar chart data
                    val response = apiService.getHourlyBarChartData(request)
                    mapBarChartResponseToParams(response, chart.chartType)
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

        } catch (e: Exception) {
            // Handle API errors
            println("Error refreshing chart data: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // New function to process daily bar chart response with better handling of duplicates
    private fun processDailyBarChartResponse(response: BarChartResponse): Map<String, String> {
        // Start with the standard processing
        val standardParams = mapBarChartResponseToParams(response, ChartType.BAR_DAILY)
        
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
    private fun mapBarChartResponseToParams(response: BarChartResponse, chartType: ChartType): Map<String, String> {
        val graphData = response.graphData.toMutableList()

        println("CHART DATA (${chartType}): Received ${graphData.size} data points from API.")
        if (graphData.isNotEmpty()) {
            println("First point: ${graphData.first().timestamp}, Last point: ${graphData.last().timestamp}")
            
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

    private fun mapGaugeChartResponseToParams(response: GaugeChartResponse): Map<String, String> {
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
            "unit" to "kW" // Match website's lowercase "kW" unit
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