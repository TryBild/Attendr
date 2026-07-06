package com.trybild.attendr.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.legal.AttendrUrls
import com.trybild.attendr.ui.legal.LegalMenuRow
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val vm: ProfileViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // surface save success / failure as a snackbar
    LaunchedEffect(state.saveMessage, state.saveError) {
        val msg = state.saveMessage ?: state.saveError
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            vm.consumeMessages()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    vm.logout {
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) { Text("Log Out", color = AttendrError) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showStartTimePicker) {
        val p = state.workStartTime.split(":").map { it.toIntOrNull() ?: 0 }
        ProfileTimePickerModal(
            title = "Work Start Time",
            initialHour = p.getOrElse(0) { 9 },
            initialMinute = p.getOrElse(1) { 0 },
            onDismiss = { showStartTimePicker = false },
            onConfirm = { h, m -> vm.setWorkStartTime("%02d:%02d".format(h, m)); showStartTimePicker = false }
        )
    }
    if (showEndTimePicker) {
        val p = state.workEndTime.split(":").map { it.toIntOrNull() ?: 0 }
        ProfileTimePickerModal(
            title = "Work End Time",
            initialHour = p.getOrElse(0) { 18 },
            initialMinute = p.getOrElse(1) { 0 },
            onDismiss = { showEndTimePicker = false },
            onConfirm = { h, m -> vm.setWorkEndTime("%02d:%02d".format(h, m)); showEndTimePicker = false }
        )
    }

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AttendrNavy, modifier = Modifier.size(28.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.loadError != null) {
                Spacer(Modifier.height(8.dp))
                ErrorBanner(state.loadError!!)
            }

            // ── Section 1: Account ────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader("Account")

            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // No photo-upload pipeline exists in the backend yet, so tapping
                // the avatar / camera badge just flags that the feature isn't wired.
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable { vm.notify("Photo upload isn't available yet") }
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(AttendrNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            nameInitials(state.adminName).ifEmpty { "A" },
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AttendrSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Change photo",
                            tint = AttendrNavy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            AttendrTextField(
                value = state.adminName,
                onValueChange = { vm.setAdminName(it) },
                label = "Full name",
                enabled = !state.saving
            )

            Spacer(Modifier.height(12.dp))
            ReadOnlyField(
                label = "Mobile number",
                value = state.mobile.ifBlank { "Not set" },
                helper = "Used for account contact — sign-in is via your admin email."
            )

            Spacer(Modifier.height(16.dp))
            AttendrButton(
                text = if (state.saving) "Saving…" else "Save",
                onClick = { vm.save("Profile updated") },
                enabled = state.accountDirty && !state.saving
            )

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = AttendrDivider)
            Spacer(Modifier.height(16.dp))

            // ── Section 2: Company (read-only) ────────────────────────────
            SectionHeader("Company")

            ReadOnlyField(label = "Company name", value = state.orgName.ifBlank { "—" })
            Spacer(Modifier.height(12.dp))

            // Org ID with copy button (same clipboard pattern as registration)
            var copied by remember { mutableStateOf(false) }
            Text(
                "Org ID",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(6.dp))
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = AttendrSurface),
                border = BorderStroke(1.dp, AttendrBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.orgId.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = AttendrNavy,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.orgId.isNotBlank()) {
                        TextButton(onClick = {
                            clipboardManager.setText(AnnotatedString(state.orgId))
                            copied = true
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (copied) "Copied!" else "Copy")
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = AttendrDivider)
            Spacer(Modifier.height(16.dp))

            // ── Section 3: Company settings ───────────────────────────────
            SectionHeader("Company settings")

            Text(
                "Work days",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    FilterChip(
                        selected = state.workDays.contains(day),
                        onClick = { if (!state.saving) vm.toggleWorkDay(day) },
                        label = { Text(day) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AttendrNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Work hours",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileTimeButton(
                    label = "Start Time",
                    value = formatTimeDisplay(state.workStartTime),
                    onClick = { if (!state.saving) showStartTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
                ProfileTimeButton(
                    label = "End Time",
                    value = formatTimeDisplay(state.workEndTime),
                    onClick = { if (!state.saving) showEndTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))
            AttendrButton(
                text = if (state.saving) "Saving…" else "Save settings",
                onClick = { vm.save("Company settings updated") },
                enabled = state.settingsDirty && state.settingsValid && !state.saving
            )

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = AttendrDivider)
            Spacer(Modifier.height(16.dp))

            // ── Section 4: Geofence locations ────────────────────────────
            SectionHeader("Geofence Locations")

            if (state.geofences.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrTextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No geofence set", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = AttendrTextPrimary)
                        Text("Add a location for attendance tracking.", style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                    }
                }
            } else {
                state.geofences.forEach { gf ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(24.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(gf.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = AttendrTextPrimary)
                                Text("Radius: ${gf.radiusMeters.toInt()}m", style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            AttendrButton(
                text = "Manage Geofences",
                onClick = { navController.navigate("admin_geofences") }
            )

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = AttendrDivider)
            Spacer(Modifier.height(16.dp))

            // ── Section 5: Billing ────────────────────────────────────────
            SectionHeader("Billing")
            AttendrButton(
                text = "Manage Subscription",
                onClick = { navController.navigate("subscription") }
            )

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = AttendrDivider)
            Spacer(Modifier.height(16.dp))

            // ── Section 6: App info + logout ──────────────────────────────
            SectionHeader("About")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("App version", style = MaterialTheme.typography.bodyMedium, color = AttendrTextSecondary)
                Text(
                    state.appVersion,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = AttendrTextPrimary
                )
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = AttendrDivider)
            Spacer(Modifier.height(16.dp))

            // ── Section 7: Legal ───────────────────────────────────────────
            SectionHeader("Legal")
            LegalMenuRow(label = "Privacy Policy", url = AttendrUrls.PRIVACY)
            LegalMenuRow(label = "Terms of Service", url = AttendrUrls.TERMS)
            LegalMenuRow(label = "Contact Support", url = AttendrUrls.CONTACT)
            LegalMenuRow(label = "Delete Account", url = AttendrUrls.DELETE_ACCOUNT, destructive = true)

            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AttendrError),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AttendrError)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Out")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
    }
}

// ── Private composables / helpers ───────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = AttendrTextPrimary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AttendrErrorBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            color = AttendrError,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String, helper: String = "") {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = AttendrTextPrimary
        )
        Spacer(Modifier.height(6.dp))
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF5F5F5)),
            border = BorderStroke(1.dp, AttendrBorder)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = AttendrTextSecondary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
            )
        }
        if (helper.isNotEmpty()) {
            Text(
                helper,
                style = MaterialTheme.typography.labelSmall,
                color = AttendrTextSecondary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun ProfileTimeButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AttendrBorder),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AttendrTextSecondary)
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AttendrTextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTimePickerModal(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.labelLarge) },
        text = { TimeInput(state = timeState) },
        confirmButton = { TextButton(onClick = { onConfirm(timeState.hour, timeState.minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatTimeDisplay(time: String): String {
    val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
    val h = parts.getOrElse(0) { 9 }
    val m = parts.getOrElse(1) { 0 }
    val ampm = if (h < 12) "AM" else "PM"
    val h12 = when (h) { 0 -> 12; in 13..23 -> h - 12; else -> h }
    return "%d:%02d %s".format(h12, m, ampm)
}

private fun nameInitials(name: String): String =
    name.trim().split("\\s+".toRegex())
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it[0].uppercaseChar().toString() }
