package com.mydrishti.co.`in`.activities.models

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("statusText")
    val statusText: String
)