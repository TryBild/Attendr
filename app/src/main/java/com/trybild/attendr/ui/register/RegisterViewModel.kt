package com.trybild.attendr.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class OtpSent(val mobile: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state

    fun requestOtp(fullName: String, mobile: String, teamId: String) {
        viewModelScope.launch {
            _state.value = RegisterState.Loading
            val result = repo.requestEmployeeOtp(fullName, mobile, teamId)
            _state.value = if (result.isSuccess)
                RegisterState.OtpSent(mobile)
            else
                RegisterState.Error(result.exceptionOrNull()?.message ?: "Could not send OTP")
        }
    }

    fun resetState() { _state.value = RegisterState.Idle }
}
