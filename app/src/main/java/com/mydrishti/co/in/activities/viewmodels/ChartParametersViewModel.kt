package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydrishti.co.`in`.activities.ChartParametersActivity.ParameterInfo
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.repositories.ChartRepository
import com.mydrishti.co.`in`.activities.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for chart parameter selection and configuration
 */
class ChartParametersViewModel(
    private val chartRepository: ChartRepository,
    private val authManager: SessionManager? = null
) : ViewModel() {

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
                println("Loading parameters for chart type: $chartType and site ID: $siteId")

                // First, get the device information using the siteId
                val devices = chartRepository.getDevices()

                // Check if we have any devices returned
                if (devices.isEmpty()) {
                    println("No devices available from API")
                    _error.postValue("No devices available. Please try again later.")
                    _availableParameters.postValue(emptyList())
                    _isLoading.postValue(false)
                    return@launch
                }

                // Convert siteId to Int for comparison
                val siteIdInt = siteId.toInt()

                // Log device IDs for debugging
                val deviceIds = devices.map { it.iotDeviceMapId }
                println("Available device IDs: $deviceIds, Looking for: $siteIdInt")

                // Find the device with matching ID
                val device = devices.find { it.iotDeviceMapId == siteIdInt }

                if (device != null) {
                    println("Found device: ${device.deviceName} (ID: ${device.iotDeviceMapId})")

                    // Pass the device object and chart type to the repository method
                    val parameters = chartRepository.getParametersForDevice(device, chartType)

                    if (parameters.isEmpty()) {
                        println("No parameters available for device ${device.deviceName}")
                        _error.postValue("No parameters available for this device")
                    } else {
                        println("Found ${parameters.size} parameters for device ${device.deviceName}")
                    }

                    _availableParameters.postValue(parameters)
                } else {
                    println("Device with ID $siteId not found in available devices")
                    _error.postValue("Device with ID $siteId not found. Available IDs: $deviceIds")
                    _availableParameters.postValue(emptyList())
                }
                _isLoading.postValue(false)
            } catch (e: Exception) {
                println("Error loading parameters: ${e.message}")
                e.printStackTrace()
                _error.postValue("Failed to load parameters: ${e.message}")
                _availableParameters.postValue(emptyList())
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
                // Debug log
                println("Saving chart: ${chartConfig.title} with ID: ${chartConfig.id}")

                // Insert chart into database
                chartRepository.insertChart(chartConfig)

                // Immediately refresh chart data to fetch actual values
                println("Refreshing chart data after saving")
                try {
                    chartRepository.refreshChartData(chartConfig.id)
                } catch (e: Exception) {
                    println("Error refreshing chart data after save: ${e.message}")
                    // Don't fail the entire operation if refresh fails
                }

                _chartSaveResult.postValue(true)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                println("Error saving chart: ${e.message}")
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
                // Debug log
                println("Updating chart: ${chartConfig.title} with ID: ${chartConfig.id}")

                // Update chart in database
                chartRepository.updateChart(chartConfig)

                // Immediately refresh chart data to fetch actual values
                println("Refreshing chart data after updating")
                try {
                    chartRepository.refreshChartData(chartConfig.id)
                } catch (e: Exception) {
                    println("Error refreshing chart data after update: ${e.message}")
                    // Don't fail the entire operation if refresh fails
                }

                _chartSaveResult.postValue(true)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                println("Error updating chart: ${e.message}")
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
     * Refresh chart data
     */
    fun refreshChartData(chartId: String) {
        println("Initiating chart data refresh for chart ID: $chartId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Get the chart to refresh
                val chart = chartRepository.getChartById(chartId)

                // Add debug log to see chart type
                println("Refreshing chart with type: ${chart?.chartType}")

                // Refresh chart data
                chartRepository.refreshChartData(chartId)

                // Log success
                println("Chart data refresh completed successfully for ID: $chartId")
                _isLoading.postValue(false)
            } catch (e: Exception) {
                println("Error during chart data refresh: ${e.message}")
                _error.postValue("Failed to refresh chart data: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Get the most recently created chart ID
     * (workaround for getting the ID of a newly created chart)
     */
    fun getLatestChartId(): String {
        // This is a simplification - in a real app you would have a better way to track IDs
        // For example, the save method could return the new ID
        return ""  // Returning empty string as a fallback
    }
}