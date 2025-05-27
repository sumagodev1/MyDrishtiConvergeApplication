package com.mydrishti.co.`in`.activities.models

/**
 * Response model for parameters API
 */
data class ParameterResponseModel(
    val success: Boolean,
    val userParameterList: List<ParameterItem>
)

/**
 * Parameter item in the API response
 */
data class ParameterItem(
    val parameterId: Int,
    val parameterName: String,
    val parameterDisplayName: String,
    val uomDisplayName: String
) 