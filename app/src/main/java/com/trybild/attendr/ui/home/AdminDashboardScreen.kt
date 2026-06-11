package com.trybild.attendr.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trybild.attendr.ui.theme.*

private data class DashboardTab(val label: String, val icon: ImageVector)

private val dashboardTabs = listOf(
    DashboardTab("Home", Icons.Outlined.Home),
    DashboardTab("Employees", Icons.Outlined.Groups),
    DashboardTab("Leaves", Icons.Outlined.EventAvailable),
    DashboardTab("Reports", Icons.Outlined.BarChart),
    DashboardTab("Profile", Icons.Outlined.Person)
)

@Composable
fun AdminDashboardScreen() {
    val vm: AdminDashboardViewModel = viewModel()
    val overview by vm.overview.collectAsState()
    val hasEmployees by vm.hasEmployees.collectAsState()
    val adminName by vm.adminName.collectAsState(initial = "Admin")

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = AttendrBackground,
        topBar = { DashboardTopBar(adminName = adminName) },
        bottomBar = {
            DashboardBottomBar(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
    ) { padding ->
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                if (!hasEmployees) {
                    item {
                        SetupCard()
                        Spacer(Modifier.height(16.dp))
                    }
                }
                item {
                    OverviewCard(overview)
                    Spacer(Modifier.height(16.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        dashboardTabs[selectedTab].icon,
                        contentDescription = null,
                        tint = AttendrTextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${dashboardTabs[selectedTab].label} — Coming soon",
                        color = AttendrTextSecondary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardTopBar(adminName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(AttendrNavy, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("A", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "Attendr",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AttendrTextPrimary
        )
        Spacer(Modifier.weight(1f))
        val initials = adminName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "A" }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AttendrNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SetupCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AttendrBorder, RoundedCornerShape(12.dp))
            .background(AttendrBackground, RoundedCornerShape(12.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Welcome to Attendr",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AttendrTextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "It looks like you haven't added your employee details",
            color = AttendrTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text("2 options to add them:", fontSize = 14.sp, color = AttendrTextPrimary)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SetupOptionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Description,
                label = "Add from\nExcel or CSV"
            )
            SetupOptionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.PersonAdd,
                label = "Add Manually\nin the App"
            )
        }
    }
}

@Composable
private fun SetupOptionCard(modifier: Modifier, icon: ImageVector, label: String) {
    Column(
        modifier = modifier
            .border(1.dp, AttendrBorder, RoundedCornerShape(12.dp))
            .background(AttendrSurface, RoundedCornerShape(12.dp))
            .clickable { /* employee onboarding ships after MVP */ }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AttendrTextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OverviewCard(overview: AttendanceOverview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AttendrBorder, RoundedCornerShape(12.dp))
            .background(AttendrBackground, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Text(
            "Today's Attendance Overview",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = AttendrTextPrimary
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DonutChart(overview)
            Spacer(Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                LegendRow(AttendrNavy, "Present", overview.present, AttendrTextPrimary)
                Spacer(Modifier.height(8.dp))
                LegendRow(AttendrTextSecondary, "Absent", overview.absent, AttendrTextSecondary)
                Spacer(Modifier.height(8.dp))
                LegendRow(AttendrBorder, "On Leave", overview.onLeave, AttendrTextSecondary)
            }
        }
    }
}

@Composable
private fun DonutChart(overview: AttendanceOverview) {
    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
            val inset = 6.dp.toPx()
            val arcSize = androidx.compose.ui.geometry.Size(
                size.width - inset * 2,
                size.height - inset * 2
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)

            if (overview.total == 0) {
                drawArc(
                    color = AttendrBorder,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
            } else {
                val presentSweep = 360f * overview.present / overview.total
                val absentSweep = 360f * overview.absent / overview.total
                val leaveSweep = 360f - presentSweep - absentSweep
                var start = -90f
                drawArc(AttendrNavy, start, presentSweep, false, topLeft, arcSize, style = stroke)
                start += presentSweep
                drawArc(AttendrTextSecondary, start, absentSweep, false, topLeft, arcSize, style = stroke)
                start += absentSweep
                drawArc(AttendrBorder, start, leaveSweep, false, topLeft, arcSize, style = stroke)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${overview.presentPercent}%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AttendrNavy
            )
            Text("Present", fontSize = 12.sp, color = AttendrTextSecondary)
        }
    }
}

@Composable
private fun LegendRow(dotColor: Color, label: String, count: Int, countColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, color = AttendrTextSecondary)
        Spacer(Modifier.weight(1f))
        Text("$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = countColor)
    }
}

@Composable
private fun DashboardBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(AttendrBorder))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AttendrBackground)
                .navigationBarsPadding()
        ) {
            dashboardTabs.forEachIndexed { index, tab ->
                val isSelected = index == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .width(32.dp)
                            .height(3.dp)
                            .background(if (isSelected) AttendrNavy else Color.Transparent)
                    )
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) AttendrNavy else AttendrTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        color = if (isSelected) AttendrNavy else AttendrTextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
