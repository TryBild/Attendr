package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdminSetupState {
    object Idle : AdminSetupState()
    object Loading : AdminSetupState()
    object Success : AdminSetupState()
    data class Error(val message: String) : AdminSetupState()
}

class AdminSetupViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow<AdminSetupState>(AdminSetupState.Idle)
    val state: StateFlow<AdminSetupState> = _state

    fun submitSetup(
        industry: String,
        workDays: List<String>,
        workStartTime: String,
        workEndTime: String,
        timezone: String,
        referralSource: String
    ) {
        viewModelScope.launch {
            _state.value = AdminSetupState.Loading
            val result = repo.adminSetup(industry, workDays, workStartTime, workEndTime, timezone, referralSource)
            _state.value = if (result.isSuccess) {
                AdminSetupState.Success
            } else {
                AdminSetupState.Error(result.exceptionOrNull()?.message ?: "Setup failed")
            }
        }
    }

    fun resetState() { _state.value = AdminSetupState.Idle }
}
