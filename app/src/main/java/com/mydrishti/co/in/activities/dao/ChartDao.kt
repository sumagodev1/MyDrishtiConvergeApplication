package com.mydrishti.co.`in`.activities.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mydrishti.co.`in`.activities.models.ChartConfig

@Dao
interface ChartDao {
    @Query("SELECT * FROM charts ORDER BY position ASC")
    fun getAllChartConfigs(): LiveData<List<ChartConfig>>

    @Query("SELECT * FROM charts ORDER BY position ASC")
    suspend fun getAllChartConfigsSync(): List<ChartConfig>

    @Query("SELECT * FROM charts WHERE id = :id")
    suspend fun getChartConfigById(id: String): ChartConfig?

    @Query("SELECT * FROM charts WHERE deviceId = :siteId ORDER BY position ASC")
    fun getChartConfigsForDevice(siteId: Long): LiveData<List<ChartConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChart(chartConfig: ChartConfig)

    @Update
    suspend fun updateChart(chartConfig: ChartConfig): Int

    @Query("UPDATE charts SET title = :title, deviceId = :deviceId, deviceName = :deviceName WHERE id = :id")
    suspend fun updateChartWithQuery(id: String, title: String, deviceId: String, deviceName: String): Int

    @Delete
    suspend fun deleteChart(chartConfig: ChartConfig)

    @Query("UPDATE charts SET position = :position WHERE id = :id")
    suspend fun updateChartPosition(id: String, position: Int)

    @Query("UPDATE charts SET lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateChartLastUpdated(id: String, timestamp: Long)

    @Query("DELETE FROM charts")
    suspend fun deleteAllCharts()
}