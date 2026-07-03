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
    suspend fun verifyOtp(@Body body: OtpVerifyBody): Response<OtpVerifyResponse>

    @POST("auth/employee/set-password")
    suspend fun setPassword(@Body body: SetPasswordRequest): Response<AuthResponse>

    @POST("auth/employee/login")
    suspend fun employeeLogin(@Body body: EmployeeLoginRequest): Response<AuthResponse>

    @POST("attendance/mark")
    suspend fun markAttendance(
        @Header("Authorization") token: String,
        @Body body: MarkAttendanceBody
    ): Response<AttendanceResponse>

    @GET("attendance/today")
    suspend fun getTodayLogs(
        @Header("Authorization") token: String
    ): Response<TodayAttendanceResponse>

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

    @GET("attendance/my")
    suspend fun getMyAttendance(
        @Header("Authorization") token: String,
        @Query("month") month: String
    ): Response<MyAttendanceResponse>

    @GET("attendance/geofences")
    suspend fun getGeofences(
        @Header("Authorization") token: String
    ): Response<GeofencesResponse>

    @GET("reports/register/month.csv")
    @Streaming
    suspend fun getMusterRollCsv(
        @Header("Authorization") token: String,
        @Query("month") month: String
    ): Response<okhttp3.ResponseBody>

    @POST("admin/employees/{id}/reset-device")
    suspend fun resetDevice(
        @Header("Authorization") token: String,
        @Path("id") employeeId: String
    ): Response<GenericResponse>

    @GET("admin/geofences")
    suspend fun getAdminGeofences(
        @Header("Authorization") token: String
    ): Response<GeofencesResponse>

    @POST("admin/geofences")
    suspend fun createGeofence(
        @Header("Authorization") token: String,
        @Body body: GeofenceCreateRequest
    ): Response<GeofenceSingleResponse>

    @PUT("admin/geofences/{id}")
    suspend fun updateGeofence(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: GeofenceCreateRequest
    ): Response<GeofenceSingleResponse>

    @DELETE("admin/geofences/{id}")
    suspend fun deleteGeofence(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericResponse>

    @GET("billing/status")
    suspend fun getBillingStatus(
        @Header("Authorization") token: String
    ): Response<BillingStatusResponse>

    @POST("billing/create-subscription")
    suspend fun createSubscription(
        @Header("Authorization") token: String
    ): Response<CreateSubscriptionResponse>

    @POST("billing/cancel")
    suspend fun cancelSubscription(
        @Header("Authorization") token: String
    ): Response<GenericResponse>
}
