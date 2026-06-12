package com.trybild.attendr.data.repository

import android.content.Context
import com.trybild.attendr.data.api.RetrofitClient
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.AdminLoginRequest
import com.trybild.attendr.data.model.AdminLoginResponse
import com.trybild.attendr.data.model.AuthResponse
import com.trybild.attendr.data.model.OtpRequestBody
import com.trybild.attendr.data.model.OtpResponse
import com.trybild.attendr.data.model.OtpVerifyBody

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
}
