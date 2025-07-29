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
import java.time.*
import java.util.*
import java.text.SimpleDateFormat
import kotlin.coroutines.cancellation.CancellationException

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
        println("ChartRepository: Attempting to get chart by ID: $chartId")
        return withContext(Dispatchers.IO) {
            try {
                val chart = chartDao.getChartConfigById(chartId)
                if (chart != null) {
                    println("ChartRepository: Successfully retrieved chart with ID: ${chart.id}, Title: ${chart.title}")
                } else {
                    println("ChartRepository: Chart with ID: $chartId not found in database.")
                }
                return@withContext chart
            } catch (e: Exception) {
                println("ChartRepository: Error retrieving chart by ID ($chartId): ${e.message}")
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
            try {
                println("ChartRepository: Attempting to delete chart with ID: ${chartConfig.id}")
                
                // Extract base chart ID by removing any time-specific suffixes (_year_month or _year_month_day)
                val baseChartId = chartConfig.id.split("_")[0]
                println("ChartRepository: Base chart ID: $baseChartId")
                
                // First try to find the chart with the exact ID
                var existingChart = chartDao.getChartConfigById(chartConfig.id)
                var chartIdToDelete = chartConfig.id
                
                // If not found with exact ID, try with base ID (for month/day specific charts)
                if (existingChart == null && baseChartId != chartConfig.id) {
                    println("ChartRepository: Chart not found with exact ID, trying base ID: $baseChartId")
                    existingChart = chartDao.getChartConfigById(baseChartId)
                    if (existingChart != null) {
                        chartIdToDelete = baseChartId
                        println("ChartRepository: Found chart with base ID: $baseChartId")
                    }
                }
                
                if (existingChart == null) {
                    println("ChartRepository: WARNING - Chart with ID ${chartConfig.id} (base: $baseChartId) not found in database")
                    
                    // As a final attempt, let's check if there are any charts in the database at all
                    val allCharts = chartDao.getAllChartConfigsSync()
                    println("ChartRepository: Total charts in database: ${allCharts.size}")
                    if (allCharts.isNotEmpty()) {
                        println("ChartRepository: Available chart IDs in database:")
                        allCharts.forEach { chart ->
                            println("  - ${chart.id} (title: ${chart.title})")
                        }
                        
                        // Try to find a chart that matches by title and device as a last resort
                        val matchingChart = allCharts.find { 
                            it.title == chartConfig.title && it.deviceId == chartConfig.deviceId 
                        }
                        if (matchingChart != null) {
                            println("ChartRepository: Found matching chart by title/device: ${matchingChart.id}")
                            chartIdToDelete = matchingChart.id
                            existingChart = matchingChart
                        }
                    }
                    
                    if (existingChart == null) {
                        throw IllegalStateException("Chart not found in database")
                    }
                }
                
                println("ChartRepository: Found existing chart: ${existingChart.title}")
                
                // Use direct deletion by ID instead of object matching
                // This is more reliable than Room's @Delete annotation which requires exact object matching
                val deletedRows = chartDao.deleteChartById(chartIdToDelete)
                
                if (deletedRows > 0) {
                    println("ChartRepository: Successfully deleted $deletedRows chart(s) with ID: $chartIdToDelete")
                    // Remove from cache (both the specific ID and base ID variants)
                    chartDataCache.remove(chartConfig.id)
                    chartDataCache.remove(baseChartId)
                    // Also remove any other cached variants for this base chart
                    val keysToRemove = chartDataCache.keys.filter { it.startsWith("${baseChartId}_") }
                    keysToRemove.forEach { chartDataCache.remove(it) }
                    println("ChartRepository: Cleared cache for chart and its variants")
                } else {
                    println("ChartRepository: ERROR - No rows deleted for chart ID: $chartIdToDelete")
                    throw IllegalStateException("Failed to delete chart - no rows affected")
                }
                
            } catch (e: Exception) {
                println("ChartRepository: Error deleting chart: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    // Update chart positions
    suspend fun updateChartPositions(charts: List<ChartConfig>) {
        withContext(Dispatchers.IO) {
            try {
                // First ensure we're working with the correct position indices
                val updatedCharts = charts.mapIndexed { index, chart ->
                    chart.copy(position = index)
                }
                
                // Update each chart position individually - no transaction needed since positions are independent
                updatedCharts.forEach { chart ->
                    chartDao.updateChartPosition(chart.id, chart.position)
                }
                
                println("Repository: Updated ${updatedCharts.size} chart positions")
            } catch (e: Exception) {
                println("Repository: Error updating chart positions: ${e.message}")
                e.printStackTrace()
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

    // Clear all chart data cache
    fun clearChartDataCache() {
        chartDataCache.clear()
        currentlyRefreshing.clear()
        _chartDataUpdates.postValue(emptyList())
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
                // Always clear cache for this chart before refreshing
                clearCacheForChart(chartId)
                println("[CACHE] Cleared cache for chart $chartId before refresh.")
                // Mark this chart as being refreshed
                currentlyRefreshing.add(chartId)
                println("[REFRESH] Started refresh for $chartId. Currently refreshing: $currentlyRefreshing")

                // Get the chart config
                // For month-specific chart IDs (format: originalId_YYYY_MM), get the base chart ID
                println("CRITICAL DEBUG: Checking chart ID format: $chartId")
                val baseChartId = if ("_" in chartId) {
                    val parts = chartId.split("_")
                    println("CRITICAL DEBUG: Chart ID split into ${parts.size} parts: $parts")
                    parts[0]
                } else {
                    chartId
                }
                println("CRITICAL DEBUG: Using base chart ID: $baseChartId")
                val chart = chartDao.getChartConfigById(baseChartId) ?: return@withContext

                try {
                    // Apply a timeout to the operation
                    withTimeout(10000) { // 10-second timeout for robustness
                        // Check if this is a month-specific chart ID
                        if (chart.chartType == ChartType.BAR_DAILY && "_" in chartId) {
                            println("CRITICAL DEBUG: Detected BAR_DAILY with underscore in ID")
                            // Extract year and month from the ID (format: originalId_YYYY_MM)
                            val parts = chartId.split("_")
                            if (parts.size >= 3) {
                                try {
                                    val year = parts[parts.size - 2].toInt()
                                    val month = parts[parts.size - 1].toInt()
                                    println("Processing month-specific chart: Year=$year, Month=$month (0-based)")

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
                                        println("API response received with ${response["timestamps"]?.split(",")?.size ?: 0} timestamps")

                                        // --- STRICT FILTER: Only include data points in selected local month/year ---
                                        val istZone = java.time.ZoneId.of("Asia/Kolkata")
                                        val timestamps = response["timestamps"]?.split(",") ?: emptyList()
                                        val values = response["values"]?.split(",") ?: emptyList()
                                        val filteredIndices = mutableListOf<Int>()
                                        for ((idx, ts) in timestamps.withIndex()) {
                                            try {
                                                val utcDateTime = java.time.ZonedDateTime.parse(ts)
                                                val localDate = utcDateTime.withZoneSameInstant(istZone).toLocalDate()
                                                if (localDate.year == year && localDate.monthValue == month + 1) {
                                                    filteredIndices.add(idx)
                                                }
                                            } catch (_: Exception) {}
                                        }
                                        if (filteredIndices.isEmpty()) {
                                            println("STRICT FILTER: No data matches selected month $month-$year, setting no_data=true")
                                            val emptyData = ChartData(
                                                chartId = chartId,
                                                chartType = chart.chartType,
                                                parameters = mapOf("no_data" to "true"),
                                                timestamp = System.currentTimeMillis()
                                            )
                                            chartDataCache[chartId] = emptyData
                                            _chartDataUpdates.postValue(chartDataCache.values.toList())
                                            return@withTimeout
                                        } else {
                                            // Build filtered response
                                            val filteredTimestamps = filteredIndices.map { timestamps[it] }
                                            val filteredValues = filteredIndices.map { values.getOrNull(it) ?: "0" }
                                            val enhancedParams = response.toMutableMap()
                                            enhancedParams["timestamps"] = filteredTimestamps.joinToString(",")
                                            enhancedParams["values"] = filteredValues.joinToString(",")
                                            // Generate labels in dd MMM format
                                            val labels = filteredTimestamps.map {
                                                try {
                                                    val utcDateTime = java.time.ZonedDateTime.parse(it)
                                                    val localDate = utcDateTime.withZoneSameInstant(istZone).toLocalDate()
                                                    val day = localDate.dayOfMonth.toString().padStart(2, '0')
                                                    val monthStr = localDate.month.toString().take(3).capitalize()
                                                    "$day $monthStr"
                                                } catch (e: Exception) {
                                                    it
                                                }
                                            }
                                            enhancedParams["labels"] = labels.joinToString(",")
                                            // Defensive: ensure labels and values always match
                                            if (labels.size != filteredValues.size) {
                                                println("ERROR: Invalid data. Labels: ${labels.size}, Values: ${filteredValues.size}")
                                            }
                                            // Create a chart data object with the filtered response
                                            val chartData = ChartData(
                                                chartId = chartId,
                                                chartType = chart.chartType,
                                                parameters = enhancedParams,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            chartDataCache[chartId] = chartData
                                            _chartDataUpdates.postValue(chartDataCache.values.toList())
                                            println("STRICT FILTER: Stored strictly filtered data for month $month-$year with ${filteredTimestamps.size} values")
                                            return@withTimeout
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

                                        // --- PATCH START: Strict filtering for selected day ---
                                        val timestamps = response["timestamps"]?.split(",") ?: emptyList()
                                        val values = response["values"]?.split(",") ?: emptyList()
                                        val filteredIndices = mutableListOf<Int>()
                                        val ist = java.util.TimeZone.getTimeZone("Asia/Kolkata")
                                        val cal = java.util.Calendar.getInstance(ist)
                                        for ((idx, ts) in timestamps.withIndex()) {
                                            try {
                                                val date = parseTimestamp(ts)
                                                if (date != null) {
                                                    cal.time = date
                                                    val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                                                    val m = cal.get(java.util.Calendar.MONTH)
                                                    val y = cal.get(java.util.Calendar.YEAR)
                                                    if (d == day && m == month && y == year) {
                                                        filteredIndices.add(idx)
                                                    }
                                                }
                                            } catch (_: Exception) {}
                                        }
                                        if (filteredIndices.isEmpty()) {
                                            println("PATCH: No data matches selected day $day-${month+1}-$year, setting no_data=true")
                                            val emptyData = ChartData(
                                                chartId = chartId,
                                                chartType = chart.chartType,
                                                parameters = mapOf("no_data" to "true"),
                                                timestamp = System.currentTimeMillis()
                                            )
                                            chartDataCache[chartId] = emptyData
                                            _chartDataUpdates.postValue(chartDataCache.values.toList())
                                            return@withTimeout
                                        } else {
                                            // Build filtered response
                                            val filteredTimestamps = filteredIndices.map { timestamps[it] }
                                            val filteredValues = filteredIndices.map { values.getOrNull(it) ?: "0" }
                                            val enhancedParams = response.toMutableMap()
                                            enhancedParams["timestamps"] = filteredTimestamps.joinToString(",")
                                            enhancedParams["values"] = filteredValues.joinToString(",")
                                            enhancedParams["selectedDay"] = day.toString()
                                            enhancedParams["selectedMonth"] = month.toString()
                                            enhancedParams["selectedYear"] = year.toString()
                                            // Create a chart data object with the filtered response
                                            val chartData = ChartData(
                                                chartId = chartId,
                                                chartType = chart.chartType,
                                                parameters = enhancedParams,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            chartDataCache[chartId] = chartData
                                            _chartDataUpdates.postValue(chartDataCache.values.toList())
                                            println("PATCH: Stored strictly filtered hourly data for day $day-${month+1}-$year with ${filteredTimestamps.size} values")
                                            return@withTimeout
                                        }
                                        // --- PATCH END ---
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
                    println("[REFRESH] Finished refresh for $chartId. Currently refreshing: $currentlyRefreshing")
                }
            } catch (e: CancellationException) {
                println("Chart data refresh cancelled (timeout or manual): ${e.message}")
                // Optionally, update UI with a user-friendly message or ignore
            } catch (e: Exception) {
                println("Error in refreshChartData with timeout: ${e.message}")
                // Make sure to clear refreshing status even on error
                currentlyRefreshing.remove(chartId)
                println("[REFRESH] Error - cleared $chartId from currentlyRefreshing. Now: $currentlyRefreshing")
            }
        }
    }

    // Add helper method to get date range for a specific month in API format
    private fun getMonthDateRangeForAPI(year: Int, month: Int): DateRange {
        // To match the website: always use the 1st of the month at 00:00 IST as start,
        // and the 27th for February, 30th for all other months at 23:59:59 IST as end
        val zoneId = java.time.ZoneId.of("Asia/Kolkata")
        val localStart = java.time.LocalDate.of(year, month + 1, 1).atStartOfDay(zoneId)
        val utcStart = localStart.withZoneSameInstant(java.time.ZoneOffset.UTC)
        val endDay = if (month + 1 == 2) 27 else 30
        val localEnd = java.time.LocalDate.of(year, month + 1, endDay).atTime(23, 59, 59).atZone(zoneId)
        val utcEnd = localEnd.withZoneSameInstant(java.time.ZoneOffset.UTC)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(java.time.ZoneOffset.UTC)
        val startDate = formatter.format(utcStart)
        val endDate = formatter.format(utcEnd)
        println("DEBUG: getMonthDateRangeForAPI (WEBSITE MATCH) year=$year, month=$month (0-based), startDate=$startDate, endDate=$endDate")
        return DateRange(
            startDate = startDate,
            endDate = endDate
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
                val username = authManager.getUsername()

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
                    userEmailId = authManager.getUsername(),
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
                        // Check if parameterEntityList is null or empty before iterating
                        if (deviceParam.parameterEntityList != null && deviceParam.parameterEntityList.isNotEmpty()) {
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
                        } else {
                            println("No parameters found in parameterEntityList for device ${device.iotDeviceMapId}")
                        }
                    }
                }

                // No fallback to general parameter API - if device-specific parameters are empty, return an empty list
                if (parameterList.isEmpty()) {
                    println("No parameters found for device ${device.iotDeviceMapId} in device-parameter response. Returning empty list without fallback.")
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
            val username = authManager.getUsername() ?: ""
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

                        // CRITICAL DEBUG: Log chart ID to understand its format
                        println("CRITICAL DEBUG: Chart ID format: [${chart.id}], Contains underscore: ${"_" in chart.id}")
                        if ("_" in chart.id) {
                            println("CRITICAL DEBUG: Chart ID parts: ${chart.id.split("_")}")
                        }

                        // Check if this is a month-specific chart ID (format: originalId_YYYY_MM)
                        if ("_" in chart.id) {
                            val parts = chart.id.split("_")
                            println("CRITICAL DEBUG: Chart ID parts: $parts (${parts.size} parts)")

                            if (parts.size >= 3) {
                                try {
                                    val year = parts[parts.size - 2].toInt()
                                    val month = parts[parts.size - 1].toInt()
                                    println("CRITICAL FIX: Detected month-specific chart ID: ${chart.id}")
                                    println("CRITICAL FIX: Using year=$year, month=$month for date range")

                                    // Get the correct date range for this month
                                    val fixedDateRange = getMonthDateRangeForAPI(year, month)
                                    println("CRITICAL FIX: Fixed date range: ${fixedDateRange.startDate} to ${fixedDateRange.endDate}")
                                    fixedDateRange
                                } catch (e: Exception) {
                                    println("CRITICAL FIX: Error parsing month-specific ID: ${e.message}")
                                    // FALLBACK: For month view, if we can't parse but we're in BAR_DAILY type,
                                    // assume current month and use that date range instead of using wrong range
                                    if (chart.chartType == ChartType.BAR_DAILY) {
                                        val currentYear = LocalDate.now().year
                                        val currentMonth = LocalDate.now().monthValue - 1 // Convert to 0-based
                                        println("CRITICAL FIX: Using fallback current month/year: $currentYear-${currentMonth+1}")
                                        val fixedDateRange = getMonthDateRangeForAPI(currentYear, currentMonth)
                                        println("CRITICAL FIX: Fallback date range: ${fixedDateRange.startDate} to ${fixedDateRange.endDate}")
                                        fixedDateRange
                                    } else {
                                        chart.customDateRange
                                    }
                                }
                            } else {
                                // FALLBACK 2: Another attempt to handle non-standard chart ID formats
                                // If chart is BAR_DAILY type, it's likely we want monthly data
                                if (chart.chartType == ChartType.BAR_DAILY) {
                                    println("CRITICAL FIX: BAR_DAILY detected but non-standard ID format. Using current month.")
                                    val currentYear = LocalDate.now().year
                                    val currentMonth = LocalDate.now().monthValue - 1 // Convert to 0-based
                                    val fixedDateRange = getMonthDateRangeForAPI(currentYear, currentMonth)
                                    println("CRITICAL FIX: Alternate fallback date range: ${fixedDateRange.startDate} to ${fixedDateRange.endDate}")
                                    fixedDateRange
                                } else {
                                    chart.customDateRange
                                }
                            }
                        } else {
                            // For BAR_DAILY without underscore, it's likely we want monthly data for current month
                            if (chart.chartType == ChartType.BAR_DAILY) {
                                println("CRITICAL FIX: BAR_DAILY without underscore ID format. Using current month.")
                                val currentYear = LocalDate.now().year
                                val currentMonth = LocalDate.now().monthValue - 1 // Convert to 0-based
                                val fixedDateRange = getMonthDateRangeForAPI(currentYear, currentMonth)
                                println("CRITICAL FIX: Default monthly date range: ${fixedDateRange.startDate} to ${fixedDateRange.endDate}")
                                fixedDateRange
                            } else {
                                chart.customDateRange
                            }
                        }
                    } else {
                        // Use current month's start and end in IST (converted to UTC)
                        val monthRange = getCurrentMonthStartAndEndInUTC()
                        val defaultRange = DateRange(
                            startDate = monthRange["startDate"] ?: "",
                            endDate = monthRange["endDate"] ?: ""
                        )
                        println("API CALL: Using current month date range for chart ${chart.id}: ${defaultRange.startDate} to ${defaultRange.endDate}")
                        defaultRange
                    }

                    // Create request as var so we can reassign it if needed
                    var request = BarChartRequest(
                        dateRange = dateRange,
                        deviceDetails = listOf(
                            DeviceDetail(
                                iotDeviceMapId = deviceId,
                                parameterIdList = getParameterIds(chart)
                            )
                        )
                    )

                    // DIRECT OVERRIDE FOR BAR_DAILY: Always ensure monthly charts have proper date range
                    // This fixes cases where the chart ID format doesn't match our expectations
                    // --- REMOVED OVERRIDE BLOCK: Do not forcibly override the date range ---
                    // (The app should always use the date range calculated for the selected month)
                    // ... existing code continues ...

                    // Log API request
                    println("API CALL: Daily bar chart request - deviceId: $deviceId, parameters: ${getParameterIds(chart)}")
                    println("API CALL: Date range: ${request.dateRange.startDate} to ${request.dateRange.endDate}")

                    // Use Gson to convert the request to JSON to see the exact format
                    val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    val jsonPayload = gson.toJson(request)
                    println("CRITICAL DEBUG: EXACT JSON PAYLOAD:\n$jsonPayload")

                    println("CRITICAL DEBUG: API request payload: {\"dateRange\":{\"startDate\":\"${request.dateRange.startDate}\",\"endDate\":\"${request.dateRange.endDate}\"},\"deviceDetails\":[{\"iotDeviceMapId\":$deviceId,\"parameterIdList\":${getParameterIds(chart)}}]}")

                    // Call API to get daily bar chart data
                    println("API CALL: Executing network request for daily bar chart data...")
                    val response = apiService.getDailyBarChartData(request)
                    println("API CALL: Received response with ${response.graphData.size} data points")

                    mapBarChartResponseToParams(response, chart.chartType, parameterInfoMap, chart)
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

                    // The mapMetricChartResponseToParams function already extracts the timestamp
                    // from the API response (using the last/most recent data point)
                    val params = mapMetricChartResponseToParams(response, parameterInfoMap)

                    // Log the timestamp for debugging
                    println("Using API timestamp for METRIC chart: ${params["timestamp"]}")

                    params
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

                                    // Get proper date range for this day
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
                    var request = BarChartRequest(
                        dateRange = dateRange,
                        deviceDetails = listOf(
                            DeviceDetail(
                                iotDeviceMapId = deviceId,
                                parameterIdList = getParameterIds(chart)
                            )
                        )
                    )

                    // Log API request
                    println("API CALL: Hourly bar chart request - deviceId: $deviceId, parameters: ${getParameterIds(chart)}")
                    println("API CALL: Date range: ${request.dateRange.startDate} to ${request.dateRange.endDate}")

                    // Use Gson to convert the request to JSON to see the exact format
                    val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    val jsonPayload = gson.toJson(request)
                    println("CRITICAL DEBUG: EXACT JSON PAYLOAD:\n$jsonPayload")

                    // Call API to get hourly bar chart data
                    val response = apiService.getHourlyBarChartData(request)
                    mapBarChartResponseToParams(response, chart.chartType, parameterInfoMap, chart)
                }
            }

            // Update chart data in cache
            // Determine the timestamp for the chart data
            var chartTimestamp = System.currentTimeMillis()
            if (chart.chartType == ChartType.GAUGE || chart.chartType == ChartType.METRIC) {
                val timestampString = updatedParams["timestamp"]
                if (!timestampString.isNullOrEmpty()) {
                    println("Parsing timestamp for ${chart.chartType}: '$timestampString'")

                    // For METRIC charts, we want to keep the original timestamp string in the parameters
                    // but also parse it to a long for the chartData.timestamp field
                    try {
                        // Try multiple timestamp formats
                        val possibleFormats = arrayOf(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                            "yyyy-MM-dd'T'HH:mm:ssXXX"
                        )

                        var parsedDate: Date? = null

                        // Try each format until one works
                        for (format in possibleFormats) {
                            try {
                                val sdf = SimpleDateFormat(format, Locale.getDefault())
                                sdf.timeZone = TimeZone.getTimeZone("UTC")
                                parsedDate = sdf.parse(timestampString)
                                if (parsedDate != null) {
                                    println("Successfully parsed timestamp with format: $format")
                                    break
                                }
                            } catch (e: Exception) {
                                // Try next format
                            }
                        }

                        // If still null, try one more approach - check if it's a Unix timestamp
                        if (parsedDate == null && timestampString.toLongOrNull() != null) {
                            val unixTimestamp = timestampString.toLong()
                            parsedDate = Date(unixTimestamp)
                            println("Parsed as Unix timestamp: $unixTimestamp")
                        }

                        if (parsedDate != null) {
                            chartTimestamp = parsedDate.time
                            println("Successfully parsed API timestamp to $chartTimestamp ms")
                        } else {
                            println("Failed to parse API timestamp for ${chart.chartType}.")
                        }
                    } catch (e: Exception) {
                        println("Error parsing timestamp: ${e.message}")
                    }
                }
            }

            // Update chart data in cache
            val chartData = ChartData(
                chartId = chart.id,
                chartType = chart.chartType,
                parameters = updatedParams,
                timestamp = chartTimestamp
            )

            // Store in cache
            chartDataCache[chart.id] = chartData

            // Update last updated timestamp in database
            chartDao.updateChartLastUpdated(chart.id, chartTimestamp)

            return updatedParams
        } catch (e: Exception) {
            // Handle API errors
            println("Error refreshing chart data: ${e.message}")
            e.printStackTrace()
            throw e
        }
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
            // For metric charts, limit to 8 parameters
            val parameterIds = if (chart.chartType == ChartType.METRIC && chart.parameterIds.size > 8) {
                println("LIMITING: Metric chart has ${chart.parameterIds.size} parameters, limiting to 8")
                chart.parameterIds.take(8)
            } else {
                chart.parameterIds
            }
            println("Found parameter IDs from chart config: $parameterIds")
            return parameterIds
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

    // Helper method to get current date in YYYY-MM-DD format
    private fun getCurrentDateISOOnly(): String {
        return java.time.LocalDate.now().toString() // Format: YYYY-MM-DD
    }

    // New helper method to get current month's start and end dates in UTC format (IST aware)
    private fun getCurrentMonthStartAndEndInUTC(): Map<String, String> {
        // Always use IST for correct month boundaries
        val zoneId = java.time.ZoneId.of("Asia/Kolkata")

        // First day of the current month at 00:00:00 IST
        val now = java.time.ZonedDateTime.now(zoneId)
        val localStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay(zoneId)

        // Last day of the current month at 23:59:59 IST
        val lastDay = now.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).toLocalDate()
        val localEnd = lastDay.atTime(23, 59, 59).atZone(zoneId)

        // Convert to UTC
        val utcStart = localStart.withZoneSameInstant(java.time.ZoneOffset.UTC)
        val utcEnd = localEnd.withZoneSameInstant(java.time.ZoneOffset.UTC)

        // Format output as ISO 8601 UTC (Z at end)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(java.time.ZoneOffset.UTC)

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
    private fun mapBarChartResponseToParams(
        response: BarChartResponse,
        chartType: ChartType,
        parameterInfoMap: Map<Int, ParameterItem>,
        chart: ChartConfig
    ): Map<String, String> {
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

            // Store UOM display name for legend - this will be used instead of chart name
            val uomDisplayName = paramInfo.uomDisplayName
            // Combine parameter display name with UOM display name (in brackets) for the legend
            val combinedLegendName = if (uomDisplayName.isNotEmpty()) {
                "${displayName} (${uomDisplayName})".trim()
            } else {
                displayName.trim()
            }
            params["legendName"] = combinedLegendName
            println("Using combined legend name for bar chart: $combinedLegendName")
        } else {
            // Fallback to parameter name from database if API info is not available
            val parameterEntity = runBlocking {
                try {
                    parameterDao.getParameterById(parameterId)
                } catch (e: Exception) {
                    null
                }
            }

            val fallbackLegend = if (parameterEntity != null) {
                // Use parameter name from database
                val dbParameterName = parameterEntity.parameterDisplayName
                val dbUomName = parameterEntity.uomDisplayName

                // Use the database parameter name and UOM (in brackets) if available
                if (dbParameterName.isNotEmpty() && dbUomName.isNotEmpty()) {
                    "$dbParameterName (${dbUomName})"
                } else if (dbParameterName.isNotEmpty() && unit.isNotEmpty()) {
                    "$dbParameterName ($unit)"
                } else if (dbParameterName.isNotEmpty()) {
                    dbParameterName
                } else if (unit.isNotEmpty()) {
                    "($unit)"
                } else {
                    ""
                }
            } else {
                // Last resort, just use the unit in brackets if available
                if (unit.isNotEmpty()) {
                    "($unit)"
                } else {
                    ""
                }
            }

            params["legendName"] = fallbackLegend
            println("Using fallback legend name from database: $fallbackLegend")
        }

        if (chartType == ChartType.BAR_DAILY) {
            // --- FILTER: Only include data points in the selected local month/year ---
            val istZone = java.time.ZoneId.of("Asia/Kolkata")
            // Determine selected month/year
            var selectedYear: Int
            var selectedMonth: Int
            // Try to extract from chart.customDateRange if available
            val customDateRange = try {
                val chartField = ChartConfig::class.java.getDeclaredField("customDateRange")
                chartField.isAccessible = true
                chartField.get(chart) as? DateRange
            } catch (e: Exception) { null }
            if (customDateRange != null && !customDateRange.startDate.isNullOrEmpty()) {
                try {
                    val startDate = java.time.ZonedDateTime.parse(customDateRange.startDate)
                    val localStart = startDate.withZoneSameInstant(istZone)
                    selectedYear = localStart.year
                    selectedMonth = localStart.monthValue // 1-based
                } catch (e: Exception) {
                    val now = java.time.ZonedDateTime.now(istZone)
                    selectedYear = now.year
                    selectedMonth = now.monthValue
                }
            } else {
                val now = java.time.ZonedDateTime.now(istZone)
                selectedYear = now.year
                selectedMonth = now.monthValue
            }
            // Strict filter: Only include if local date is in selected year and month
            val filteredGraphData = adjustedGraphData.filter { dataPoint ->
                try {
                    val utcDateTime = java.time.ZonedDateTime.parse(dataPoint.timestamp)
                    val localDate = utcDateTime.withZoneSameInstant(istZone).toLocalDate()
                    val include = localDate.year == selectedYear && localDate.monthValue == selectedMonth
                    println("Filtering: UTC=${dataPoint.timestamp}, Local=$localDate, Selected=$selectedYear-$selectedMonth, Include=$include")
                    include
                } catch (e: Exception) {
                    false
                }
            }
            // Use filteredGraphData for all further processing
            if (filteredGraphData.isEmpty()) {
                return mapOf("no_data" to "true")
            }
            val values = filteredGraphData.map { it.value.toString() }
            val labels = filteredGraphData.map { point ->
                try {
                    val utcDateTime = java.time.ZonedDateTime.parse(point.timestamp)
                    val localDate = utcDateTime.withZoneSameInstant(istZone).toLocalDate()
                    val day = localDate.dayOfMonth.toString().padStart(2, '0')
                    val monthStr = localDate.month.toString().take(3).capitalize()
                    "$day $monthStr"
                } catch (e: Exception) {
                    point.timestamp
                }
            }
            // Defensive: ensure labels and values always match
            if (labels.size != values.size) {
                println("ERROR: Invalid data. Labels: ${labels.size}, Values: ${values.size}")
            }
            params["labels"] = labels.joinToString(",")
            params["values"] = values.joinToString(",")
            params["isDaily"] = "true"
            params["dateFormat"] = "dd MMM"
            return params
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
            // Log the timestamp for debugging
            "timestamp_debug" to "Original timestamp from API: ${response.timestamp}",
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
            val username = authManager.getUsername() ?: ""
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

        // Sort graph data by timestamp to ensure we use the latest data point.
        val sortedGraphData = response.graphData.sortedBy { it.timestamp }

        // Store parameter IDs for reference
        val parameterIds = sortedGraphData.map { it.parameterId.toString() }
        params["parameterIds"] = parameterIds.joinToString(",")

        // Add timestamp information from the latest data point
        val latestTimestamp = sortedGraphData.lastOrNull()?.timestamp ?: ""
        params["timestamp"] = latestTimestamp
        println("Using API timestamp for metric chart: $latestTimestamp")

        // Process each data point
        sortedGraphData.forEach { point ->
            // Store the value by parameter ID. Since data is sorted, this will be the latest value.
            val parameterId = point.parameterId.toString()

            // Store original string value exactly as received from API
            val rawValue = point.value.toString()
            params[parameterId] = rawValue

            // Add a flag to indicate if this is a text/status value (like "ON" or "OFF")
            val isTextValue = rawValue.matches(Regex("[A-Za-z]+"))
            params["isText_$parameterId"] = isTextValue.toString()

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
            println("Metric chart data - Parameter $parameterId: ${point.value} $unit (isText: $isTextValue)")
        }

        return params
    }

    /**
     * Clear chart data cache for a specific chart config
     * This ensures we always get fresh data from the API
     */
    suspend fun clearCacheForChart(chartId: String) {
        println("[CACHE] Clearing cache for chart $chartId")
        withContext(Dispatchers.IO) {
            try {
                // If this is a month-specific or day-specific chart, clear all related cache
                val baseId = chartId.substringBefore("_")
                val keysToRemove = chartDataCache.keys.filter { it == baseId || it.startsWith(baseId + "_") }
                keysToRemove.forEach { key ->
                    chartDataCache.remove(key)
                    println("[CACHE] Removed cache entry with key: $key")
                }
                println("[CACHE] Successfully cleared cache for chart $chartId and related entries")
            } catch (e: Exception) {
                println("[CACHE] Error clearing chart cache: ${e.message}")
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

    fun getContext(): android.content.Context? {
        return authManager.getContext()
    }
}