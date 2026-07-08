package com.trybild.attendr.data.model

data class AdminLoginRequest(val email: String, val password: String)
data class AdminCompany(
    val id: String, val name: String, val teamId: String,
    val plan: String, val city: String?, val state: String?,
    val setupComplete: Boolean = false, val photoUrl: String? = null
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
    val adminEmail: String? = null,
    val phone: String?,
    val workDays: List<String>?, val workStartTime: String?, val workEndTime: String?,
    val photoUrl: String? = null,
    val error: String?
)

// purpose: "register" (default) or "forgot"; fullName only required for register
data class OtpRequestBody(val fullName: String?, val mobile: String, val teamId: String, val purpose: String = "register")
data class OtpVerifyBody(val mobile: String, val teamId: String, val otp: String)
data class OtpResponse(val ok: Boolean, val message: String?)

// Backend: POST /auth/otp/verify → { ok, pendingToken, fullName } (pendingToken expires 15m)
data class OtpVerifyResponse(val ok: Boolean, val pendingToken: String?, val fullName: String?, val error: String?)

// Backend: POST /auth/employee/set-password → { ok, token, employee } (same shape as AuthResponse)
// Also reused for admin forgot-password reset, where the response has `company` instead of `employee`.
data class SetPasswordRequest(
    val pendingToken: String, val password: String,
    val confirmPassword: String, val deviceId: String? = null
)

// Backend: POST /auth/employee/login → { ok, token, employee: { ... } } (same shape as AuthResponse)
data class EmployeeLoginRequest(val mobile: String, val teamId: String, val password: String, val deviceId: String? = null)

data class EmployeeCompany(val name: String, val teamId: String)
data class EmployeeProfile(
    val id: String,
    val fullName: String,
    val mobile: String,
    val employeeCode: String?,
    val designation: String?,
    val department: String?,
    val joinedAt: String?,
    val photoUrl: String? = null,
    val company: EmployeeCompany?
)
data class AuthResponse(
    val ok: Boolean, val token: String?,
    val employee: EmployeeProfile?, val company: AdminCompany? = null,
    val error: String?
)

// Backend: POST /auth/profile/photo (multipart, field name "photo") → { ok, photoUrl }
data class PhotoUploadResponse(val ok: Boolean, val photoUrl: String?, val error: String?)
// Body must match backend: const { latitude, longitude, action } = req.body
// action values must be "checkin" or "checkout" (not "in"/"out")
data class MarkAttendanceBody(val action: String, val latitude: Double?, val longitude: Double?, val mockDetected: Boolean = false, val deviceId: String? = null)

// Backend success: { ok, action, time, status, geofence, distance }
// Backend error may include a machine-readable `code` (e.g. "GEOFENCE_NOT_SET") + `message`.
data class AttendanceResponse(val ok: Boolean, val action: String?, val time: String?, val error: String?, val code: String? = null, val message: String? = null)

// Backend GET /attendance/today: single record { ok, status, checkInTime, checkOutTime, ... }
data class TodayAttendanceResponse(
    val ok: Boolean,
    val status: String?,
    val checkInTime: String?,
    val checkOutTime: String?,
    val workingHours: Double?,
    val mockDetected: Boolean = false
)

// Constructed locally from TodayAttendanceResponse; type is "in" or "out"
data class AttendanceLog(val type: String, val at: String)

// Admin dashboard
data class DashboardTodayStats(
    val date: String,
    val totalEmployees: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val attendancePercent: Int,
    val mockFlaggedCount: Int = 0
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
    val joinedAt: String?,
    val deviceBound: Boolean = false
)
data class AdminEmployeesResponse(
    val ok: Boolean,
    val employees: List<AdminEmployeeItem>?,
    val error: String?
)
data class GenericResponse(val ok: Boolean, val message: String?, val error: String?)

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
    val late: Boolean,
    val mockDetected: Boolean = false
)
data class DayRegisterResponse(
    val ok: Boolean,
    val date: String?,
    val rows: List<DayRegisterRow>?,
    val present: Int = 0,
    val total: Int = 0,
    val error: String?
)

// Employee: GET /api/attendance/my
data class MyAttendanceRecord(
    val date: String,
    val status: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val workingHours: Double?,
    val mockDetected: Boolean = false
)
data class MyAttendanceSummary(
    val totalMarked: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val leaves: Int,
    val workingDays: Int,
    val attendancePercent: Int
)
data class MyAttendanceResponse(
    val ok: Boolean,
    val month: String?,
    val records: List<MyAttendanceRecord>?,
    val summary: MyAttendanceSummary?,
    val error: String?
)

// Geofences (shared between admin and employee)
data class GeofenceItem(
    val _id: String? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val address: String? = null
)
data class GeofencesResponse(
    val ok: Boolean,
    val geofences: List<GeofenceItem>?,
    val error: String?
)
data class GeofenceCreateRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val address: String? = null
)
data class GeofenceSingleResponse(val ok: Boolean, val geofence: GeofenceItem?, val error: String?)

// Billing
data class BillingStatusResponse(
    val ok: Boolean,
    val plan: String?,
    val status: String?,
    val trialDaysLeft: Int = 0,
    val trialEndsAt: String?,
    val renewsAt: String?,
    val razorpaySubscriptionId: String?,
    val error: String?
)
data class CreateSubscriptionResponse(
    val ok: Boolean,
    val subscriptionId: String?,
    val shortUrl: String?,
    val razorpayKeyId: String?,
    val error: String?
)
