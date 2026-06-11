package com.trybild.attendr.ui.register

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

sealed class OtpUiState {
    object Idle : OtpUiState()
    object Loading : OtpUiState()
    object Success : OtpUiState()
    object OtpResent : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}

class OtpViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = TokenDataStore(app)
    private val api = RetrofitClient.api

    private val _state = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val state: StateFlow<OtpUiState> = _state

    fun verifyOtp(mobile: String, otp: String, teamId: String) {
        viewModelScope.launch {
            _state.value = OtpUiState.Loading
            try {
                val res = api.verifyOtp(OtpVerifyBody(mobile, teamId, otp))
                if (res.isSuccessful && res.body()?.ok == true) {
                    res.body()?.token?.let { dataStore.saveToken(it) }
                    _state.value = OtpUiState.Success
                } else {
                    _state.value = OtpUiState.Error(res.body()?.error ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                _state.value = OtpUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resendOtp(fullName: String, mobile: String, teamId: String) {
        viewModelScope.launch {
            try {
                val res = api.requestOtp(OtpRequestBody(fullName, mobile, teamId))
                _state.value = if (res.isSuccessful && res.body()?.ok == true)
                    OtpUiState.OtpResent
                else OtpUiState.Error("Failed to resend OTP. Try again.")
            } catch (e: Exception) {
                _state.value = OtpUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetState() { _state.value = OtpUiState.Idle }
}
