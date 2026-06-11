package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.local.TokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminLoginViewModel(app: Application) : AndroidViewModel(app) {
    private val api = AdminAuthClient.api
    private val dataStore = TokenDataStore(app)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    fun clearError() { _error.value = "" }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = ""
            try {
                val res = api.login(AdminLoginBody(email.trim(), password))
                val body = res.body()
                if (res.isSuccessful && body?.ok == true && body.token != null) {
                    dataStore.saveToken(body.token)
                    onSuccess()
                } else {
                    _error.value = body?.error ?: body?.message ?: "Invalid email or password."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error. Please try again."
            } finally {
                _loading.value = false
            }
        }
    }
}
