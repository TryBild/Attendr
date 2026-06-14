package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminDashboardState(
    val loading: Boolean = true,
    val orgName: String = "",
    val adminName: String = "",
    val orgId: String = ""
)

class AdminDashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val dataStore = TokenDataStore(app)

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            val cachedOrgName = dataStore.companyName.firstOrNull() ?: ""
            val cachedOrgId = dataStore.orgId.firstOrNull() ?: ""
            _state.update { it.copy(orgName = cachedOrgName, orgId = cachedOrgId) }

            val result = repo.adminProfile()
            if (result.isSuccess) {
                val p = result.getOrNull()!!
                _state.update {
                    it.copy(
                        loading = false,
                        orgName = p.orgName ?: cachedOrgName,
                        adminName = p.adminName ?: "",
                        orgId = p.orgId ?: cachedOrgId
                    )
                }
            } else {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            dataStore.clearToken()
            onDone()
        }
    }
}
