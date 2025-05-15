package com.mydrishti.co.`in`.activities.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mydrishti.co.`in`.activities.models.ChartType

/**
 * Type converters for Room database
 */
class Converters {
    private val gson = Gson()
    
    /**
     * Convert Map<String, String> to JSON string for storage
     */
    @TypeConverter
    fun fromMapToString(map: Map<String, String>): String {
        return gson.toJson(map)
    }
    
    /**
     * Convert JSON string to Map<String, String> for retrieval
     */
    @TypeConverter
    fun fromStringToMap(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }
    
    /**
     * Convert ChartType to string for storage
     */
    @TypeConverter
    fun fromChartType(chartType: ChartType): String {
        return chartType.name
    }
    
    /**
     * Convert string to ChartType for retrieval
     */
    @TypeConverter
    fun toChartType(value: String): ChartType {
        return ChartType.valueOf(value)
    }
}