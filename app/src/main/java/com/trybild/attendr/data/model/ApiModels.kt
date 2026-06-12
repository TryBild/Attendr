package com.trybild.attendr.data.model

data class AdminLoginRequest(val email: String, val password: String)
data class AdminCompany(
    val id: String, val name: String, val teamId: String,
    val plan: String, val city: String?, val state: String?,
    val setupComplete: Boolean = false
)
data class AdminLoginResponse(val ok: Boolean, val token: String?, val company: AdminCompany?, val error: String?)

data class AdminRegisterRequest(
    val orgName: String, val adminName: String, val email: String,
    val phone: String, val city: String, val orgSize: String, val password: String
)
data class AdminRegisterResponse(val ok: Boolean, val orgId: String?, val token: String?, val error: String?)

data class AdminSetupRequest(
    val industry: String, val workDays: List<String>,
    val workStartTime: String, val workEndTime: String,
    val timezone: String, val referralSource: String
)
data class AdminSetupResponse(val ok: Boolean, val success: Boolean?, val error: String?)

data class AdminProfileResponse(
    val ok: Boolean, val setupComplete: Boolean,
    val orgId: String?, val orgName: String?, val adminName: String?,
    val error: String?
)

data class OtpRequestBody(val fullName: String, val mobile: String, val teamId: String)
data class OtpVerifyBody(val mobile: String, val teamId: String, val otp: String)
data class OtpResponse(val ok: Boolean, val message: String?)

// Matches actual backend: POST /auth/otp/verify → { ok, token, employee: { ... } }
data class EmployeeCompany(val name: String, val teamId: String)
data class EmployeeProfile(
    val id: String,
    val fullName: String,
    val mobile: String,
    val employeeCode: String?,
    val designation: String?,
    val department: String?,
    val joinedAt: String?,
    val company: EmployeeCompany?
)
data class AuthResponse(val ok: Boolean, val token: String?, val employee: EmployeeProfile?, val error: String?)
data class MarkAttendanceBody(val type: String, val lat: Double?, val lng: Double?, val mock: Boolean = false)
data class AttendanceLog(val type: String, val at: String, val mockDetected: Boolean)
data class TodayLogsResponse(val ok: Boolean, val logs: List<AttendanceLog>?)
data class AttendanceResponse(val ok: Boolean, val at: String?, val flagged: Boolean?, val error: String?)
