package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mydrishti.co.`in`.activities.repositories.ChartRepository


class ChartParameterViewModelFactory(private val chartRepository: ChartRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChartParametersViewModel::class.java)) {
                return ChartParametersViewModel(chartRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }