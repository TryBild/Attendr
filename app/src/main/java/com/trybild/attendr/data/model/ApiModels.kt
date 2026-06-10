package com.trybild.attendr.data.model

data class OtpRequestBody(val fullName: String, val mobile: String, val teamId: String)
data class OtpVerifyBody(val mobile: String, val teamId: String, val otp: String)
data class OtpResponse(val ok: Boolean, val message: String?)
data class AuthResponse(val ok: Boolean, val token: String?, val name: String?, val companyId: String?, val error: String?)
data class MarkAttendanceBody(val type: String, val lat: Double?, val lng: Double?, val mock: Boolean = false)
data class AttendanceLog(val type: String, val at: String, val mockDetected: Boolean)
data class TodayLogsResponse(val ok: Boolean, val logs: List<AttendanceLog>?)
data class AttendanceResponse(val ok: Boolean, val at: String?, val flagged: Boolean?, val error: String?)
