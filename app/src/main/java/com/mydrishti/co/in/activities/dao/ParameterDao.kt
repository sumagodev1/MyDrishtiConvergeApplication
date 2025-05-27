package com.mydrishti.co.`in`.activities.dao

import androidx.room.*
import com.mydrishti.co.`in`.activities.ChartParametersActivity.ParameterInfo
import com.mydrishti.co.`in`.activities.models.ParameterEntity
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.Device

/**
 * Data Access Object for parameter-related database operations
 */
@Dao
interface ParameterDao {
    /**
     * Get all parameters for a specific device
     * @param deviceId The ID of the device to get parameters for
     * @return A list of parameters for the device
     */
    @Query("SELECT * FROM parameters WHERE deviceId = :deviceId")
    suspend fun getParametersForDevice(deviceId: Int): List<ParameterEntity>

    /**
     * Get all chart configurations for a specific site
     * @param siteId The ID of the site to get chart configs for
     * @return A list of chart configurations for the site
     */
    @Query("SELECT * FROM charts WHERE deviceId = :siteId")
    suspend fun getChartConfigsForSite(siteId: String): List<ChartConfig>

    /**
     * Insert parameters for a device
     * @param parameters The parameters to insert
     * @param device The device these parameters belong to
     */
    @Transaction
    suspend fun insertParameters(parameters: List<ParameterInfo>, device: Device) {
        // Delete existing parameters for this device
        deleteParametersForDevice(device.iotDeviceMapId)

        // Insert new parameters
        val parameterEntities = parameters.map { param ->
            ParameterEntity(
                parameterId = param.id.toInt(),
                parameterName = param.name,
                parameterDisplayName = param.displayName,
                uomDisplayName = param.uomDisplayName,
                deviceId = device.iotDeviceMapId
            )
        }
        insertParameterEntities(parameterEntities)
    }

    /**
     * Delete all parameters for a specific device
     * @param deviceId The ID of the device to delete parameters for
     */
    @Query("DELETE FROM parameters WHERE deviceId = :deviceId")
    suspend fun deleteParametersForDevice(deviceId: Int)

    /**
     * Insert parameter entities
     * @param parameters The parameter entities to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameterEntities(parameters: List<ParameterEntity>)
}