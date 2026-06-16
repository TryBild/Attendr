package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.model.AdminEmployeeItem
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminEmployeesState(
    val loading: Boolean = true,
    val employees: List<AdminEmployeeItem> = emptyList(),
    val filtered: List<AdminEmployeeItem> = emptyList(),
    val query: String = "",
    val error: String? = null
)

class AdminEmployeesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val _state = MutableStateFlow(AdminEmployeesState())
    val state: StateFlow<AdminEmployeesState> = _state

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = repo.getAdminEmployees()
            if (result.isSuccess) {
                val list = result.getOrNull()?.employees ?: emptyList()
                _state.update { it.copy(loading = false, employees = list, filtered = list) }
            } else {
                _state.update {
                    it.copy(loading = false, error = result.exceptionOrNull()?.message)
                }
            }
        }
    }

    fun onQuery(q: String) {
        val all = _state.value.employees
        val filtered = if (q.isBlank()) all else all.filter {
            it.fullName.contains(q, ignoreCase = true) ||
            it.department?.name?.contains(q, ignoreCase = true) == true ||
            it.employeeCode?.contains(q, ignoreCase = true) == true ||
            it.designation?.contains(q, ignoreCase = true) == true
        }
        _state.update { it.copy(query = q, filtered = filtered) }
    }
}
