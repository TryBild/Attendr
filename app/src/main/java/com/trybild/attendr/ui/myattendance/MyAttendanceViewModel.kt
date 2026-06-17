package com.trybild.attendr.ui.myattendance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.model.MyAttendanceSummary
import com.trybild.attendr.data.repository.AuthRepository
import com.trybild.attendr.ui.admin.Holidays
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class EmployeeDayData(
    val date: String,
    val status: String?,
    val checkInTime: String?,
    val checkOutTime: String?,
    val workingHours: Double?,
    val isHoliday: Boolean,
    val holidayName: String?,
    val isWeekend: Boolean
)

data class MyAttendanceState(
    val year: Int = 0,
    val month: Int = 0,
    val dayData: Map<String, EmployeeDayData> = emptyMap(),
    val summary: MyAttendanceSummary? = null,
    val loading: Boolean = false,
    val selectedDate: String? = null,
    val error: String? = null
)

class MyAttendanceViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val _state = MutableStateFlow(MyAttendanceState())
    val state: StateFlow<MyAttendanceState> = _state

    init {
        val cal = Calendar.getInstance()
        _state.update { it.copy(year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH) + 1) }
        loadMonth()
    }

    fun prevMonth() {
        val (y, m) = currentYearMonth()
        val cal = Calendar.getInstance()
        cal.set(y, m - 1, 1)
        cal.add(Calendar.MONTH, -1)
        _state.update {
            it.copy(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                dayData = emptyMap(),
                summary = null,
                selectedDate = null
            )
        }
        loadMonth()
    }

    fun nextMonth() {
        val (y, m) = currentYearMonth()
        val cal = Calendar.getInstance()
        cal.set(y, m - 1, 1)
        cal.add(Calendar.MONTH, 1)
        _state.update {
            it.copy(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                dayData = emptyMap(),
                summary = null,
                selectedDate = null
            )
        }
        loadMonth()
    }

    fun selectDate(date: String) {
        _state.update { it.copy(selectedDate = date) }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedDate = null) }
    }

    private fun loadMonth() {
        val (year, month) = currentYearMonth()
        _state.update { it.copy(loading = true, error = null) }

        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dayCal = Calendar.getInstance()
        val initial = (1..daysInMonth).associate { day ->
            val dateStr = "%04d-%02d-%02d".format(year, month, day)
            dayCal.set(year, month - 1, day)
            val dow = dayCal.get(Calendar.DAY_OF_WEEK)
            val isWeekend = dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
            val holidayName = Holidays.forDate(dateStr, year)
            dateStr to EmployeeDayData(
                date = dateStr,
                status = null,
                checkInTime = null,
                checkOutTime = null,
                workingHours = null,
                isHoliday = holidayName != null,
                holidayName = holidayName,
                isWeekend = isWeekend
            )
        }
        _state.update { it.copy(dayData = initial) }

        viewModelScope.launch {
            val monthStr = "%04d-%02d".format(year, month)
            val result = repo.getMyAttendance(monthStr)
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                val updated = initial.toMutableMap()
                for (r in body.records ?: emptyList()) {
                    val existing = updated[r.date] ?: continue
                    updated[r.date] = existing.copy(
                        status = r.status,
                        checkInTime = r.checkInTime,
                        checkOutTime = r.checkOutTime,
                        workingHours = r.workingHours
                    )
                }
                _state.update { it.copy(dayData = updated, summary = body.summary, loading = false) }
            } else {
                _state.update { it.copy(loading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    private fun currentYearMonth(): Pair<Int, Int> =
        _state.value.let { Pair(it.year, it.month) }
}
