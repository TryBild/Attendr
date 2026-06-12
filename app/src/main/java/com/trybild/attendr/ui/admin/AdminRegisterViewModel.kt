package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdminRegisterState {
    object Idle : AdminRegisterState()
    object Loading : AdminRegisterState()
    data class Success(val orgId: String) : AdminRegisterState()
    data class Error(val message: String) : AdminRegisterState()
}

class AdminRegisterViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow<AdminRegisterState>(AdminRegisterState.Idle)
    val state: StateFlow<AdminRegisterState> = _state

    fun register(
        orgName: String, adminName: String, email: String,
        phone: String, city: String, orgSize: String, password: String
    ) {
        viewModelScope.launch {
            _state.value = AdminRegisterState.Loading
            val result = repo.adminRegister(orgName, adminName, email, phone, city, orgSize, password)
            _state.value = if (result.isSuccess) {
                AdminRegisterState.Success(result.getOrNull()?.orgId ?: "")
            } else {
                AdminRegisterState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resetState() { _state.value = AdminRegisterState.Idle }
}
