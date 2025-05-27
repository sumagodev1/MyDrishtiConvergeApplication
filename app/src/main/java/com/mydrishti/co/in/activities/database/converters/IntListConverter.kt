package com.mydrishti.co.`in`.activities.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser

/**
 * Converter for List<Int> to store in database
 */
class IntListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): List<Int> {
        if (value == null || value.isEmpty()) {
            return emptyList()
        }
        
        return try {
            val jsonArray = JsonParser.parseString(value).asJsonArray
            val result = ArrayList<Int>()
            
            for (i in 0 until jsonArray.size()) {
                val element = jsonArray.get(i)
                if (!element.isJsonNull) {
                    result.add(element.asInt)
                }
            }
            
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String {
        if (list == null || list.isEmpty()) {
            return "[]"
        }
        
        val jsonArray = JsonArray()
        for (item in list) {
            jsonArray.add(item)
        }
        
        return jsonArray.toString()
    }
}