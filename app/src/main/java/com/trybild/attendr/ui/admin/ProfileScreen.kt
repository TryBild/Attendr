package com.trybild.attendr.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.components.IndustrialCard
import com.trybild.attendr.ui.components.IndustrialCardRow
import com.trybild.attendr.ui.components.IndustrialSectionLabel
import com.trybild.attendr.ui.legal.AttendrUrls
import com.trybild.attendr.ui.legal.LegalMenuRow
import com.trybild.attendr.ui.theme.*
import com.trybild.attendr.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val vm: ProfileViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.Default) { ImageUtils.compressImageFromUri(context, uri) }
                    vm.uploadPhoto(bytes)
                } catch (e: ImageUtils.ImageReadException) {
                    vm.notify(e.message ?: "Could not read selected image")
                }
            }
        }
    }

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

    Box(modifier = Modifier.fillMaxSize().background(IndustrialPageBackground)) {
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(StitchSurface)
                    .drawBehind {
                        drawLine(
                            color = StitchOutlineVariant,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StitchPrimary)
                }
                Text("Settings", style = StitchHeadlineMd, color = StitchPrimary)
            }
        },
        bottomBar = {
            AdminBottomNavBar(
                currentRoute = "admin_profile",
                onNavigate = { route ->
                    navController.navigate(route) { popUpTo("admin_home") { saveState = true } }
                }
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
                .widthIn(max = 640.dp)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.loadError != null) {
                Spacer(Modifier.height(8.dp))
                ErrorBanner(state.loadError!!)
            }

            // ── Section 1: Account / Personal Information ─────────────────
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable(enabled = !state.uploadingPhoto) {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(StitchSurfaceContainer)
                            .border(1.5.dp, StitchOutlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.photoUrl != null) {
                            AsyncImage(
                                model = state.photoUrl,
                                contentDescription = "Profile photo",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                nameInitials(state.adminName).ifEmpty { "A" },
                                style = StitchHeadlineLg,
                                color = StitchPrimary
                            )
                        }
                        if (state.uploadingPhoto) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StitchPrimary)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            IndustrialSectionLabel("Personal Information")
            IndustrialCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    AttendrTextField(
                        value = state.adminName,
                        onValueChange = { vm.setAdminName(it) },
                        label = "Full name",
                        enabled = !state.saving
                    )

                    Spacer(Modifier.height(12.dp))
                    ReadOnlyField(
                        label = "Email",
                        value = state.email.ifBlank { "Not set" },
                        helper = "Your sign-in email — contact support to change it."
                    )

                    Spacer(Modifier.height(12.dp))
                    ReadOnlyField(
                        label = "Mobile number",
                        value = state.mobile.ifBlank { "Not set" },
                        helper = "Used for account contact."
                    )

                    Spacer(Modifier.height(16.dp))
                    AttendrButton(
                        text = if (state.saving) "Saving…" else "Save",
                        onClick = { vm.save("Profile updated") },
                        enabled = state.accountDirty && !state.saving
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Section 2+3: Company Settings ──────────────────────────────
            var copied by remember { mutableStateOf(false) }
            IndustrialSectionLabel("Company Settings")
            IndustrialCard {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("COMPANY NAME", style = StitchLabelSm.copy(letterSpacing = 0.05.em), color = StitchOutline)
                    Text(state.orgName.ifBlank { "—" }, style = StitchBodyLg, color = StitchOnSurface)
                }
                HorizontalDivider(color = StitchOutlineVariant, thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ORGANIZATION ID", style = StitchLabelSm.copy(letterSpacing = 0.05.em), color = StitchOutline)
                        Text(
                            state.orgId.ifBlank { "—" },
                            style = StitchBodyMd.copy(fontFamily = FontFamily.Monospace),
                            color = StitchOnSurface
                        )
                    }
                    if (state.orgId.isNotBlank()) {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(state.orgId))
                            copied = true
                        }) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = if (copied) "Copied" else "Copy Organization ID",
                                tint = StitchPrimary
                            )
                        }
                    }
                }
                HorizontalDivider(color = StitchOutlineVariant, thickness = 1.dp)
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WORK DAYS", style = StitchLabelSm.copy(letterSpacing = 0.05.em), color = StitchOutline)
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
                                    selectedContainerColor = StitchPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("WORK SCHEDULE", style = StitchLabelSm.copy(letterSpacing = 0.05.em), color = StitchOutline)
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
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Section 4: Geofence locations ────────────────────────────
            IndustrialSectionLabel("Geofence Locations")
            IndustrialCard {
                if (state.geofences.isEmpty()) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = StitchOutline, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No geofence set", style = StitchBodyMd, color = StitchOnSurface)
                        Text("Add a location for attendance tracking.", style = StitchLabelSm, color = StitchOutline)
                    }
                } else {
                    state.geofences.forEachIndexed { index, gf ->
                        if (index > 0) HorizontalDivider(color = StitchOutlineVariant, thickness = 1.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = StitchOnSurfaceVariant)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(gf.name, style = StitchBodyLg, color = StitchOnSurface)
                                Text("Radius: ${gf.radiusMeters.toInt()}m", style = StitchLabelSm, color = StitchOutline)
                            }
                        }
                    }
                }
                HorizontalDivider(color = StitchOutlineVariant, thickness = 1.dp)
                IndustrialCardRow(
                    icon = Icons.Default.LocationOn,
                    label = "Manage Geofences",
                    onClick = { navController.navigate("admin_geofences") },
                    trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = StitchOutline) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Section 5+6: App Settings (Billing + About) ────────────────
            IndustrialSectionLabel("App Settings")
            IndustrialCard {
                IndustrialCardRow(
                    icon = Icons.Default.CreditCard,
                    label = "Manage Subscription",
                    onClick = { navController.navigate("subscription") },
                    trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = StitchOutline) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Section 7: Legal ───────────────────────────────────────────
            IndustrialSectionLabel("Legal")
            IndustrialCard {
                LegalMenuRow(label = "Privacy Policy", url = AttendrUrls.PRIVACY)
                HorizontalDivider(color = StitchOutlineVariant, thickness = 1.dp)
                LegalMenuRow(label = "Terms of Service", url = AttendrUrls.TERMS)
                HorizontalDivider(color = StitchOutlineVariant, thickness = 1.dp)
                LegalMenuRow(label = "Contact Support", url = AttendrUrls.CONTACT)
                HorizontalDivider(color = StitchOutlineVariant, thickness = 1.dp)
                LegalMenuRow(label = "Delete Account", url = AttendrUrls.DELETE_ACCOUNT, destructive = true)
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = StitchShapeXl,
                border = BorderStroke(1.5.dp, StitchError),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = StitchError)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("SIGN OUT", style = StitchLabelBold)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Version ${state.appVersion}",
                style = StitchLabelSm,
                color = StitchOutline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))
        }
    }
    }
}

// Scoped to this screen only - admin has no shared shell/bottom-nav owner
// (unlike EmployeeShell), so adding it here doesn't cascade to admin_home
// or admin_attendance, which stay on the old theme. Only real destinations
// are included: no "Reports" tab, since no dedicated screen route exists
// for it - the mockup's 4th tab has nothing honest to navigate to.
@Composable
private fun AdminBottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    data class NavTab(val route: String, val label: String, val outlined: ImageVector, val filled: ImageVector)
    val tabs = listOf(
        NavTab("admin_home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
        NavTab("admin_attendance", "Attendance", Icons.Outlined.Fingerprint, Icons.Filled.Fingerprint),
        NavTab("admin_profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(StitchSurface)
            .drawBehind {
                drawLine(
                    color = StitchOutlineVariant,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val active = currentRoute == tab.route
            val tint = if (active) StitchPrimary else StitchOnSurfaceVariant
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(enabled = !active) { onNavigate(tab.route) }
            ) {
                Icon(if (active) tab.filled else tab.outlined, contentDescription = tab.label, tint = tint)
                Text(tab.label, style = StitchLabelSm, color = tint)
            }
        }
    }
}

// ── Private composables / helpers ───────────────────────────────────────────

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
