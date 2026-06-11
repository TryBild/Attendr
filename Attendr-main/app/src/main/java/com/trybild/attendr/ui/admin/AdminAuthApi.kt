package com.trybild.attendr.ui.admin

import com.trybild.attendr.utils.Constants
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Admin auth lives here (not in data/) because the existing data layer
// files are frozen for this rebuild.

data class AdminLoginBody(val email: String, val password: String)

data class AdminRegisterBody(
    val companyName: String,
    val adminEmail: String,
    val password: String,
    val city: String? = null,
    val state: String? = null
)

data class AdminAuthResponse(
    val ok: Boolean,
    val token: String?,
    val teamId: String?,
    val companyName: String?,
    val message: String?,
    val error: String?
)

interface AdminAuthApi {
    @POST("auth/admin/login")
    suspend fun login(@Body body: AdminLoginBody): Response<AdminAuthResponse>

    @POST("auth/admin/register")
    suspend fun register(@Body body: AdminRegisterBody): Response<AdminAuthResponse>
}

object AdminAuthClient {
    val api: AdminAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdminAuthApi::class.java)
    }
}
