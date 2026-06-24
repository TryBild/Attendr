package com.trybild.attendr.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import com.trybild.attendr.utils.DeviceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EmployeeLoginState {
    object Idle : EmployeeLoginState()
    object Loading : EmployeeLoginState()
    object Success : EmployeeLoginState()
    data class Error(val message: String) : EmployeeLoginState()
}

class EmployeeLoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val deviceId = DeviceUtils.getDeviceId(app)

    private val _state = MutableStateFlow<EmployeeLoginState>(EmployeeLoginState.Idle)
    val state: StateFlow<EmployeeLoginState> = _state

    fun login(mobile: String, teamId: String, password: String) {
        viewModelScope.launch {
            _state.value = EmployeeLoginState.Loading
            val result = repo.employeeLogin(mobile, teamId, password, deviceId)
            _state.value = if (result.isSuccess)
                EmployeeLoginState.Success
            else
                EmployeeLoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
        }
    }

    fun resetState() { _state.value = EmployeeLoginState.Idle }
}
