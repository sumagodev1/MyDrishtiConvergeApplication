package com.mydrishti.co.`in`.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.utils.SessionManager


class SiteViewModelFactory(
private val apiService: ApiService,
private val authManager: SessionManager
) : ViewModelProvider.Factory {
override fun <T : ViewModel> create(modelClass: Class<T>): T {
if (modelClass.isAssignableFrom(SiteViewModel::class.java)) {
@Suppress("UNCHECKED_CAST")
return SiteViewModel(apiService, authManager) as T
}
throw IllegalArgumentException("Unknown ViewModel class")
}
}
