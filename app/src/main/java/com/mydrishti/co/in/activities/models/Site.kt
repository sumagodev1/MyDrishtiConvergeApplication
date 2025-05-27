package com.mydrishti.co.`in`.activities.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model class representing a site
 */
data class Site(
    val id: Long,
    val name: String,
    val displayName: String,
    val location: String,
    val activationTimestamp: String,
    val protocol: String
)

/**
 * Model class representing a parameter
 */
data class Parameter(
    val id: Long,
    val name: String,
    val displayName: String,
    val uomDisplayName: String
)

/**
 * Model class representing a device parameter mapping
 */


/**
 * Wrapper class for device and parameters in API response
 */
data class DeviceParameterWrapper(
    val deviceEntity: DeviceEntity,
    val parameterEntityList: List<ParameterEntity>
)

