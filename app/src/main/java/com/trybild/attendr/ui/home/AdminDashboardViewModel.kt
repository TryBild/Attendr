package com.trybild.attendr.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

data class AttendanceOverview(
    val present: Int = 0,
    val absent: Int = 0,
    val onLeave: Int = 0
) {
    val total: Int get() = present + absent + onLeave
    val presentPercent: Int get() = if (total == 0) 0 else (present * 100) / total
}

class AdminDashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = TokenDataStore(app)

    // Mock data until the backend exposes team attendance endpoints
    private val _overview = MutableStateFlow(AttendanceOverview(present = 0, absent = 0, onLeave = 0))
    val overview: StateFlow<AttendanceOverview> = _overview

    private val _hasEmployees = MutableStateFlow(false)
    val hasEmployees: StateFlow<Boolean> = _hasEmployees

    val adminName = dataStore.token.map { token ->
        token?.let { JwtUtils.decodeTokenName(it) } ?: "Admin"
    }
}
