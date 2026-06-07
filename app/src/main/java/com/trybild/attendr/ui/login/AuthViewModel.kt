package com.trybild.attendr.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.api.RetrofitClient
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.AdminVerifyBody
import com.trybild.attendr.data.model.OtpRequestBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class OtpSent(val phone: String) : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = TokenDataStore(app)
    private val api = RetrofitClient.api

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun requestOtp(phone: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val res = api.requestOtp(OtpRequestBody(phone))
                _state.value = if (res.isSuccessful && res.body()?.ok == true)
                    AuthState.OtpSent(phone)
                else AuthState.Error("OTP send failed")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun verifyOtp(phone: String, code: String, companyName: String = "") {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val res = api.verifyAdmin(
                    AdminVerifyBody(phone, code, companyName.ifBlank { null })
                )
                if (res.isSuccessful && res.body()?.ok == true) {
                    res.body()?.token?.let { dataStore.saveToken(it) }
                    dataStore.saveDeviceId(UUID.randomUUID().toString())
                    _state.value = AuthState.Success
                } else {
                    _state.value = AuthState.Error(res.body()?.error ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetState() { _state.value = AuthState.Idle }
}
