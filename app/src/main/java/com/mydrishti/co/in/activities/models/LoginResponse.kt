package com.mydrishti.co.`in`.activities.models

data class LoginResponse(
    val success: Boolean,
    val token: String,
    val userDetails: UserDetails
)
