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
    val workDays: List<String>,
    val workStartTime: String, val workEndTime: String,
    val industry: String? = null, val timezone: String? = null,
    val referralSource: String? = null, val adminName: String? = null
)
data class AdminSetupResponse(val ok: Boolean, val success: Boolean?, val error: String?)

data class AdminProfileResponse(
    val ok: Boolean, val setupComplete: Boolean,
    val orgId: String?, val orgName: String?, val adminName: String?,
    val phone: String?,
    val workDays: List<String>?, val workStartTime: String?, val workEndTime: String?,
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

// Admin dashboard
data class DashboardTodayStats(
    val date: String,
    val totalEmployees: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val attendancePercent: Int
)
data class DashboardMonthStats(val avgAttendance: Int, val totalWorkingDays: Int)
data class RecentActivityItem(
    val employeeName: String,
    val department: String?,
    val action: String,
    val status: String?,
    val time: String?
)
data class AdminDashboardResponse(
    val ok: Boolean,
    val today: DashboardTodayStats?,
    val thisMonth: DashboardMonthStats?,
    val recentActivity: List<RecentActivityItem>?,
    val error: String?
)

// Admin employees list (GET /api/admin/employees)
data class AdminEmployeeDept(val name: String)
data class AdminEmployeeItem(
    val id: String,
    val fullName: String,
    val mobile: String,
    val employeeCode: String?,
    val designation: String?,
    val department: AdminEmployeeDept?,
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val joinedAt: String?
)
data class AdminEmployeesResponse(
    val ok: Boolean,
    val employees: List<AdminEmployeeItem>?,
    val error: String?
)

// Day register (GET /api/admin/attendance/day)
data class DayRegisterRow(
    val employeeId: String,
    val employeeCode: String,
    val fullName: String,
    val department: String,
    val status: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val workingHours: Double?,
    val late: Boolean
)
data class DayRegisterResponse(
    val ok: Boolean,
    val date: String?,
    val rows: List<DayRegisterRow>?,
    val present: Int = 0,
    val total: Int = 0,
    val error: String?
)
