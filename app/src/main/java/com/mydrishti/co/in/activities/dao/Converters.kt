package com.mydrishti.co.`in`.activities.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mydrishti.co.`in`.activities.models.ChartType
import java.util.*

/**
 * Type converters for Room database
 */
class Converters {
    private val gson = Gson()
    
    /**
     * Convert Map<String, String> to JSON string for storage
     */
    @TypeConverter
    fun fromMapToString(map: Map<String, String>?): String {
        if (map == null || map.isEmpty()) {
            return "{}"
        }
        
        val jsonObject = JsonObject()
        for ((key, value) in map) {
            jsonObject.addProperty(key, value)
        }
        
        return jsonObject.toString()
    }
    
    /**
     * Convert JSON string to Map<String, String> for retrieval
     */
    @TypeConverter
    fun fromStringToMap(value: String?): Map<String, String> {
        if (value == null || value.isEmpty() || value == "{}") {
            return hashMapOf()
        }
        
        return try {
            val result = HashMap<String, String>()
            val jsonObject = JsonParser.parseString(value).asJsonObject
            
            for (entry in jsonObject.entrySet()) {
                val key = entry.key
                val element = entry.value
                
                if (!element.isJsonNull) {
                    result[key] = element.asString
                }
            }
            
            result
        } catch (e: Exception) {
            hashMapOf()
        }
    }
    
    /**
     * Convert ChartType to string for storage
     */
    @TypeConverter
    fun fromChartType(chartType: ChartType?): String {
        return chartType?.name ?: ChartType.BAR_DAILY.name
    }
    
    /**
     * Convert string to ChartType for retrieval
     */
    @TypeConverter
    fun toChartType(value: String?): ChartType {
        return if (value == null || value.isEmpty()) {
            ChartType.BAR_DAILY
        } else {
            try {
                ChartType.valueOf(value)
            } catch (e: Exception) {
                ChartType.BAR_DAILY
            }
        }
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}