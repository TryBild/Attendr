package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdminLoginState {
    object Idle : AdminLoginState()
    object Loading : AdminLoginState()
    data class Success(val companyName: String) : AdminLoginState()
    data class Error(val message: String) : AdminLoginState()
}

class AdminLoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow<AdminLoginState>(AdminLoginState.Idle)
    val state: StateFlow<AdminLoginState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AdminLoginState.Loading
            val result = repo.adminLogin(email, password)
            _state.value = if (result.isSuccess) {
                AdminLoginState.Success(result.getOrNull()?.company?.name ?: "Admin")
            } else {
                AdminLoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun resetState() { _state.value = AdminLoginState.Idle }
}
