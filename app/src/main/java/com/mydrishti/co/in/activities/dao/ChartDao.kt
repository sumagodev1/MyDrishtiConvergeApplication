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
    @Query("SELECT * FROM chart_configs ORDER BY position ASC")
    fun getAllChartConfigs(): LiveData<List<ChartConfig>>

    @Query("SELECT * FROM chart_configs WHERE id = :id")
    suspend fun getChartConfigById(id: String): ChartConfig?

    @Query("SELECT * FROM chart_configs WHERE deviceId = :siteId ORDER BY position ASC")
    fun getChartConfigsForDevice(siteId: Long): LiveData<List<ChartConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChart(chartConfig: ChartConfig)

    @Update
    suspend fun updateChart(chartConfig: ChartConfig)

    @Delete
    suspend fun deleteChart(chartConfig: ChartConfig)

    @Query("UPDATE chart_configs SET position = :position WHERE id = :id")
    suspend fun updateChartPosition(id: String, position: Int)

    @Query("DELETE FROM chart_configs")
    suspend fun deleteAllCharts()
}