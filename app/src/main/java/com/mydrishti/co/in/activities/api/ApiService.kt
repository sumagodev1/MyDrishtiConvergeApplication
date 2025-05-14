package com.mydrishti.co.`in`.activities.api

import com.mydrishti.co.`in`.activities.models.LoginRequest
import com.mydrishti.co.`in`.activities.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
    
    // Add other API endpoints here
}