package com.trybild.attendr.data.repository

import android.content.Context
import com.google.gson.Gson
import com.trybild.attendr.data.api.RetrofitClient
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.*
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(context: Context) {
    private val api = RetrofitClient.api
    private val dataStore = TokenDataStore(context)

    suspend fun adminLogin(email: String, password: String): Result<AdminLoginResponse> {
        return try {
            val res = api.adminLogin(AdminLoginRequest(email, password))
            if (res.isSuccessful && res.body()?.ok == true) {
                val body = res.body()!!
                body.token?.let { dataStore.saveToken(it) }
                dataStore.saveUserKind("admin")
                body.company?.name?.let { dataStore.saveCompanyName(it) }
                Result.success(body)
            } else {
                val msg = res.body()?.error
                    ?: res.errorBody()?.string()
                    ?: "Login failed"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun requestEmployeeOtp(fullName: String, mobile: String, teamId: String): Result<OtpResponse> {
        return try {
            val res = api.requestOtp(OtpRequestBody(fullName, mobile, teamId))
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                val msg = res.body()?.message ?: "Could not send OTP. Check your details and try again."
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun verifyEmployeeOtp(mobile: String, teamId: String, otp: String): Result<AuthResponse> {
        return try {
            val res = api.verifyOtp(OtpVerifyBody(mobile, teamId, otp))
            if (res.isSuccessful && res.body()?.ok == true) {
                val body = res.body()!!
                body.token?.let { dataStore.saveToken(it) }
                dataStore.saveUserKind("employee")
                body.employee?.fullName?.let { dataStore.saveEmployeeName(it) }
                body.employee?.company?.name?.let { dataStore.saveCompanyName(it) }
                Result.success(body)
            } else {
                val msg = res.body()?.error ?: "Invalid OTP. Please try again."
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun employeeLogin(mobile: String, teamId: String, password: String, deviceId: String? = null): Result<AuthResponse> {
        return try {
            val res = api.employeeLogin(EmployeeLoginRequest(mobile, teamId, password, deviceId))
            if (res.isSuccessful && res.body()?.ok == true) {
                val body = res.body()!!
                body.token?.let { dataStore.saveToken(it) }
                dataStore.saveUserKind("employee")
                body.employee?.fullName?.let { dataStore.saveEmployeeName(it) }
                body.employee?.company?.name?.let { dataStore.saveCompanyName(it) }
                Result.success(body)
            } else {
                // 401/403/404 bodies are in errorBody() as { ok:false, error }; surface the exact message.
                val msg = runCatching {
                    Gson().fromJson(res.errorBody()?.string(), AuthResponse::class.java)?.error
                }.getOrNull() ?: res.body()?.error ?: "Login failed"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun adminRegister(
        orgName: String, adminName: String, email: String,
        phone: String, city: String, orgSize: String, password: String
    ): Result<AdminRegisterResponse> {
        return try {
            val res = api.adminRegister(
                AdminRegisterRequest(orgName, adminName, email, phone, city, orgSize, password)
            )
            if (res.isSuccessful && res.body()?.ok == true) {
                val body = res.body()!!
                body.token?.let { dataStore.saveToken(it) }
                dataStore.saveUserKind("admin")
                dataStore.saveSetupComplete(false)
                body.orgId?.let { dataStore.saveOrgId(it) }
                Result.success(body)
            } else {
                val msg = res.body()?.error
                    ?: res.errorBody()?.string()
                    ?: "Registration failed"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun adminSetup(
        workDays: List<String>,
        workStartTime: String, workEndTime: String,
        industry: String? = null, timezone: String? = null,
        referralSource: String? = null, adminName: String? = null
    ): Result<AdminSetupResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.adminSetup(
                "Bearer $token",
                AdminSetupRequest(
                    workDays = workDays,
                    workStartTime = workStartTime,
                    workEndTime = workEndTime,
                    industry = industry,
                    timezone = timezone,
                    referralSource = referralSource,
                    adminName = adminName
                )
            )
            if (res.isSuccessful && res.body()?.ok == true) {
                dataStore.saveSetupComplete(true)
                Result.success(res.body()!!)
            } else {
                val msg = res.body()?.error ?: "Setup failed"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun getAdminEmployees(): Result<AdminEmployeesResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.getAdminEmployees("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not fetch employees"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun adminDashboard(): Result<AdminDashboardResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.adminDashboard("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not fetch dashboard"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun getDayRegister(date: String): Result<DayRegisterResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.getDayRegister("Bearer $token", date)
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not fetch register"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun getMyAttendance(month: String): Result<MyAttendanceResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.getMyAttendance("Bearer $token", month)
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not fetch attendance"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun getGeofences(): Result<GeofencesResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.getGeofences("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not fetch geofences"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun resetDevice(employeeId: String): Result<GenericResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.resetDevice("Bearer $token", employeeId)
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not reset device"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun adminProfile(): Result<AdminProfileResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.adminProfile("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                val body = res.body()!!
                dataStore.saveSetupComplete(body.setupComplete)
                body.orgId?.let { dataStore.saveOrgId(it) }
                Result.success(body)
            } else {
                val msg = res.body()?.error ?: "Could not fetch profile"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }
}
