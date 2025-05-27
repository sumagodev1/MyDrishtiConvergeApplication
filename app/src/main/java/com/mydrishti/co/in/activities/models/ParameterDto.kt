package com.mydrishti.co.`in`.activities.models

/**
 * Data Transfer Object for parameters received from the API
 */
data class ParameterDto(
    val id: String,
    val name: String,
    val displayName: String,
    val uomDisplayName: String
)