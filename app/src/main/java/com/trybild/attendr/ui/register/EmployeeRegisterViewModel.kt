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

class EmployeeRegisterViewModel(app: Application) : AndroidViewModel(app) {
    private val api = RetrofitClient.api
    private val dataStore = TokenDataStore(app)

    private val _step = MutableStateFlow(1)
    val step: StateFlow<Int> = _step

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName

    private val _mobile = MutableStateFlow("")
    val mobile: StateFlow<String> = _mobile

    private val _teamId = MutableStateFlow("")
    val teamId: StateFlow<String> = _teamId

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    fun onFullNameChange(value: String) { _fullName.value = value }
    fun onMobileChange(value: String) {
        _mobile.value = value.filter { it.isDigit() }.take(10)
    }
    fun onTeamIdChange(value: String) { _teamId.value = value.uppercase() }
    fun onOtpChange(value: String) { _otp.value = value }
    fun clearError() { _error.value = "" }

    fun goBack() {
        if (_step.value > 1) _step.value -= 1
    }

    fun sendOtp(onSent: () -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = ""
            try {
                val res = api.requestOtp(
                    OtpRequestBody(_fullName.value.trim(), _mobile.value, _teamId.value.trim())
                )
                if (res.isSuccessful && res.body()?.ok == true) {
                    _otp.value = ""
                    _step.value = 2
                    onSent()
                } else {
                    _error.value = res.body()?.message ?: "Could not send OTP. Please try again."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error. Please try again."
            } finally {
                _loading.value = false
            }
        }
    }

    fun verifyOtp(onVerified: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = ""
            try {
                val res = api.verifyOtp(
                    OtpVerifyBody(_mobile.value, _teamId.value.trim(), _otp.value)
                )
                val body = res.body()
                if (res.isSuccessful && body?.ok == true && body.token != null) {
                    dataStore.saveToken(body.token)
                    onVerified()
                } else {
                    _error.value = body?.error ?: "Invalid OTP. Please try again."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error. Please try again."
            } finally {
                _loading.value = false
            }
        }
    }
}
