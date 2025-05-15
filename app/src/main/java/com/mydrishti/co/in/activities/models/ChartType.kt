package com.mydrishti.co.`in`.activities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mydrishti.co.`in`.activities.dao.Converters
import java.util.UUID

/**
 * Chart types supported by the application
 */
enum class ChartType {
    BAR_DAILY,
    BAR_HOURLY,
    GAUGE,
    METRIC
}

/**
 * Entity representing a chart configuration
 */
@Entity(tableName = "chart_configs")
@TypeConverters(Converters::class)
data class ChartConfig(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val chartType: ChartType,
    val siteId: String,
    val siteName: String,
    val title: String,
    val parameters: Map<String, String>,
    val position: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)