package com.trybild.attendr.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SetPasswordUiState {
    object Idle : SetPasswordUiState()
    object Loading : SetPasswordUiState()
    object Success : SetPasswordUiState()
    data class Error(val message: String) : SetPasswordUiState()
}

class SetPasswordViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow<SetPasswordUiState>(SetPasswordUiState.Idle)
    val state: StateFlow<SetPasswordUiState> = _state

    fun setPassword(pendingToken: String, password: String) {
        viewModelScope.launch {
            _state.value = SetPasswordUiState.Loading
            val result = repo.setEmployeePassword(pendingToken, password)
            _state.value = if (result.isSuccess)
                SetPasswordUiState.Success
            else
                SetPasswordUiState.Error(result.exceptionOrNull()?.message ?: "Could not set password")
        }
    }

    fun resetState() { _state.value = SetPasswordUiState.Idle }
}
