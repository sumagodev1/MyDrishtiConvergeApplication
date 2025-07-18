package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartData
import com.mydrishti.co.`in`.activities.repositories.ChartRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class ChartViewModel(private val repository: ChartRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Track last refresh time for each chart to implement debouncing
    private val lastChartRefreshTimes = mutableMapOf<String, Long>()
    
    // Track last refresh job for each chart to support cancellation
    private val chartRefreshJobs = mutableMapOf<String, Job>()
    
    // Get chart data updates from repository
    val chartDataUpdates: LiveData<List<ChartData>> = repository.chartDataUpdates

    // Get all chart configurations
    fun getAllChartConfigs(): LiveData<List<ChartConfig>> {
        return repository.getAllChartConfigs()
    }
    
    // Get chart data for a specific chart
    fun getChartData(chartId: String): ChartData? {
        return repository.getChartData(chartId)
    }
    
    // Get all chart data
    fun getAllChartData(): List<ChartData> {
        return repository.getAllChartData()
    }

    // Insert a chart
    fun insertChart(chartConfig: ChartConfig) {
        viewModelScope.launch {
            repository.insertChart(chartConfig)
        }
    }

    // Update a chart
    fun updateChart(chartConfig: ChartConfig) {
        viewModelScope.launch {
            repository.updateChart(chartConfig)
        }
    }

    // Delete a chart
    fun deleteChart(chartConfig: ChartConfig) {
        viewModelScope.launch {
            repository.deleteChart(chartConfig)
        }
    }

    // Update chart positions
    fun updateChartPositions(charts: List<ChartConfig>) {
        viewModelScope.launch {
            try {
                // First make sure all positions are sequential and correct
                val updatedCharts = charts.mapIndexed { index, chart ->
                    chart.copy(position = index)
                }
                
                // Update each chart's position in the database
                repository.updateChartPositions(updatedCharts)
                
                println("Updated ${charts.size} chart positions")
            } catch (e: Exception) {
                println("Error updating chart positions: ${e.message}")
                _error.value = "Failed to update chart positions: ${e.message}"
            }
        }
    }
    
    // Track global refresh time
    private var lastAllChartsRefreshTime = 0L
    
    // Refresh all chart data with improved performance
    fun refreshAllChartData() {
        if (repository is com.mydrishti.co.`in`.activities.repositories.ChartRepository) {
            val context = repository.getContext()
            if (context != null && !com.mydrishti.co.`in`.activities.utils.NetworkUtils.isNetworkAvailable(context)) {
                _error.value = "No internet connection. Please connect to the internet."
                _isLoading.value = false
                return
            }
        }
        // Prevent multiple simultaneous refreshes
        if (_isLoading.value == true) {
            println("Skipping refresh - already in progress")
            return
        }
        
        // Implement debouncing for global refresh
        val now = System.currentTimeMillis()
        val lastAllRefreshTime = lastAllChartsRefreshTime
        if (now - lastAllRefreshTime < 3000) { // 3 second debounce for all charts refresh
            println("Debouncing global refresh - too soon since last refresh")
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        // Update global refresh timestamp
        lastAllChartsRefreshTime = now
        
        viewModelScope.launch {
            try {
                println("Starting charts refresh...")
                // Get all chart configs
                val charts = repository.getAllChartConfigs().value ?: listOf()
                
                // Refresh all charts in a single call
                repository.refreshAllChartData()
                
                // Reduced delay before any additional processing
                delay(200)
                
                println("Charts refresh completed")
                
                // Set timeout to ensure refreshing state doesn't get stuck
                delay(7000) // 7 seconds maximum for global refresh indicator
            } catch (e: Exception) {
                _error.value = "Failed to refresh chart data: ${e.message}"
                println("Charts refresh failed: ${e.message}")
            } finally {
                // Always set loading state to false when complete
                _isLoading.value = false
                
                // Reset the "refreshed via swipe" flag after data is loaded
                // This ensures orientation changes will use saved states again
                com.mydrishti.co.`in`.activities.utils.ChartStateManager.setRefreshedViaSwipe(false)
            }
        }
    }
    
    // Refresh a specific chart's data
    fun refreshChartData(chartId: String) {
        val now = System.currentTimeMillis()
        val lastRefresh = lastChartRefreshTimes[chartId] ?: 0L
        if (now - lastRefresh < 2000) { // 2 seconds debounce
            println("Debouncing chart refresh for $chartId")
            return
        }
        lastChartRefreshTimes[chartId] = now

        // Cancel any ongoing refresh for this chart
        chartRefreshJobs[chartId]?.cancel()

        if (repository is com.mydrishti.co.`in`.activities.repositories.ChartRepository) {
            val context = repository.getContext()
            if (context != null && !com.mydrishti.co.`in`.activities.utils.NetworkUtils.isNetworkAvailable(context)) {
                _error.value = "No internet connection. Please connect to the internet."
                _isLoading.value = false
                return
            }
        }
        _isLoading.value = true
        _error.value = null

        val job = viewModelScope.launch {
            try {
                repository.refreshChartData(chartId)
            } catch (e: Exception) {
                _error.value = "Failed to refresh chart data: ${e.message}"
                println("Chart refresh failed: ${e.message}")
            } finally {
                _isLoading.value = false
                chartRefreshJobs.remove(chartId)
                // Force UI update for this specific chart
                if (chartId.contains("_")) {
                    // Get base chart ID for month-specific charts
                    val baseChartId = chartId.split("_")[0]
                    val charts = repository.getAllChartConfigs().value ?: listOf()
                    val chart = charts.find { it.id == baseChartId }
                    // If we found the base chart, notify about data change
                    if (chart != null) {
                        // Get all chart data to trigger UI update
                        val allData = repository.getAllChartData()
                        repository.refreshAllChartData()
                    }
                }
            }
        }
        chartRefreshJobs[chartId] = job
    }
    
    // Special function to ensure gauge charts load proper data
    fun ensureGaugeChartDataFresh(chartId: String) {
        // Set loading state to prevent refresh loops
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Get the current chart config
                val chart = repository.getChartById(chartId)
                
                // Only process gauge charts
                if (chart != null && chart.chartType == com.mydrishti.co.`in`.activities.models.ChartType.GAUGE) {
                    println("Ensuring fresh data for gauge chart $chartId")
                    
                    // Single refresh with updated parameters should be sufficient
                    repository.refreshChartData(chartId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to refresh gauge chart: ${e.message}"
            } finally {
                // Always ensure loading is set to false when complete
                _isLoading.value = false
            }
        }
    }

    // Clear error
    fun clearError() {
        _error.value = null
    }

    // Clear cache for a chart and all its month-specific variants, then refresh data for the selected month
    fun refreshChartDataForMonth(baseChartId: String, year: Int, month: Int) {
        val monthSpecificId = "${baseChartId}_${year}_${month}"
        viewModelScope.launch {
            repository.clearCacheForChart(baseChartId)
            refreshChartData(monthSpecificId)
        }
    }

    // Clear cache for a chart and all its day-specific variants, then refresh data for the selected day (for hourly bar charts)
    fun refreshChartDataForDay(baseChartId: String, year: Int, month: Int, day: Int) {
        val daySpecificId = "${baseChartId}_${year}_${month}_${day}"
        viewModelScope.launch {
            repository.clearCacheForChart(baseChartId)
            refreshChartData(daySpecificId)
        }
    }
}

class ChartViewModelFactory(private val repository: ChartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}