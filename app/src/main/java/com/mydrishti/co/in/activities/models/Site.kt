package com.mydrishti.co.`in`.activities.models

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
data class DeviceParameter(
    val device: Site,
    val parameters: List<Parameter>
)

/**
 * Model class for API response containing device parameters
 */
data class DeviceParameterResponse(
    val deviceParameter: List<DeviceParameterWrapper>
)

/**
 * Wrapper class for device and parameters in API response
 */
data class DeviceParameterWrapper(
    val deviceEntity: DeviceEntity,
    val parameterEntityList: List<ParameterEntity>
)

/**
 * Device entity as returned by API
 */
data class DeviceEntity(
    val iotDeviceMapId: Long,
    val deviceName: String,
    val deviceDisplayName: String,
    val deviceActivationTimestamp: String,
    val protocol: String
)

/**
 * Parameter entity as returned by API
 */
data class ParameterEntity(
    val parameterId: Long,
    val parameterName: String,
    val parameterDisplayName: String,
    val uomDisplayName: String,
    val siteId: Long
)