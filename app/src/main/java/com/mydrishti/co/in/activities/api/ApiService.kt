package com.mydrishti.co.`in`.activities.api

import com.mydrishti.co.`in`.activities.models.DeviceParameterRequest
import com.mydrishti.co.`in`.activities.models.DeviceParameterResponse
import com.mydrishti.co.`in`.activities.models.DeviceResponseModel
import com.mydrishti.co.`in`.activities.models.LoginRequest
import com.mydrishti.co.`in`.activities.models.LoginResponse
import com.mydrishti.co.`in`.activities.models.LoginResponseModel
import com.mydrishti.co.`in`.activities.models.ParameterDto
import com.mydrishti.co.`in`.activities.models.Site
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API service interface for network requests
 * Defines all the endpoints used in the application
 */
interface ApiService {
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponseModel>

    /**
     * Get parameters available for a specific site
     * @param siteId ID of the site to get parameters for
     * @return List of parameter DTOs
     */
    @GET("api/sites/{siteId}/parameters")
    suspend fun getSiteParameters(@Path("siteId") siteId: Long): List<ParameterDto>

    /**
     * Get daily bar chart data for a site
     * @param siteId ID of the site to get data for
     */
    @GET("api/charts/daily")
    suspend fun getDailyBarChartData(@Query("siteId") siteId: Long): Any

    /**
     * Get hourly bar chart data for a site
     * @param siteId ID of the site to get data for
     */
    @GET("api/charts/hourly")
    suspend fun getHourlyBarChartData(@Query("siteId") siteId: Long): Any

    /**
     * Get gauge chart data for a site
     * @param siteId ID of the site to get data for
     */
    @GET("api/charts/gauge")
    suspend fun getGaugeChartData(@Query("siteId") siteId: Long): Any

    /**
     * Get metric data for a site
     * @param siteId ID of the site to get data for
     */
    @GET("api/charts/metric")
    suspend fun getMetricData(@Query("siteId") siteId: Long): Any

    @POST("user/device-parameter") // Likely the endpoint, based on naming pattern
    fun getDeviceParameters(@Body request: DeviceParameterRequest): DeviceParameterResponse

    @GET("user/lalitvijay@mgumst.org/device")
    suspend fun getSites(): DeviceResponseModel
}