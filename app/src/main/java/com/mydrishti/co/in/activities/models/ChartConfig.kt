package com.mydrishti.co.`in`.activities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mydrishti.co.`in`.activities.database.converters.DateRangeConverter
import com.mydrishti.co.`in`.activities.database.converters.IntListConverter
import com.mydrishti.co.`in`.activities.models.DateRange
import java.util.*

/**
 * Represents a chart configuration in the app
 */
@Entity(tableName = "charts")
@TypeConverters(IntListConverter::class, DateRangeConverter::class)
data class ChartConfig(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val deviceId: String,
    val deviceName: String,
    val chartType: ChartType,
    
    @TypeConverters(IntListConverter::class)
    val parameterIds: List<Int> = listOf(),
    
    val position: Int = 0,
    val lastUpdated: Long = 0,
    
    @TypeConverters(DateRangeConverter::class)
    val customDateRange: DateRange? = null
) 