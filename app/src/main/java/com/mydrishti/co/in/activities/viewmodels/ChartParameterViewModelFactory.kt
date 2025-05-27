package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mydrishti.co.`in`.activities.repositories.ChartRepository
import com.mydrishti.co.`in`.activities.utils.SessionManager

/**
 * Factory for creating ChartParametersViewModel with dependencies
 */
class ChartParameterViewModelFactory(
    private val chartRepository: ChartRepository,
    private val authManager: SessionManager? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChartParametersViewModel::class.java)) {
            return ChartParametersViewModel(chartRepository, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}