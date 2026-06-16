package com.trybild.attendr.data.api

import com.trybild.attendr.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/admin/login")
    suspend fun adminLogin(@Body body: AdminLoginRequest): Response<AdminLoginResponse>

    @POST("auth/admin/register")
    suspend fun adminRegister(@Body body: AdminRegisterRequest): Response<AdminRegisterResponse>

    @PATCH("auth/admin/setup")
    suspend fun adminSetup(
        @Header("Authorization") token: String,
        @Body body: AdminSetupRequest
    ): Response<AdminSetupResponse>

    @GET("auth/admin/profile")
    suspend fun adminProfile(
        @Header("Authorization") token: String
    ): Response<AdminProfileResponse>

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

    @GET("admin/employees")
    suspend fun getAdminEmployees(
        @Header("Authorization") token: String
    ): Response<AdminEmployeesResponse>

    @GET("admin/dashboard")
    suspend fun adminDashboard(
        @Header("Authorization") token: String
    ): Response<AdminDashboardResponse>

    @GET("admin/attendance/day")
    suspend fun getDayRegister(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): Response<DayRegisterResponse>
}
