package com.mydrishti.co.`in`.activities.models

data class User(
    val activeIndicator: String,
    val companyId: Int,
    val companyName: String,
    val creationTime: String,
    val forcedPasswordChange: String,
    val password: String,
    val userEmailId: String
)