package com.trybild.attendr.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.api.RetrofitClient
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.OtpRequestBody
import com.trybild.attendr.data.model.OtpVerifyBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class OtpSent(val mobile: String) : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = TokenDataStore(app)
    private val api = RetrofitClient.api

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun requestOtp(fullName: String, mobile: String, teamId: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val res = api.requestOtp(OtpRequestBody(fullName, mobile, teamId))
                _state.value = if (res.isSuccessful && res.body()?.ok == true)
                    AuthState.OtpSent(mobile)
                else AuthState.Error("Could not send OTP. Check your details and try again.")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun verifyOtp(mobile: String, otp: String, teamId: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val res = api.verifyOtp(OtpVerifyBody(mobile, teamId, otp))
                if (res.isSuccessful && res.body()?.ok == true) {
                    res.body()?.token?.let { dataStore.saveToken(it) }
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
