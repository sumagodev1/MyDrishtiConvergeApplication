package com.mydrishti.co.`in`.activities.database.converters

import androidx.room.TypeConverter
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mydrishti.co.`in`.activities.models.DateRange
import org.json.JSONObject

/**
 * Converter for DateRange to store in database
 */
class DateRangeConverter {
    
    @TypeConverter
    fun fromString(value: String?): DateRange? {
        if (value == null || value.isEmpty()) {
            return null
        }
        
        return try {
            val jsonObject = JsonParser.parseString(value).asJsonObject
            
            val startDate = jsonObject.get("startDate").asString
            val endDate = jsonObject.get("endDate").asString
            
            DateRange(startDate = startDate, endDate = endDate)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromDateRange(dateRange: DateRange?): String? {
        if (dateRange == null) {
            return null
        }
        
        return try {
            val jsonObject = JsonObject()
            jsonObject.addProperty("startDate", dateRange.startDate)
            jsonObject.addProperty("endDate", dateRange.endDate)
            
            jsonObject.toString()
        } catch (e: Exception) {
            null
        }
    }
} 