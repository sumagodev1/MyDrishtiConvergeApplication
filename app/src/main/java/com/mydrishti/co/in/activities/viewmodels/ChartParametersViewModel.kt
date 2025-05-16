package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mydrishti.co.`in`.activities.ChartParametersActivity.ParameterInfo
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.repositories.ChartRepository
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for chart parameter selection and configuration
 */
class ChartParametersViewModel(private val chartRepository: ChartRepository) : ViewModel() {

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Error handling
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Chart save/update result
    private val _chartSaveResult = MutableLiveData<Boolean>(false)
    val chartSaveResult: LiveData<Boolean> = _chartSaveResult

    // Available parameters for current site and chart type
    private val _availableParameters = MutableLiveData<List<ParameterInfo>>(listOf())
    val availableParameters: LiveData<List<ParameterInfo>> = _availableParameters

    // Current chart configuration (for editing)
    private val _chartConfig = MutableLiveData<ChartConfig?>(null)
    val chartConfig: LiveData<ChartConfig?> = _chartConfig

    // Selected date range for bar charts
    private val _startDate = MutableLiveData<Date>()
    val startDate: LiveData<Date> = _startDate

    private val _endDate = MutableLiveData<Date>()
    val endDate: LiveData<Date> = _endDate

    /**
     * Load available parameters for a specific chart type and site
     */
    fun loadAvailableParameters(chartType: ChartType, siteId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // First, get the device information using the siteId
                val devices = chartRepository.getDevices()
                val device = devices.find { it.iotDeviceMapId == siteId.toInt() }

                if (device != null) {
                    // Pass the device object to the repository method
                    val parameters = chartRepository.getParametersForDevice(device)
                    _availableParameters.postValue(parameters)
                } else {
                    _error.postValue("Device with ID $siteId not found")
                }
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _error.postValue("Failed to load parameters: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Load existing chart configuration for editing
     */
    fun loadChartConfig(chartId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val config = chartRepository.getChartById(chartId.toString())
                _chartConfig.postValue(config)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _error.postValue("Failed to load chart configuration: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Save new chart configuration
     */
    fun saveChart(chartConfig: ChartConfig) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                chartRepository.insertChart(chartConfig)
                _chartSaveResult.postValue(true)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _error.postValue("Failed to save chart: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Update existing chart configuration
     */
    fun updateChart(chartConfig: ChartConfig) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                chartRepository.updateChart(chartConfig)
                _chartSaveResult.postValue(true)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _error.postValue("Failed to update chart: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Set date range for bar charts
     */
    fun setDateRange(startDate: Date, endDate: Date) {
        _startDate.value = startDate
        _endDate.value = endDate
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Factory for creating ChartParametersViewModel with dependencies
     */

}