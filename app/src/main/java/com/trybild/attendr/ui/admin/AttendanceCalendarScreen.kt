package com.trybild.attendr.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.trybild.attendr.data.model.DayRegisterRow
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val HolidayBg    = Color(0xFFFFF8E1)
private val HolidayDot   = Color(0xFFFFB300)
private val LateAmber    = Color(0xFFF57C00)
private val PresentGreen = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCalendarScreen(navController: NavController) {
    val vm: AttendanceCalendarViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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

            // Month navigation header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month nav row
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

                    // Calendar grid
                    CalendarGrid(
                        year = state.year,
                        month = state.month,
                        daySummaries = state.daySummaries,
                        onDateClick = { vm.selectDate(it) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Holiday legend
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Legend",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AttendrTextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendItem(color = PresentGreen, label = "≥75% present")
                        LegendItem(color = LateAmber,    label = "40–74%")
                        LegendItem(color = AttendrError, label = "<40%")
                        LegendItem(color = HolidayDot,   label = "Holiday")
                    }
                }
            }

            // Holidays in this month (if any)
            val monthHolidays = state.daySummaries.values
                .filter { it.isHoliday }
                .sortedBy { it.date }
            if (monthHolidays.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Holidays this month",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AttendrTextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        monthHolidays.forEach { h ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier
                                    .size(8.dp).clip(CircleShape).background(HolidayDot))
                                Text(
                                    "${shortDate(h.date)}  ·  ${h.holidayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AttendrTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
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
            val summary = state.daySummaries[date]
            DayDetailSheet(
                date = date,
                summary = summary,
                sheetLoading = state.sheetLoading
            )
        }
    }
}

// ── Calendar grid ──────────────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    daySummaries: Map<String, CalendarDaySummary>,
    onDateClick: (String) -> Unit
) {
    if (year == 0) return

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val today = sdf.format(Date())

    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Offset from Monday (Mon=0 … Sun=6)
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val startOffset = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY

    // Build list: nulls for leading blanks, then 1..daysInMonth
    val cells: List<Int?> = List(startOffset) { null } + (1..daysInMonth).toList()
    val weeks = cells.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        val dateStr = "%04d-%02d-%02d".format(year, month, day)
                        val summary = daySummaries[dateStr]
                        CalendarCell(
                            day = day,
                            dateStr = dateStr,
                            summary = summary,
                            isToday = dateStr == today,
                            isFuture = dateStr > today,
                            onClick = { onDateClick(dateStr) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Pad final week to 7 columns
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(
    day: Int,
    dateStr: String,
    summary: CalendarDaySummary?,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHoliday = summary?.isHoliday == true
    val isLoaded  = summary?.loaded == true

    val cellBg = when {
        isToday   -> AttendrNavy.copy(alpha = 0.12f)
        isHoliday -> HolidayBg
        else      -> Color.Transparent
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
                color = when {
                    isToday   -> AttendrNavy
                    isFuture  -> AttendrTextSecondary.copy(alpha = 0.5f)
                    isHoliday -> Color(0xFF7B5800)
                    else      -> AttendrTextPrimary
                }
            )
            Spacer(Modifier.height(2.dp))
            // Indicator dot
            val dotColor = when {
                isHoliday              -> HolidayDot
                !isLoaded || isFuture  -> Color.Transparent
                summary != null && summary.total > 0 -> {
                    val pct = summary.present.toFloat() / summary.total
                    when {
                        pct >= 0.75f -> PresentGreen
                        pct >= 0.40f -> LateAmber
                        else         -> AttendrError
                    }
                }
                else -> Color.Transparent
            }
            Box(modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor))
        }
    }
}

// ── Day detail bottom sheet ────────────────────────────────────────────────────

@Composable
private fun DayDetailSheet(
    date: String,
    summary: CalendarDaySummary?,
    sheetLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Date title
        Text(
            longDate(date),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = AttendrTextPrimary
        )

        // Holiday badge
        if (summary?.isHoliday == true) {
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = HolidayBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null,
                        tint = HolidayDot, modifier = Modifier.size(14.dp))
                    Text(
                        summary.holidayName ?: "Holiday",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF7B5800)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (sheetLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AttendrNavy, modifier = Modifier.size(28.dp))
            }
            return
        }

        if (summary == null || !summary.loaded) {
            Text(
                "No data available for this date.",
                style = MaterialTheme.typography.bodyMedium,
                color = AttendrTextSecondary
            )
            return
        }

        // Summary chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SheetStatChip("Present", "${summary.present}", PresentGreen)
            SheetStatChip("Late",    "${summary.rows.count { it.late }}", LateAmber)
            SheetStatChip("Absent",  "${summary.total - summary.present}", AttendrError)
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = AttendrDivider)
        Spacer(Modifier.height(8.dp))

        if (summary.rows.isEmpty()) {
            Text("No employee records for this date.",
                style = MaterialTheme.typography.bodyMedium,
                color = AttendrTextSecondary,
                modifier = Modifier.padding(vertical = 12.dp))
        } else {
            // Employee list (max ~10 rows visible before scrolling handled by sheet)
            summary.rows.forEachIndexed { index, row ->
                DayEmployeeRow(row = row)
                if (index < summary.rows.lastIndex) {
                    HorizontalDivider(color = AttendrDivider, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun SheetStatChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun DayEmployeeRow(row: DayRegisterRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AvatarCircle(name = row.fullName, size = 34)
        Column(modifier = Modifier.weight(1f)) {
            Text(row.fullName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = AttendrTextPrimary)
            if (row.department != "-") {
                Text(row.department,
                    style = MaterialTheme.typography.labelSmall,
                    color = AttendrTextSecondary)
            }
        }
        // Status chip
        val (chipText, chipColor) = when {
            row.status == "absent" -> "Absent" to AttendrError
            row.late               -> "Late" to LateAmber
            row.status == "present" -> "Present" to PresentGreen
            else                   -> row.status to AttendrTextSecondary
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = chipColor.copy(alpha = 0.12f)
        ) {
            Text(
                chipText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = chipColor
            )
        }
        if (!row.checkInTime.isNullOrBlank()) {
            Text(row.checkInTime,
                style = MaterialTheme.typography.labelSmall,
                color = AttendrTextSecondary)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
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

private fun shortDate(dateStr: String): String {
    return try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr) ?: return dateStr
        SimpleDateFormat("d MMM", Locale.ENGLISH).format(d)
    } catch (e: Exception) { dateStr }
}
