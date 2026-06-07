package com.trybild.attendr.data.api

import com.trybild.attendr.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequestBody): Response<OtpResponse>

    @POST("auth/admin/verify")
    suspend fun verifyAdmin(@Body body: AdminVerifyBody): Response<AuthResponse>

    @POST("auth/employee/verify")
    suspend fun verifyEmployee(@Body body: EmployeeVerifyBody): Response<AuthResponse>

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
