package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.models.Device
import com.mydrishti.co.`in`.activities.models.Site
import kotlinx.coroutines.launch

class SiteViewModel(private val apiService: ApiService) : ViewModel() {
    private val _sites = MutableLiveData<List<Device>>()
    val sites: LiveData<List<Device>> = _sites

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Load sites for bar charts (daily/hourly)
    fun loadBarChartSites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Assuming you have an API endpoint for bar chart sites
                // If not, you might need to filter from all sites
                val response = apiService.getSites() // Or a specific endpoint for bar chart sites
                _sites.value = response.deviceList
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load bar chart sites"
                if (_sites.value.isNullOrEmpty()) {
                    _sites.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load sites for gauge charts
    fun loadGaugeChartSites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Assuming you have an API endpoint for gauge chart sites
                // If not, you might need to filter from all sites
                val response = apiService.getSites() // Or a specific endpoint for bar chart sites
                _sites.value = response.deviceList
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load gauge chart sites"
                if (_sites.value.isNullOrEmpty()) {
                    _sites.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load sites for metric charts
    fun loadMetricChartSites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Assuming you have an API endpoint for metric chart sites
                // If not, you might need to filter from all sites
                val response = apiService.getSites() // Or a specific endpoint for bar chart sites
                _sites.value = response.deviceList
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load metric chart sites"
                if (_sites.value.isNullOrEmpty()) {
                    _sites.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Original loadSites function (kept for backward compatibility)
    private fun loadSites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getSites() // Or a specific endpoint for bar chart sites
                _sites.value = response.deviceList
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load sites"
                if (_sites.value.isNullOrEmpty()) {
                    _sites.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Refresh sites
    fun refreshSites() {
        loadSites()
    }

    // Clear error
    fun clearError() {
        _error.value = null
    }


}