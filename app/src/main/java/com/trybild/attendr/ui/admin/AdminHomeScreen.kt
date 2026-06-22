package com.trybild.attendr.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.data.model.RecentActivityItem
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val AmberColor = Color(0xFFF57C00)
private val AmberLight = Color(0xFFFFF8E1)
private val AmberBorder = Color(0xFFFFB300)
private val GreenLight = Color(0xFFE8F5E9)
private val RedLight = Color(0xFFFFEBEE)
private val BlueLight = Color(0xFFE3F2FD)
private val BlueDeep = Color(0xFF1565C0)
private val AmberChipBg = Color(0xFFFFF3E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {
    val vm: AdminDashboardViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── 1. Header ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${greeting()}, ${state.adminName.ifEmpty { "Admin" }}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = AttendrTextPrimary
                    )
                    Text(
                        headerDate(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AttendrTextSecondary
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = AttendrTextSecondary
                    )
                }
                val initials = nameInitials(state.adminName)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AttendrNavy)
                        .clickable { navController.navigate("admin_profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials.ifEmpty { "A" },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // ── Org badge ─────────────────────────────────────────────────
            if (state.orgName.isNotEmpty() || state.orgId.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, AttendrNavy.copy(alpha = 0.25f), RoundedCornerShape(50))
                            .background(AttendrNavy.copy(alpha = 0.07f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        val label = buildString {
                            if (state.orgName.isNotEmpty()) append(state.orgName)
                            if (state.orgName.isNotEmpty() && state.orgId.isNotEmpty()) append(" · ")
                            if (state.orgId.isNotEmpty()) append(state.orgId)
                        }
                        Text(
                            label,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = AttendrNavy
                        )
                    }
                }
            }

            // ── 2. Today's Overview ───────────────────────────────────────
            SectionTitle("Today's Overview")

            if (state.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AttendrNavy, modifier = Modifier.size(28.dp))
                }
            } else {
                val onTimePresentCount = (state.present - state.late).coerceAtLeast(0)
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            label = "Present",
                            value = "$onTimePresentCount",
                            sub = "of ${state.totalEmployees} employees",
                            valueColor = AttendrSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Absent",
                            value = "${state.absent}",
                            sub = "today",
                            valueColor = AttendrError,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            label = "Late",
                            value = "${state.late}",
                            sub = "late arrivals",
                            valueColor = AmberColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Attendance",
                            value = "${state.attendancePercent}%",
                            sub = "attendance rate",
                            valueColor = AttendrNavy,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── 3. Alert banner ───────────────────────────────────────────
            AnimatedVisibility(
                visible = !state.loading && state.notCheckedIn > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberLight),
                    border = BorderStroke(1.dp, AmberBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "${state.notCheckedIn} employee${if (state.notCheckedIn > 1) "s" else ""} haven't checked in yet — it's ${currentTime()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF7B5800)
                            )
                            Text(
                                "View list",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AttendrNavy,
                                modifier = Modifier.clickable { /* stub — no dedicated screen yet */ }
                            )
                        }
                    }
                }
            }

            // ── 4. This week's attendance ─────────────────────────────────
            SectionTitle("This week's attendance")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (state.weeklyLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AttendrNavy, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.weeklyData.forEach { day ->
                            WeeklyRow(day)
                        }
                    }
                }
            }

            // ── 5. Recent activity ────────────────────────────────────────
            if (state.recentActivity.isNotEmpty()) {
                SectionTitle("Recent activity")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        state.recentActivity.forEachIndexed { index, item ->
                            ActivityRow(item)
                            if (index < state.recentActivity.lastIndex) {
                                HorizontalDivider(color = AttendrDivider, thickness = 0.5.dp)
                            }
                        }
                        TextButton(
                            onClick = { /* stub — no dedicated activity screen yet */ },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("See all", color = AttendrNavy, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // ── 6. Quick actions ──────────────────────────────────────────
            SectionTitle("Quick Actions")

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.People,
                    title = "Manage Employees",
                    subtitle = "View and manage your team",
                    onClick = { navController.navigate("admin_employees") }
                )
                QuickActionCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Attendance Records",
                    subtitle = "View attendance history",
                    onClick = { navController.navigate("admin_attendance") }
                )
            }
        }
    }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = AttendrTextPrimary,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp)
    )
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    sub: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AttendrTextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = valueColor
            )
            Text(
                sub,
                style = MaterialTheme.typography.labelSmall,
                color = AttendrTextSecondary
            )
        }
    }
}

@Composable
private fun WeeklyRow(day: WeeklyDayData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            day.dayLabel,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (day.isFuture) AttendrTextSecondary.copy(alpha = 0.5f) else AttendrTextSecondary,
            modifier = Modifier.width(28.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (day.isFuture) Color(0xFFF0F0F0) else AttendrBorder)
        ) {
            if (day.hasData && day.percent > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (day.percent / 100f).coerceIn(0f, 1f))
                        .background(AttendrNavy)
                )
            }
        }
        Text(
            if (day.isFuture) "–" else "${day.percent}%",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (day.isFuture) AttendrTextSecondary.copy(alpha = 0.4f) else AttendrTextPrimary,
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
private fun ActivityRow(item: RecentActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val initials = nameInitials(item.employeeName)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AttendrNavy.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initials.ifEmpty { "?" },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = AttendrNavy
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.employeeName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = AttendrTextPrimary
            )
            if (!item.department.isNullOrEmpty()) {
                Text(
                    item.department,
                    style = MaterialTheme.typography.labelSmall,
                    color = AttendrTextSecondary
                )
            }
        }
        val (chipText, chipBg, chipFg) = when {
            item.action == "checkout" -> Triple("Checked out", BlueLight, BlueDeep)
            item.status == "late"     -> Triple("Late", AmberChipBg, AmberColor)
            else                      -> Triple("Checked in", GreenLight, AttendrSuccess)
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = chipBg
        ) {
            Text(
                chipText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = chipFg
            )
        }
        if (!item.time.isNullOrEmpty()) {
            Text(
                item.time,
                style = MaterialTheme.typography.labelSmall,
                color = AttendrTextSecondary
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AttendrNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = AttendrTextPrimary
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AttendrTextSecondary
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = AttendrTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun headerDate(): String =
    SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).format(Date())

private fun currentTime(): String =
    SimpleDateFormat("h:mm a", Locale.ENGLISH).format(Date())

private fun nameInitials(name: String): String =
    name.trim().split("\\s+".toRegex())
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it[0].uppercaseChar().toString() }
