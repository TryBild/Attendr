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
    data class Success(val companyName: String, val setupComplete: Boolean) : AdminLoginState()
    data class Error(val message: String) : AdminLoginState()
}

class AdminLoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow<AdminLoginState>(AdminLoginState.Idle)
    val state: StateFlow<AdminLoginState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AdminLoginState.Loading
            val loginResult = repo.adminLogin(email, password)
            if (loginResult.isFailure) {
                _state.value = AdminLoginState.Error(
                    loginResult.exceptionOrNull()?.message ?: "Login failed"
                )
                return@launch
            }
            val companyName = loginResult.getOrNull()?.company?.name ?: "Admin"
            val profileResult = repo.adminProfile()
            val setupComplete = if (profileResult.isSuccess) {
                profileResult.getOrNull()?.setupComplete ?: false
            } else {
                loginResult.getOrNull()?.company?.setupComplete ?: false
            }
            _state.value = AdminLoginState.Success(companyName, setupComplete)
        }
    }

    fun resetState() { _state.value = AdminLoginState.Idle }
}
