package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.model.DayRegisterRow
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDaySummary(
    val date: String,
    val present: Int = 0,
    val total: Int = 0,
    val rows: List<DayRegisterRow> = emptyList(),
    val loaded: Boolean = false,
    val isHoliday: Boolean = false,
    val holidayName: String? = null
)

data class AttendanceCalendarState(
    val year: Int = 0,
    val month: Int = 0,                         // 1–12
    val daySummaries: Map<String, CalendarDaySummary> = emptyMap(),
    val selectedDate: String? = null,
    val sheetLoading: Boolean = false
)

class AttendanceCalendarViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val _state = MutableStateFlow(AttendanceCalendarState())
    val state: StateFlow<AttendanceCalendarState> = _state

    init {
        val cal = Calendar.getInstance()
        _state.update {
            it.copy(year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH) + 1)
        }
        loadMonthData()
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
                daySummaries = emptyMap(),
                selectedDate = null
            )
        }
        loadMonthData()
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
                daySummaries = emptyMap(),
                selectedDate = null
            )
        }
        loadMonthData()
    }

    fun selectDate(date: String) {
        val existing = _state.value.daySummaries[date]
        if (existing?.loaded == true) {
            _state.update { it.copy(selectedDate = date, sheetLoading = false) }
            return
        }
        _state.update { it.copy(selectedDate = date, sheetLoading = true) }
        viewModelScope.launch {
            val result = repo.getDayRegister(date)
            if (result.isSuccess) {
                val r = result.getOrNull()!!
                val updated = (existing ?: CalendarDaySummary(date)).copy(
                    present = r.present,
                    total = r.total,
                    rows = r.rows ?: emptyList(),
                    loaded = true
                )
                _state.update {
                    it.copy(
                        sheetLoading = false,
                        daySummaries = it.daySummaries + (date to updated)
                    )
                }
            } else {
                _state.update { it.copy(sheetLoading = false) }
            }
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedDate = null) }
    }

    private fun loadMonthData() {
        val (year, month) = currentYearMonth()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())

        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Pre-populate calendar with holiday info (client-side, instant)
        val initial = (1..daysInMonth).associate { day ->
            val dateStr = "%04d-%02d-%02d".format(year, month, day)
            val holidayName = Holidays.forDate(dateStr, year)
            dateStr to CalendarDaySummary(
                date = dateStr,
                isHoliday = holidayName != null,
                holidayName = holidayName
            )
        }
        _state.update { it.copy(daySummaries = initial) }

        // Fetch attendance data for each past date, concurrently
        val pastDates = initial.keys.filter { it <= today }
        pastDates.forEach { date ->
            viewModelScope.launch {
                val result = repo.getDayRegister(date)
                if (result.isSuccess) {
                    val r = result.getOrNull()!!
                    val holiday = initial[date]
                    val updated = CalendarDaySummary(
                        date = date,
                        present = r.present,
                        total = r.total,
                        rows = r.rows ?: emptyList(),
                        loaded = true,
                        isHoliday = holiday?.isHoliday == true,
                        holidayName = holiday?.holidayName
                    )
                    _state.update { it.copy(daySummaries = it.daySummaries + (date to updated)) }
                } else {
                    val holiday = initial[date]
                    _state.update {
                        it.copy(
                            daySummaries = it.daySummaries + (date to
                                (holiday ?: CalendarDaySummary(date)).copy(loaded = true))
                        )
                    }
                }
            }
        }
    }

    private fun currentYearMonth(): Pair<Int, Int> =
        _state.value.let { Pair(it.year, it.month) }
}
