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

class ChartViewModel(private val repository: ChartRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
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
            repository.updateChartPositions(charts)
        }
    }
    
    // Refresh all chart data
    fun refreshAllChartData() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                // Get all chart configs
                val charts = repository.getAllChartConfigs().value ?: listOf()
                
                // First refresh all non-gauge charts
                repository.refreshAllChartData()
                
                // Then handle gauge charts specially to ensure they show correct values
                charts.filter { it.chartType == com.mydrishti.co.`in`.activities.models.ChartType.GAUGE }
                    .forEach { gaugeChart ->
                        println("Ensuring fresh data for gauge chart ${gaugeChart.id}")
                        
                        // Small delay before refreshing gauge charts again
                        kotlinx.coroutines.delay(500)
                        
                        // Second refresh for gauge charts to ensure correct values
                        repository.refreshChartData(gaugeChart.id)
                    }
            } catch (e: Exception) {
                _error.value = "Failed to refresh chart data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Refresh a specific chart's data
    fun refreshChartData(chartId: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                repository.refreshChartData(chartId)
            } catch (e: Exception) {
                _error.value = "Failed to refresh chart data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Special function to ensure gauge charts load proper data
    fun ensureGaugeChartDataFresh(chartId: String) {
        viewModelScope.launch {
            try {
                // Get the current chart config
                val chart = repository.getChartById(chartId)
                
                // Only process gauge charts
                if (chart != null && chart.chartType == com.mydrishti.co.`in`.activities.models.ChartType.GAUGE) {
                    println("Ensuring fresh data for gauge chart $chartId")
                    
                    // Refresh data twice to overcome the 1.1 issue
                    repository.refreshChartData(chartId)
                    
                    // Small delay to ensure first refresh completes
                    kotlinx.coroutines.delay(500)
                    
                    // Refresh again to get correct data
                    repository.refreshChartData(chartId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to refresh gauge chart: ${e.message}"
            }
        }
    }

    // Clear error
    fun clearError() {
        _error.value = null
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