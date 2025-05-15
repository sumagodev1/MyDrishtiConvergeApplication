package com.mydrishti.co.`in`.activities.models

import com.mydrishti.co.`in`.activities.models.User

data class LoginResponseModel(
    val accessToken: String,
    val statusText: String,
    val user: User
)