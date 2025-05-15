package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.repositories.ChartRepository
import kotlinx.coroutines.launch

class ChartViewModel(private val repository: ChartRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Get all chart configurations
    fun getAllChartConfigs(): LiveData<List<ChartConfig>> {
        return repository.getAllChartConfigs()
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

    // Refresh all chart data from API
    fun refreshAllChartData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshAllChartData()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to refresh chart data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Refresh specific chart data
    fun refreshChartData(chartId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshChartData(chartId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to refresh chart data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clear error
    fun clearError() {
        _error.value = null
    }

    // Factory class for creating ChartViewModel with repository dependency
    class Factory(private val repository: ChartRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChartViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}