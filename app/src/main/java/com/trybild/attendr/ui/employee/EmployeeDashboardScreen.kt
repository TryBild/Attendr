package com.trybild.attendr.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.trybild.attendr.ui.components.GeofenceBadgeChip
import com.trybild.attendr.ui.home.GeofenceBadge
import com.trybild.attendr.ui.myattendance.EmployeeDayData
import com.trybild.attendr.ui.theme.*
import com.trybild.attendr.utils.formatDuration
import com.trybild.attendr.utils.formatIsoTime
import com.trybild.attendr.utils.isoToEpochMs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

private val PresentGreen  = Color(0xFF2E7D32)
private val AbsentRed     = Color(0xFFD32F2F)
private val HolidayOrange = Color(0xFFFFB300)
private val OffGrey       = Color(0xFFBDBDBD)
private val LeaveBlue     = Color(0xFF1565C0)

@Composable
fun EmployeeDashboardScreen(
    innerNav: NavController,
    badge: StateFlow<GeofenceBadge>
) {
    val vm: EmployeeDashboardViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val badgeValue by badge.collectAsStateWithLifecycle(initialValue = GeofenceBadge.Loading)

    // Refresh today's status whenever Dashboard tab is shown
    LaunchedEffect(Unit) { vm.refresh() }

    // Live-duration ticker — updates every minute while checked-in
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.checkInTime, state.checkOutTime) {
        if (state.checkInTime != null && state.checkOutTime == null) {
            while (true) {
                delay(60_000L)
                nowMs = System.currentTimeMillis()
            }
        }
    }

    val today = SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH).format(Date())
    val greeting = greeting()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        // ── Header ────────────────────────────────────────────────────────────
        Text(
            "$greeting,",
            style = MaterialTheme.typography.bodyMedium,
            color = AttendrTextSecondary
        )
        Text(
            state.employeeName.ifBlank { "Employee" },
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = AttendrTextPrimary
        )
        Text(
            today,
            style = MaterialTheme.typography.bodySmall,
            color = AttendrTextSecondary
        )

        Spacer(Modifier.height(12.dp))
        GeofenceBadgeChip(badgeValue)
        Spacer(Modifier.height(20.dp))

        // ── Today's status card ───────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AttendrSurface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Today",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AttendrTextSecondary
                )
                Spacer(Modifier.height(10.dp))

                when {
                    state.todayStatus == "not_marked" || state.todayStatus == null -> {
                        Text(
                            "Not checked in yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AttendrTextSecondary
                        )
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TodayItem(
                                label = "Check In",
                                value = formatIsoTime(state.checkInTime) ?: "—"
                            )
                            TodayItem(
                                label = "Check Out",
                                value = formatIsoTime(state.checkOutTime) ?: "—"
                            )
                            TodayItem(
                                label = if (state.checkOutTime == null) "Live" else "Hours",
                                value = when {
                                    state.checkOutTime != null ->
                                        "%.1f h".format(state.workingHours ?: 0.0)
                                    state.checkInTime != null -> {
                                        val start = isoToEpochMs(state.checkInTime) ?: nowMs
                                        formatDuration(nowMs - start)
                                    }
                                    else -> "—"
                                },
                                highlight = state.checkOutTime == null && state.checkInTime != null
                            )
                        }

                        if (state.todayStatus == "late") {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = HolidayOrange.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "Late arrival",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = HolidayOrange
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Monthly summary ───────────────────────────────────────────────────
        state.summary?.let { summary ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "This Month",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AttendrTextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonthlyStat("Present",  "${summary.present}",           PresentGreen, Modifier.weight(1f))
                        MonthlyStat("Absent",   "${summary.absent}",            AbsentRed,    Modifier.weight(1f))
                        MonthlyStat("Late",     "${summary.late}",              HolidayOrange, Modifier.weight(1f))
                        MonthlyStat("Attnd %",  "${summary.attendancePercent}%", AttendrNavy,  Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Week mini-view ────────────────────────────────────────────────────
        if (state.weekData.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "This Week",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AttendrTextSecondary
                        )
                        TextButton(
                            onClick = { innerNav.navigate("tab_calendar") {
                                popUpTo("tab_home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }},
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "View Full Calendar →",
                                style = MaterialTheme.typography.labelSmall,
                                color = AttendrNavy
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    WeekStrip(days = state.weekData)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Week strip ────────────────────────────────────────────────────────────────

private val DAY_LETTERS = listOf("M", "T", "W", "T", "F", "S")

@Composable
private fun WeekStrip(days: List<EmployeeDayData>) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val today = sdf.format(Date())

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        days.forEachIndexed { index, day ->
            val isToday = day.date == today
            val isFuture = day.date > today

            val dotColor = when {
                isFuture  -> Color.Transparent
                day.isHoliday -> HolidayOrange
                day.status == "present" || day.status == "late" -> PresentGreen
                day.status == "absent"  -> AbsentRed
                day.status == "leave"   -> LeaveBlue
                day.isWeekend           -> OffGrey
                else                    -> OffGrey
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    DAY_LETTERS[index],
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isToday) AttendrNavy else AttendrTextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isToday) AttendrNavy.copy(alpha = 0.10f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        day.date.takeLast(2).trimStart('0').ifBlank { "0" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isToday) AttendrNavy else AttendrTextPrimary,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

// ── Small composables ─────────────────────────────────────────────────────────

@Composable
private fun TodayItem(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (highlight) PresentGreen else AttendrTextPrimary
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = AttendrTextSecondary)
    }
}

@Composable
private fun MonthlyStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.10f), modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        else   -> "Good evening"
    }
}
