package com.trybild.attendr.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.api.RetrofitClient
import com.trybild.attendr.data.model.OtpRequestBody
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
    private val api = RetrofitClient.api

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state

    fun requestOtp(fullName: String, mobile: String, teamId: String) {
        viewModelScope.launch {
            _state.value = RegisterState.Loading
            try {
                val res = api.requestOtp(OtpRequestBody(fullName, mobile, teamId))
                _state.value = if (res.isSuccessful && res.body()?.ok == true)
                    RegisterState.OtpSent(mobile)
                else RegisterState.Error("Could not send OTP. Check the number and try again.")
            } catch (e: Exception) {
                _state.value = RegisterState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetState() { _state.value = RegisterState.Idle }
}
