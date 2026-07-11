package com.trybild.attendr.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.admin.AvatarCircle
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrCard
import com.trybild.attendr.ui.components.GeofenceBadgeChip
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.theme.AttendrError
import com.trybild.attendr.utils.formatIsoTime
import com.trybild.attendr.utils.formatShortDate

@Composable
fun HomeScreen(
    navController: NavController,
    vm: HomeViewModel = viewModel(),
    onViewCalendar: () -> Unit = { navController.navigate("my_attendance") },
    onOpenProfile: () -> Unit = { navController.navigate("tab_profile") }
) {
    val state by vm.state.collectAsState()
    val logs by vm.logs.collectAsState()
    val recent by vm.recent.collectAsState()
    val employeeName by vm.employeeName.collectAsState()
    val photoUrl by vm.photoUrl.collectAsState()
    val badge by vm.badge.collectAsState()
    val isMockDetected by vm.mockDetected.collectAsState()
    val notifyingAdmin by vm.notifyingAdmin.collectAsState()
    val context = LocalContext.current
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var locationGranted by remember { mutableStateOf(false) }
    var permDeniedCount by remember { mutableIntStateOf(0) }
    var showRationale by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationGranted = granted
        if (!granted) permDeniedCount++
    }

    LaunchedEffect(Unit) {
        permLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    LaunchedEffect(locationGranted) {
        if (locationGranted) vm.onLocationPermissionGranted()
        else vm.onLocationPermissionDenied()
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Location Required", fontWeight = FontWeight.Bold) },
            text = { Text("Attendr needs your location to verify you're at the office when marking attendance. Without it, check-in/out won't work.") },
            confirmButton = {
                if (permDeniedCount >= 2) {
                    TextButton(onClick = {
                        showRationale = false
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        })
                    }) { Text("Open Settings") }
                } else {
                    TextButton(onClick = {
                        showRationale = false
                        permLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }) { Text("Grant Permission") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text("Cancel") }
            }
        )
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is HomeState.CheckedIn  -> { successMsg = "Checked in ✅"; errorMsg = ""; vm.resetState() }
            is HomeState.CheckedOut -> { successMsg = "Checked out 🔴"; errorMsg = ""; vm.resetState() }
            is HomeState.Error -> { errorMsg = s.message; vm.resetState() }
            // Keep GeofenceNotSet in state so the "Notify admin" card stays visible; just clear banners.
            is HomeState.GeofenceNotSet -> { errorMsg = ""; successMsg = "" }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        vm.notifyResult.collect { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
    }

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LogoIcon(size = 32.dp)
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onOpenProfile() }
        ) {
            AvatarCircle(name = employeeName.ifBlank { "Employee" }, size = 72, photoUrl = photoUrl)
        }
        Spacer(Modifier.height(20.dp))

        GeofenceBadgeChip(badge, isMockDetected = isMockDetected)
        Spacer(Modifier.height(if (badge is GeofenceBadge.InsideZone || badge is GeofenceBadge.DistanceAway || isMockDetected) 16.dp else 8.dp))

        if (isMockDetected) {
            AttendrCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    "Fake GPS detected! Attendance will be marked but flagged for review.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = AttendrError
                )
            }
        }

        if (!locationGranted) {
            AttendrCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Location permission is required to mark attendance",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AttendrError
                    )
                    Spacer(Modifier.height(12.dp))
                    AttendrButton(
                        text = if (permDeniedCount >= 2) "Open Settings" else "Allow Location",
                        onClick = { showRationale = true }
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AttendrButton(
                    text = "Check In",
                    onClick = { vm.markAttendance("in") },
                    modifier = Modifier.weight(1f),
                    enabled = state !is HomeState.Loading
                )
                AttendrButton(
                    text = "Check Out",
                    onClick = { vm.markAttendance("out") },
                    modifier = Modifier.weight(1f),
                    enabled = state !is HomeState.Loading,
                    containerColor = AttendrError
                )
            }
        }

        (state as? HomeState.GeofenceNotSet)?.let { gns ->
            Spacer(Modifier.height(12.dp))
            AttendrCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        gns.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    AttendrButton(
                        text = if (notifyingAdmin) "Notifying…" else "Notify Admin",
                        onClick = { vm.notifyAdminGeofence() },
                        enabled = !notifyingAdmin
                    )
                }
            }
        }

        if (state is HomeState.Loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }
        if (successMsg.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(successMsg, color = Color(0xFF16A34A))
        }
        if (errorMsg.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Today's Log", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onViewCalendar) {
                Text("My Attendance →", style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.height(8.dp))

        if (logs.isEmpty()) {
            Text("No attendance marked today", color = Color.Gray)
        } else {
            logs.forEach { log ->
                AttendrCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (log.type == "in") "✅ Check In" else "🔴 Check Out")
                        Text(log.at.take(16).replace("T", " "))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        if (recent.isEmpty()) {
            Text("No recent activity", color = Color.Gray)
        } else {
            recent.forEach { rec ->
                val statusColor = when (rec.status.lowercase()) {
                    "present" -> Color(0xFF16A34A)
                    "late" -> Color(0xFFD97706)
                    "absent" -> Color(0xFFDC2626)
                    "leave", "leaves" -> Color(0xFF2563EB)
                    else -> Color.Gray
                }
                AttendrCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                formatShortDate(rec.date),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                rec.status.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "In: ${formatIsoTime(rec.checkInTime) ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                "Out: ${formatIsoTime(rec.checkOutTime) ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
