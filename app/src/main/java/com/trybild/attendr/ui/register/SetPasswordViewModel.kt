package com.trybild.attendr.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import com.trybild.attendr.utils.DeviceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SetPasswordState {
    object Idle : SetPasswordState()
    object Loading : SetPasswordState()
    object Success : SetPasswordState()
    data class Error(val message: String) : SetPasswordState()
}

class SetPasswordViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val deviceId = DeviceUtils.getDeviceId(app)

    private val _state = MutableStateFlow<SetPasswordState>(SetPasswordState.Idle)
    val state: StateFlow<SetPasswordState> = _state

    fun setPassword(pendingToken: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _state.value = SetPasswordState.Loading
            val result = repo.setEmployeePassword(pendingToken, password, confirmPassword, deviceId)
            _state.value = if (result.isSuccess)
                SetPasswordState.Success
            else
                SetPasswordState.Error(result.exceptionOrNull()?.message ?: "Could not set password")
        }
    }

    fun resetState() { _state.value = SetPasswordState.Idle }
}
