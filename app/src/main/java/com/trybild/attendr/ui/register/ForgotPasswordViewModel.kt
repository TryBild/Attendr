package com.trybild.attendr.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class OtpSent(val mobile: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val state: StateFlow<ForgotPasswordState> = _state

    fun requestOtp(mobile: String, teamId: String) {
        viewModelScope.launch {
            _state.value = ForgotPasswordState.Loading
            val result = repo.requestEmployeeOtp(null, mobile, teamId, purpose = "forgot")
            _state.value = if (result.isSuccess)
                ForgotPasswordState.OtpSent(mobile)
            else
                ForgotPasswordState.Error(result.exceptionOrNull()?.message ?: "Could not send OTP")
        }
    }

    fun resetState() { _state.value = ForgotPasswordState.Idle }
}
