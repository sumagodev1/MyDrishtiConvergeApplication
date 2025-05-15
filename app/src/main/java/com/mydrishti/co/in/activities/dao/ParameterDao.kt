package com.mydrishti.co.`in`.activities.dao

import androidx.room.*
import com.mydrishti.co.`in`.activities.ChartParametersActivity.ParameterInfo
import com.mydrishti.co.`in`.activities.models.ParameterEntity

/**
 * Data Access Object for parameter-related database operations
 */
@Dao
interface ParameterDao {
    /**
     * Get all parameters for a specific site
     * @param siteId The ID of the site to get parameters for
     * @return A list of parameters for the site
     */
    @Query("SELECT * FROM chart_configs WHERE siteId = :siteId")
    suspend fun getParametersForSite(siteId: Long): List<ParameterEntity>
    
    /**
     * Insert parameters for a site
     * @param parameters The parameters to insert
     * @param siteId The ID of the site these parameters belong to
     */
    @Transaction
    suspend fun insertParameters(parameters: List<ParameterInfo>, siteId: Long) {
        // Delete existing parameters for this site
        deleteParametersForSite(siteId)
        
        // Insert new parameters
        val parameterEntities = parameters.map { param ->
            ParameterEntity(
                parameterId = param.id,
                parameterName = param.name,
                parameterDisplayName = param.displayName,
                uomDisplayName = param.uomDisplayName,
                siteId = siteId
            )
        }
        insertParameterEntities(parameterEntities)
    }
    
    /**
     * Delete all parameters for a specific site
     * @param siteId The ID of the site to delete parameters for
     */
    @Query("DELETE FROM chart_configs WHERE siteId = :siteId")
    suspend fun deleteParametersForSite(siteId: Long)
    
    /**
     * Insert parameter entities
     * @param parameters The parameter entities to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameterEntities(parameters: List<ParameterEntity>)
}