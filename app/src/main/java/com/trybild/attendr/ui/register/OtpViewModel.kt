package com.trybild.attendr.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OtpUiState {
    object Idle : OtpUiState()
    object Loading : OtpUiState()
    data class Success(val pendingToken: String) : OtpUiState()
    object OtpResent : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}

class OtpViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val state: StateFlow<OtpUiState> = _state

    fun verifyOtp(mobile: String, otp: String, teamId: String) {
        viewModelScope.launch {
            _state.value = OtpUiState.Loading
            val result = repo.verifyEmployeeOtp(mobile, teamId, otp)
            _state.value = if (result.isSuccess)
                OtpUiState.Success(result.getOrNull()?.pendingToken ?: "")
            else
                OtpUiState.Error(result.exceptionOrNull()?.message ?: "Invalid OTP")
        }
    }

    fun resendOtp(fullName: String, mobile: String, teamId: String) {
        viewModelScope.launch {
            val result = repo.requestEmployeeOtp(fullName, mobile, teamId)
            _state.value = if (result.isSuccess)
                OtpUiState.OtpResent
            else
                OtpUiState.Error(result.exceptionOrNull()?.message ?: "Failed to resend OTP")
        }
    }

    fun resetState() { _state.value = OtpUiState.Idle }
}
