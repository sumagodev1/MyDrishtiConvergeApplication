package com.mydrishti.co.`in`.activities.models

import androidx.room.TypeConverters
import com.mydrishti.co.`in`.activities.database.converters.DateRangeConverter
import com.google.gson.annotations.SerializedName

/**
 * Date range model for API requests and chart configuration
 */
data class DateRange(
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String
) 