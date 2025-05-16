package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mydrishti.co.`in`.activities.api.ApiService

    // Factory class for creating SiteViewModel with API service dependency
    class SiteViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SiteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SiteViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
