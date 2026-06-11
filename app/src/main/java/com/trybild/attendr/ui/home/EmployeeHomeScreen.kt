package com.trybild.attendr.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.AttendanceLog
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.theme.*
import com.trybild.attendr.utils.JwtUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EmployeeHomeScreen() {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()
    val logs by vm.logs.collectAsState()
    var errorMsg by remember { mutableStateOf("") }
    var locationGranted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dataStore = remember { TokenDataStore(context) }
    val token by dataStore.token.collectAsState(initial = null)
    val userName = remember(token) {
        token?.let { JwtUtils.decodeTokenName(it) } ?: "there"
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        permLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is HomeState.CheckedIn -> { errorMsg = ""; vm.resetState() }
            is HomeState.CheckedOut -> { errorMsg = ""; vm.resetState() }
            is HomeState.Error -> { errorMsg = s.message; vm.resetState() }
            else -> {}
        }
    }

    val lastInLog = logs.lastOrNull { it.type == "in" }
    val checkedIn = lastInLog != null
    val checkedOut = logs.lastOrNull()?.type == "out" && checkedIn
    val loading = state is HomeState.Loading

    Box(Modifier.fillMaxSize().background(AttendrBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${greetingForNow()}, $userName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AttendrTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(Date()),
                    fontSize = 14.sp,
                    color = AttendrTextSecondary
                )
            }
            Text("Ready to mark attendance?", color = AttendrTextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(32.dp))

            StatusCard(
                checkedIn = checkedIn,
                checkedOut = checkedOut,
                sinceTime = lastInLog?.let { formatLogTime(it.at) } ?: ""
            )

            Spacer(Modifier.height(32.dp))

            when {
                !locationGranted -> AttendrButton(
                    text = "Allow Location Permission",
                    onClick = {
                        permLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
                !checkedIn -> AttendrButton(
                    text = "Check In",
                    onClick = { vm.markAttendance("in") },
                    loading = loading,
                    containerColor = AttendrSuccess,
                    leadingIcon = {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                )
                !checkedOut -> AttendrButton(
                    text = "Check Out",
                    onClick = { vm.markAttendance("out") },
                    loading = loading,
                    containerColor = AttendrError
                )
                else -> AttendrButton(
                    text = "Done for today ✓",
                    onClick = {},
                    enabled = false,
                    containerColor = AttendrTextSecondary
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Today's Activity",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(12.dp))

            if (logs.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.EventBusy,
                        contentDescription = null,
                        tint = AttendrBorder,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No attendance marked today", color = AttendrTextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(logs) { log -> LogCard(log) }
                }
            }
        }

        ErrorToast(
            message = errorMsg,
            visible = errorMsg.isNotEmpty(),
            onDismiss = { errorMsg = "" },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
        )
    }
}

@Composable
private fun StatusCard(checkedIn: Boolean, checkedOut: Boolean, sinceTime: String) {
    val bg: Color
    val icon = if (checkedIn) Icons.Filled.CheckCircle else Icons.Outlined.Schedule
    val iconTint: Color
    val title: String
    val titleColor: Color
    val subtitle: String

    when {
        checkedIn && checkedOut -> {
            bg = AttendrCompleteBg
            iconTint = AttendrNavy
            title = "Attendance Complete"
            titleColor = AttendrNavy
            subtitle = "You're done for today"
        }
        checkedIn -> {
            bg = AttendrSuccessBg
            iconTint = AttendrSuccess
            title = "Checked In"
            titleColor = AttendrSuccess
            subtitle = "Since $sinceTime"
        }
        else -> {
            bg = AttendrSurface
            iconTint = AttendrTextSecondary
            title = "Not Checked In"
            titleColor = AttendrTextPrimary
            subtitle = "Tap below to mark attendance"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 14.sp, color = AttendrTextSecondary)
    }
}

@Composable
private fun LogCard(log: AttendanceLog) {
    val isIn = log.type == "in"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AttendrBorder, RoundedCornerShape(12.dp))
            .background(AttendrBackground, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isIn) AttendrSuccessBg else AttendrErrorBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isIn) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                contentDescription = null,
                tint = if (isIn) AttendrSuccess else AttendrError,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (isIn) "Checked In" else "Checked Out",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AttendrTextPrimary
            )
            Text(formatLogTime(log.at), fontSize = 13.sp, color = AttendrTextSecondary)
        }
        Box(
            modifier = Modifier.background(AttendrSuccessBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("Verified", fontSize = 12.sp, color = AttendrSuccess, fontWeight = FontWeight.Medium)
        }
    }
}

private fun greetingForNow(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun formatLogTime(at: String): String {
    // Backend sends ISO-8601 timestamps; show just the HH:mm portion
    val time = at.take(16).substringAfter("T", "")
    return time.ifEmpty { at }
}
