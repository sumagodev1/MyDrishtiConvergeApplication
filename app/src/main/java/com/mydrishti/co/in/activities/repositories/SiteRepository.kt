package com.mydrishti.co.`in`.activities.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.models.Device
import com.mydrishti.co.`in`.activities.models.DeviceParameter
import com.mydrishti.co.`in`.activities.models.DeviceParameterRequest
import com.mydrishti.co.`in`.activities.models.ParameterEntity
import com.mydrishti.co.`in`.activities.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for site and parameter data
 */
class SiteRepository(
    private val apiService: ApiService,
    private val authManager: SessionManager
) {
    // LiveData for the list of devices
    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices

    // Store the full device parameters for later reference
    private val _deviceParameters = mutableMapOf<Int, List<ParameterEntity>>()

    /**
     * Load sites with parameters for a specific chart type
     */
    suspend fun loadSitesForChartType(chartType: String) {
        withContext(Dispatchers.IO) {
            try {
                // Map chart type enum to API chart type string
                val apiChartType = when (chartType) {
                    "BAR_DAILY" -> "bar-chart"
                    "BAR_HOURLY" -> "hourly-bar-chart"
                    "GAUGE" -> "gauge-chart"
                    "METRIC" -> "metric-chart"
                    else -> throw IllegalArgumentException("Invalid chart type: $chartType")
                }

                // Create request with user email and chart type
                val request = DeviceParameterRequest(
                    userEmailId = authManager.getUsername() ?: "",
                    type = apiChartType
                )

                // Call API
                val response = apiService.getDeviceParameters(request)

                // Extract Device objects and store parameters
                val devices = response.deviceParameter.map { deviceParam ->
                    // Store the parameters for this device
                    _deviceParameters[deviceParam.deviceEntity.iotDeviceMapId] = deviceParam.parameterEntityList

                    // Convert DeviceEntity to Device object
                    with(deviceParam.deviceEntity) {
                        Device(
                            iotDeviceMapId = iotDeviceMapId,
                            deviceName = deviceName,
                            deviceDisplayName = deviceDisplayName,
                            deviceActivationTimestamp = deviceActivationTimestamp,
                            protocol = protocol
                        )
                    }
                }

                // Update LiveData with devices
                _devices.postValue(devices)
            } catch (e: Exception) {
                // Handle error
                _devices.postValue(emptyList())
                throw e
            }
        }
    }

    /**
     * Get parameters for a specific device
     */
    fun getParametersForDevice(deviceId: Int): List<ParameterEntity> {
        return _deviceParameters[deviceId] ?: emptyList()
    }
}