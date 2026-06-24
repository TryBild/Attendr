package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.RecentActivityItem
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class WeeklyDayData(
    val dayLabel: String,
    val date: String,
    val percent: Int,
    val hasData: Boolean,
    val isFuture: Boolean
)

data class AdminDashboardState(
    val loading: Boolean = true,
    val orgName: String = "",
    val adminName: String = "",
    val orgId: String = "",
    val totalEmployees: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val late: Int = 0,
    val attendancePercent: Int = 0,
    val notCheckedIn: Int = 0,
    val weeklyData: List<WeeklyDayData> = emptyList(),
    val weeklyLoading: Boolean = true,
    val recentActivity: List<RecentActivityItem> = emptyList(),
    val billingStatus: String = "",
    val trialDaysLeft: Int = 0
)

class AdminDashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val dataStore = TokenDataStore(app)

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, weeklyLoading = true) }
        viewModelScope.launch {
            val cachedOrgName = dataStore.companyName.firstOrNull() ?: ""
            val cachedOrgId = dataStore.orgId.firstOrNull() ?: ""
            _state.update { it.copy(orgName = cachedOrgName, orgId = cachedOrgId) }

            val profileDeferred = async { repo.adminProfile() }
            val dashboardDeferred = async { repo.adminDashboard() }

            val profileResult = profileDeferred.await()
            val dashboardResult = dashboardDeferred.await()

            if (profileResult.isSuccess) {
                val p = profileResult.getOrNull()!!
                _state.update {
                    it.copy(
                        orgName = p.orgName ?: cachedOrgName,
                        adminName = p.adminName ?: "",
                        orgId = p.orgId ?: cachedOrgId
                    )
                }
            }

            if (dashboardResult.isSuccess) {
                val d = dashboardResult.getOrNull()!!
                val today = d.today
                if (today != null) {
                    _state.update {
                        it.copy(
                            loading = false,
                            totalEmployees = today.totalEmployees,
                            present = today.present,
                            absent = today.absent,
                            late = today.late,
                            attendancePercent = today.attendancePercent,
                            notCheckedIn = today.absent,
                            recentActivity = d.recentActivity ?: emptyList()
                        )
                    }
                } else {
                    _state.update { it.copy(loading = false) }
                }
            } else {
                _state.update { it.copy(loading = false) }
            }

            loadWeeklyData()
            loadBilling()
        }
    }

    private suspend fun loadBilling() {
        val result = repo.getBillingStatus()
        if (result.isSuccess) {
            val b = result.getOrNull()!!
            _state.update { it.copy(billingStatus = b.status ?: "", trialDaysLeft = b.trialDaysLeft) }
        }
    }

    private suspend fun loadWeeklyData() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())
        val weekDates = currentWeekDates()
        val result = mutableListOf<WeeklyDayData>()

        for ((label, date) in weekDates) {
            if (date > today) {
                result.add(WeeklyDayData(label, date, 0, hasData = false, isFuture = true))
            } else {
                val res = repo.getDayRegister(date)
                if (res.isSuccess) {
                    val r = res.getOrNull()!!
                    val pct = if (r.total > 0) (r.present * 100) / r.total else 0
                    result.add(WeeklyDayData(label, date, pct, hasData = true, isFuture = false))
                } else {
                    result.add(WeeklyDayData(label, date, 0, hasData = false, isFuture = false))
                }
            }
        }

        _state.update { it.copy(weeklyData = result, weeklyLoading = false) }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            dataStore.clearToken()
            onDone()
        }
    }
}

private fun currentWeekDates(): List<Pair<String, String>> {
    val cal = Calendar.getInstance()
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val offset = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_MONTH, -offset)

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return labels.map { label ->
        val date = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        Pair(label, date)
    }
}
