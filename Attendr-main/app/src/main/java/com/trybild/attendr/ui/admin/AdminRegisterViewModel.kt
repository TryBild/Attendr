package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.local.TokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminRegisterViewModel(app: Application) : AndroidViewModel(app) {
    private val api = AdminAuthClient.api
    private val dataStore = TokenDataStore(app)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    // Non-null once registration succeeds; drives the success card
    private val _createdTeamId = MutableStateFlow<String?>(null)
    val createdTeamId: StateFlow<String?> = _createdTeamId

    fun clearError() { _error.value = "" }

    fun register(
        companyName: String,
        adminEmail: String,
        password: String,
        city: String,
        state: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = ""
            try {
                val res = api.register(
                    AdminRegisterBody(
                        companyName = companyName.trim(),
                        adminEmail = adminEmail.trim(),
                        password = password,
                        city = city.trim().ifEmpty { null },
                        state = state.trim().ifEmpty { null }
                    )
                )
                val body = res.body()
                if (res.isSuccessful && body?.ok == true && body.token != null) {
                    dataStore.saveToken(body.token)
                    _createdTeamId.value = body.teamId ?: ""
                } else {
                    _error.value = body?.error ?: body?.message ?: "Registration failed. Please try again."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error. Please try again."
            } finally {
                _loading.value = false
            }
        }
    }
}
