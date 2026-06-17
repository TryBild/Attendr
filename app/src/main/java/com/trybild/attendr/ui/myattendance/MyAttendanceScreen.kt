package com.trybild.attendr.ui.myattendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val PresentGreen = Color(0xFF2E7D32)
private val AbsentRed    = Color(0xFFD32F2F)
private val HolidayOrange = Color(0xFFFFB300)
private val HolidayBg    = Color(0xFFFFF8E1)
private val OffGrey      = Color(0xFFBDBDBD)
private val LeaveBlue    = Color(0xFF1565C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAttendanceScreen(navController: NavController) {
    val vm: MyAttendanceViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = AttendrBackground,
        topBar = {
            TopAppBar(
                title = { Text("My Attendance", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AttendrBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(4.dp))

            // Summary chips
            state.summary?.let { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryChip("Present",  "${summary.present}",           PresentGreen, Modifier.weight(1f))
                        SummaryChip("Absent",   "${summary.absent}",            AbsentRed,    Modifier.weight(1f))
                        SummaryChip("Late",     "${summary.late}",              HolidayOrange, Modifier.weight(1f))
                        SummaryChip("Attnd %",  "${summary.attendancePercent}%", AttendrNavy,  Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Calendar card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month nav
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { vm.prevMonth() }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month",
                                tint = AttendrNavy)
                        }
                        Text(
                            monthYearLabel(state.year, state.month),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AttendrTextPrimary
                        )
                        IconButton(onClick = { vm.nextMonth() }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next month",
                                tint = AttendrNavy)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Day-of-week header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { d ->
                            Text(
                                d,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AttendrTextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (state.loading) {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AttendrNavy, modifier = Modifier.size(28.dp))
                        }
                    } else {
                        EmployeeCalendarGrid(
                            year = state.year,
                            month = state.month,
                            dayData = state.dayData,
                            onDateClick = { vm.selectDate(it) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Legend
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendDot(PresentGreen, "Present")
                    LegendDot(AbsentRed,    "Absent")
                    LegendDot(HolidayOrange, "Holiday")
                    LegendDot(OffGrey,      "Off")
                    LegendDot(LeaveBlue,    "Leave")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Day detail bottom sheet
    if (state.selectedDate != null) {
        ModalBottomSheet(
            onDismissRequest = { vm.clearSelection() },
            sheetState = sheetState,
            containerColor = AttendrSurface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(36.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AttendrBorder)
                )
            }
        ) {
            val date = state.selectedDate!!
            val data = state.dayData[date]
            DaySheet(date = date, data = data)
        }
    }
}

// ── Calendar grid ──────────────────────────────────────────────────────────────

@Composable
private fun EmployeeCalendarGrid(
    year: Int,
    month: Int,
    dayData: Map<String, EmployeeDayData>,
    onDateClick: (String) -> Unit
) {
    if (year == 0) return

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val today = sdf.format(Date())

    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val startOffset = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY

    val cells: List<Int?> = List(startOffset) { null } + (1..daysInMonth).toList()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        val dateStr = "%04d-%02d-%02d".format(year, month, day)
                        val data = dayData[dateStr]
                        EmployeeCalendarCell(
                            day = day,
                            dateStr = dateStr,
                            data = data,
                            isToday = dateStr == today,
                            isFuture = dateStr > today,
                            onClick = { onDateClick(dateStr) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                repeat(7 - week.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun EmployeeCalendarCell(
    day: Int,
    dateStr: String,
    data: EmployeeDayData?,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHoliday = data?.isHoliday == true
    val isWeekend = data?.isWeekend == true

    val cellBg = when {
        isToday   -> AttendrNavy.copy(alpha = 0.12f)
        isHoliday -> HolidayBg
        else      -> Color.Transparent
    }

    val dotColor = when {
        isFuture  -> Color.Transparent
        isHoliday -> HolidayOrange
        data?.status == "present" || data?.status == "late" -> PresentGreen
        data?.status == "absent"  -> AbsentRed
        data?.status == "leave"   -> LeaveBlue
        isWeekend -> OffGrey
        !isFuture -> OffGrey
        else      -> Color.Transparent
    }

    val textColor = when {
        isToday   -> AttendrNavy
        isFuture  -> AttendrTextSecondary.copy(alpha = 0.5f)
        isHoliday -> Color(0xFF7B5800)
        isWeekend -> AttendrTextSecondary
        else      -> AttendrTextPrimary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(cellBg)
            .then(if (isToday) Modifier.border(1.dp, AttendrNavy.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                  else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$day",
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            Spacer(Modifier.height(2.dp))
            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(dotColor))
        }
    }
}

// ── Day detail sheet ──────────────────────────────────────────────────────────

@Composable
private fun DaySheet(date: String, data: EmployeeDayData?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            longDate(date),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = AttendrTextPrimary
        )

        if (data?.isHoliday == true) {
            Spacer(Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(50), color = HolidayBg) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null,
                        tint = HolidayOrange, modifier = Modifier.size(14.dp))
                    Text(
                        data.holidayName ?: "Holiday",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF7B5800)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = AttendrDivider)
        Spacer(Modifier.height(12.dp))

        if (data == null) {
            Text("No data for this date.", style = MaterialTheme.typography.bodyMedium,
                color = AttendrTextSecondary)
            return
        }

        when {
            data.isWeekend && data.status == null && !data.isHoliday -> {
                Text("Day off (weekend)", style = MaterialTheme.typography.bodyMedium,
                    color = AttendrTextSecondary)
            }
            data.status == "present" || data.status == "late" -> {
                SheetRow("Check In",  formatIsoTime(data.checkInTime)  ?: "—")
                SheetRow("Check Out", formatIsoTime(data.checkOutTime) ?: "—")
                data.workingHours?.let {
                    SheetRow("Hours", "%.1f hrs".format(it))
                }
                if (data.status == "late") {
                    Spacer(Modifier.height(6.dp))
                    Surface(shape = RoundedCornerShape(50), color = HolidayOrange.copy(alpha = 0.12f)) {
                        Text("Late arrival",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = HolidayOrange)
                    }
                }
            }
            data.status == "absent" -> {
                Text("Absent", style = MaterialTheme.typography.bodyMedium,
                    color = AbsentRed)
            }
            data.status == "leave" -> {
                Text("On leave", style = MaterialTheme.typography.bodyMedium,
                    color = LeaveBlue)
            }
            else -> {
                Text("No record for this date.", style = MaterialTheme.typography.bodyMedium,
                    color = AttendrTextSecondary)
            }
        }
    }
}

@Composable
private fun SheetRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AttendrTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = AttendrTextPrimary)
    }
}

@Composable
private fun SummaryChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.10f), modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color)
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = AttendrTextSecondary)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun monthYearLabel(year: Int, month: Int): String {
    if (year == 0) return ""
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    return SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time)
}

private fun longDate(dateStr: String): String {
    return try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr) ?: return dateStr
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).format(d)
    } catch (e: Exception) { dateStr }
}

private fun formatIsoTime(isoString: String?): String? {
    if (isoString.isNullOrBlank()) return null
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inFmt.parse(isoString) ?: return isoString.take(16)
        SimpleDateFormat("h:mm a", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }.format(date)
    } catch (_: Exception) { isoString.take(16) }
}
