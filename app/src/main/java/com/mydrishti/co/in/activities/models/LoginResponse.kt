package com.mydrishti.co.`in`.activities.models

data class LoginResponse(
    val success: Boolean,
    val accessToken: String,
    val userDetails: UserDetails
)
