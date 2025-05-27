package com.mydrishti.co.`in`.activities.api

import com.mydrishti.co.`in`.activities.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API service interface for network requests that match the actual MyDrishti API
 */
interface ApiService {
    /**
     * Login to the MyDrishti system
     * @param loginRequest Request containing userEmail and password
     * @return Response with user info and access token
     */
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponseModel>

    /**
     * Get a list of devices for the user
     * @param username User email to get devices for
     * @return Response containing a list of devices
     */
    @GET("user/{username}/device")
    suspend fun getDevices(@Path("username") username: String): DeviceResponseModel

    /**
     * Get parameters for the user
     * @param username User email to get parameters for
     * @return Response containing parameters
     */
    @GET("user/{username}/parameter")
    suspend fun getParameters(@Path("username") username: String): ParameterResponseModel

    /**
     * Get device parameters based on chart type
     * Supported types (case sensitive): "bar-chart", "hourly-bar-chart", "gauge-chart", "metric-chart"
     * @param request Request containing userEmailId and type
     * @return Response containing device parameters
     */
    @POST("user/device-parameter")
    suspend fun getDeviceParameters(@Body request: DeviceParameterRequest): DeviceParameterResponse

    /**
     * Get daily bar chart data
     * @param request Request containing dateRange and deviceDetails
     * @return Response with chart data
     */
    @POST("bar-chart")
    suspend fun getDailyBarChartData(@Body request: BarChartRequest): BarChartResponse

    /**
     * Get hourly bar chart data
     * @param request Request containing dateRange and deviceDetails
     * @return Response with chart data
     */
    @POST("hourly-bar-chart")
    suspend fun getHourlyBarChartData(@Body request: BarChartRequest): BarChartResponse

    /**
     * Get gauge chart data
     * @param request Request containing device and parameter info
     * @return Response with gauge data
     */
    @POST("gauge-chart")
    suspend fun getGaugeChartData(@Body request: GaugeChartRequest): GaugeChartResponse

    /**
     * Get metric chart data
     * @param request Request containing device and parameter info
     * @return Response with metric data
     */
    @POST("metric-chart")
    suspend fun getMetricChartData(@Body request: MetricChartRequest): MetricChartResponse
}