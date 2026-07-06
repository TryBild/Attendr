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

    suspend fun requestEmployeeOtp(
        fullName: String?, mobile: String, teamId: String, purpose: String = "register"
    ): Result<OtpResponse> {
        return try {
            val res = api.requestOtp(OtpRequestBody(fullName, mobile, teamId, purpose))
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                // Error bodies come back as { ok:false, error } in errorBody()
                val msg = runCatching {
                    Gson().fromJson(res.errorBody()?.string(), GenericResponse::class.java)?.error
                }.getOrNull() ?: res.body()?.message ?: "Could not send OTP. Check your details and try again."
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    // Verify returns a short-lived pendingToken; session is only created by setEmployeePassword.
    suspend fun verifyEmployeeOtp(mobile: String, teamId: String, otp: String): Result<OtpVerifyResponse> {
        return try {
            val res = api.verifyOtp(OtpVerifyBody(mobile, teamId, otp))
            if (res.isSuccessful && res.body()?.ok == true && res.body()?.pendingToken != null) {
                Result.success(res.body()!!)
            } else {
                val msg = runCatching {
                    Gson().fromJson(res.errorBody()?.string(), OtpVerifyResponse::class.java)?.error
                }.getOrNull() ?: res.body()?.error ?: "Invalid OTP. Please try again."
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun setEmployeePassword(
        pendingToken: String, password: String, confirmPassword: String, deviceId: String? = null
    ): Result<AuthResponse> {
        return try {
            val res = api.setPassword(SetPasswordRequest(pendingToken, password, confirmPassword, deviceId))
            if (res.isSuccessful && res.body()?.ok == true) {
                val body = res.body()!!
                body.token?.let { dataStore.saveToken(it) }
                dataStore.saveUserKind("employee")
                body.employee?.fullName?.let { dataStore.saveEmployeeName(it) }
                body.employee?.company?.name?.let { dataStore.saveCompanyName(it) }
                Result.success(body)
            } else {
                val msg = runCatching {
                    Gson().fromJson(res.errorBody()?.string(), AuthResponse::class.java)?.error
                }.getOrNull() ?: res.body()?.error ?: "Could not set password. Please try again."
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

    suspend fun notifyAdminGeofence(): Result<GenericResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.notifyAdminGeofence("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not notify your admin"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun downloadMusterRollCsv(month: String): Result<ByteArray> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.getMusterRollCsv("Bearer $token", month)
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!.bytes())
            } else {
                Result.failure(Exception("Could not download muster roll"))
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

    suspend fun getAdminGeofences(): Result<GeofencesResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.getAdminGeofences("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not fetch geofences"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun createGeofence(body: GeofenceCreateRequest): Result<GeofenceSingleResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.createGeofence("Bearer $token", body)
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not create geofence"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun updateGeofence(id: String, body: GeofenceCreateRequest): Result<GeofenceSingleResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.updateGeofence("Bearer $token", id, body)
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not update geofence"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun deleteGeofence(id: String): Result<GenericResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.deleteGeofence("Bearer $token", id)
            if (res.isSuccessful) {
                Result.success(GenericResponse(ok = true, message = "Deleted", error = null))
            } else {
                Result.failure(Exception("Could not delete geofence"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun getBillingStatus(): Result<BillingStatusResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.getBillingStatus("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not fetch billing status"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun createSubscription(): Result<CreateSubscriptionResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.createSubscription("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not create subscription"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error"))
        }
    }

    suspend fun cancelSubscription(): Result<GenericResponse> {
        return try {
            val token = dataStore.token.firstOrNull()
                ?: return Result.failure(Exception("Not logged in"))
            val res = api.cancelSubscription("Bearer $token")
            if (res.isSuccessful && res.body()?.ok == true) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.body()?.error ?: "Could not cancel subscription"))
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
