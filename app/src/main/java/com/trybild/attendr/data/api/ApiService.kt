package com.trybild.attendr.data.api

import com.trybild.attendr.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/admin/login")
    suspend fun adminLogin(@Body body: AdminLoginRequest): Response<AdminLoginResponse>

    @POST("auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequestBody): Response<OtpResponse>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyBody): Response<AuthResponse>

    @POST("attendance/mark")
    suspend fun markAttendance(
        @Header("Authorization") token: String,
        @Body body: MarkAttendanceBody
    ): Response<AttendanceResponse>

    @GET("attendance/today")
    suspend fun getTodayLogs(
        @Header("Authorization") token: String
    ): Response<TodayLogsResponse>
}
