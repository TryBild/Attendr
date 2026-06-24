package com.trybild.attendr.ui.employee

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.api.RetrofitClient
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.MyAttendanceSummary
import com.trybild.attendr.data.repository.AuthRepository
import com.trybild.attendr.ui.admin.Holidays
import com.trybild.attendr.ui.myattendance.EmployeeDayData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DashboardState(
    val employeeName: String = "",
    val companyName: String = "",
    val todayStatus: String? = null,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val workingHours: Double? = null,
    val summary: MyAttendanceSummary? = null,
    val weekData: List<EmployeeDayData> = emptyList(),
    val loading: Boolean = false,
    val mockFlaggedCount: Int = 0
)

class EmployeeDashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = TokenDataStore(app)
    private val api = RetrofitClient.api
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init { loadAll() }

    fun refresh() { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            val name = dataStore.employeeName.firstOrNull() ?: ""
            val company = dataStore.companyName.firstOrNull() ?: ""
            _state.update { it.copy(employeeName = name, companyName = company) }
        }
        loadToday()
        loadMonth()
    }

    private fun loadToday() {
        viewModelScope.launch {
            try {
                val t = dataStore.token.firstOrNull() ?: return@launch
                val res = api.getTodayLogs("Bearer $t")
                if (res.isSuccessful) {
                    val body = res.body()
                    _state.update {
                        it.copy(
                            todayStatus  = body?.status,
                            checkInTime  = body?.checkInTime,
                            checkOutTime = body?.checkOutTime,
                            workingHours = body?.workingHours
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadMonth() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val monthStr = "%04d-%02d".format(year, month)

        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val result = repo.getMyAttendance(monthStr)
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val today = sdf.format(Date())

                // Find Monday of the week containing today
                val todayCal = Calendar.getInstance()
                val dow = todayCal.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
                todayCal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)

                val dayCal = Calendar.getInstance()
                val weekData = (0..5).map { i ->   // Mon … Sat
                    dayCal.time = todayCal.time
                    dayCal.add(Calendar.DAY_OF_MONTH, i)
                    val dateStr = sdf.format(dayCal.time)
                    val record = body.records?.find { it.date == dateStr }
                    val isWeekend = dayCal.get(Calendar.DAY_OF_WEEK).let {
                        it == Calendar.SATURDAY || it == Calendar.SUNDAY
                    }
                    val holidayName = Holidays.forDate(dateStr, dayCal.get(Calendar.YEAR))
                    EmployeeDayData(
                        date = dateStr,
                        status = record?.status,
                        checkInTime = record?.checkInTime,
                        checkOutTime = record?.checkOutTime,
                        workingHours = record?.workingHours,
                        isHoliday = holidayName != null,
                        holidayName = holidayName,
                        isWeekend = isWeekend
                    )
                }
                val flagged = body.records?.count { it.mockDetected } ?: 0
                _state.update { it.copy(summary = body.summary, weekData = weekData, loading = false, mockFlaggedCount = flagged) }
            } else {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}
