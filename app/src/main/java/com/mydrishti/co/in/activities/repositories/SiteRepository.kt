package com.mydrishti.co.`in`.activities.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.models.DeviceParameter
import com.mydrishti.co.`in`.activities.models.DeviceParameterRequest
import com.mydrishti.co.`in`.activities.models.Parameter
import com.mydrishti.co.`in`.activities.models.Site
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
    private val _sites = MutableLiveData<List<DeviceParameter>>()
    val sites: LiveData<List<DeviceParameter>> = _sites

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

                // Map response to domain models
                val deviceParameters = response.deviceParameter.map { wrapper ->
                    // Map device entity to Site
                    val site = Site(
                        id = wrapper.deviceEntity.iotDeviceMapId,
                        name = wrapper.deviceEntity.deviceName,
                        displayName = wrapper.deviceEntity.deviceDisplayName,
                        activationTimestamp = wrapper.deviceEntity.deviceActivationTimestamp,
                        protocol = wrapper.deviceEntity.protocol,
                        location = "Unknown",
                    )

                    // Map parameter entities to Parameters
                    val parameters = wrapper.parameterEntityList.map { paramEntity ->
                        Parameter(
                            id = paramEntity.parameterId,
                            name = paramEntity.parameterName,
                            displayName = paramEntity.parameterDisplayName,
                            uomDisplayName = paramEntity.uomDisplayName
                        )
                    }

                    // Create DeviceParameter with site and parameters
                    DeviceParameter(
                        device = site,
                        parameters = parameters
                    )
                }

                // Update LiveData with results
                _sites.postValue(deviceParameters)
            } catch (e: Exception) {
                // Handle error
                _sites.postValue(emptyList())
                throw e
            }
        }
    }
}