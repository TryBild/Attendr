package com.trybild.attendr.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminSetupScreen(navController: NavController) {
    val vm: AdminSetupViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    // Setup is compulsory — intercept back press
    BackHandler {}

    var industry by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf("Mon", "Tue", "Wed", "Thu", "Fri")) }
    var workStartTime by remember { mutableStateOf("09:00") }
    var workEndTime by remember { mutableStateOf("18:00") }
    var timezone by remember { mutableStateOf("Asia/Kolkata") }
    var referralSource by remember { mutableStateOf("") }

    var industryExpanded by remember { mutableStateOf(false) }
    var timezoneExpanded by remember { mutableStateOf(false) }
    var referralExpanded by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var industryError by remember { mutableStateOf("") }
    var daysError by remember { mutableStateOf("") }
    var timeError by remember { mutableStateOf("") }
    var referralError by remember { mutableStateOf("") }
    var serverError by remember { mutableStateOf("") }

    val industries = listOf("IT / Software", "Manufacturing", "Retail", "Logistics", "Other")
    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val timezones = listOf(
        "Asia/Kolkata" to "India Standard Time (IST)",
        "Asia/Dubai" to "Gulf Standard Time (GST)",
        "Asia/Singapore" to "Singapore Time (SGT)",
        "Europe/London" to "Greenwich Mean Time (GMT)",
    )
    val referralOptions = listOf("Google Search", "Friend / Colleague", "Social Media", "Other")

    LaunchedEffect(state) {
        when (state) {
            is AdminSetupState.Success -> {
                navController.navigate("admin_home") {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            is AdminSetupState.Error -> {
                serverError = (state as AdminSetupState.Error).message
                vm.resetState()
            }
            else -> {}
        }
    }

    fun validate(): Boolean {
        var valid = true
        industryError = if (industry.isBlank()) { valid = false; "Select an industry" } else ""
        daysError = if (selectedDays.isEmpty()) { valid = false; "Select at least one day" } else ""
        timeError = if (workEndTime <= workStartTime) {
            valid = false; "End time must be after start time"
        } else ""
        referralError = if (referralSource.isBlank()) { valid = false; "Select an option" } else ""
        return valid
    }

    fun formatDisplay(time: String): String {
        val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
        val h = parts.getOrElse(0) { 9 }
        val m = parts.getOrElse(1) { 0 }
        val ampm = if (h < 12) "AM" else "PM"
        val h12 = when (h) { 0 -> 12; in 13..23 -> h - 12; else -> h }
        return "%d:%02d %s".format(h12, m, ampm)
    }

    if (showStartTimePicker) {
        val p = workStartTime.split(":").map { it.toIntOrNull() ?: 0 }
        TimePickerModal(
            title = "Work Start Time",
            initialHour = p.getOrElse(0) { 9 },
            initialMinute = p.getOrElse(1) { 0 },
            onDismiss = { showStartTimePicker = false },
            onConfirm = { h, m ->
                workStartTime = "%02d:%02d".format(h, m)
                timeError = ""
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        val p = workEndTime.split(":").map { it.toIntOrNull() ?: 0 }
        TimePickerModal(
            title = "Work End Time",
            initialHour = p.getOrElse(0) { 18 },
            initialMinute = p.getOrElse(1) { 0 },
            onDismiss = { showEndTimePicker = false },
            onConfirm = { h, m ->
                workEndTime = "%02d:%02d".format(h, m)
                timeError = ""
                showEndTimePicker = false
            }
        )
    }

    val isLoading = state is AdminSetupState.Loading

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Almost there!",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = AttendrNavy
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tell us more about your organization",
                style = MaterialTheme.typography.bodyLarge,
                color = AttendrTextSecondary,
                textAlign = TextAlign.Center
            )

            if (serverError.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = AttendrErrorBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        serverError,
                        color = AttendrError,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // 1. Industry
            FieldLabel("Industry / Sector")
            Spacer(Modifier.height(6.dp))
            SetupDropdown(
                value = industry,
                options = industries,
                placeholder = "Select industry",
                expanded = industryExpanded,
                onExpandedChange = { if (!isLoading) industryExpanded = it },
                onSelect = { industry = it; industryError = ""; industryExpanded = false },
                errorText = industryError,
                enabled = !isLoading
            )

            Spacer(Modifier.height(20.dp))

            // 2. Work Days
            FieldLabel("Work Days")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allDays.forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = {
                            if (!isLoading) {
                                selectedDays = if (selectedDays.contains(day)) selectedDays - day
                                              else selectedDays + day
                                daysError = ""
                            }
                        },
                        label = { Text(day) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AttendrNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            if (daysError.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    daysError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // 3 & 4. Work Hours
            FieldLabel("Work Hours")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeButton(
                    label = "Start Time",
                    value = formatDisplay(workStartTime),
                    onClick = { if (!isLoading) showStartTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
                TimeButton(
                    label = "End Time",
                    value = formatDisplay(workEndTime),
                    onClick = { if (!isLoading) showEndTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }
            if (timeError.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    timeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // 5. Timezone
            FieldLabel("Timezone")
            Spacer(Modifier.height(6.dp))
            val timezoneLabel = timezones.find { it.first == timezone }?.second ?: timezone
            SetupDropdown(
                value = timezoneLabel,
                options = timezones.map { it.second },
                placeholder = "Select timezone",
                expanded = timezoneExpanded,
                onExpandedChange = { if (!isLoading) timezoneExpanded = it },
                onSelect = { label ->
                    timezone = timezones.find { it.second == label }?.first ?: label
                    timezoneExpanded = false
                },
                errorText = "",
                enabled = !isLoading
            )
            Text(
                "Most Indian businesses use IST — change only if your team works from another region.",
                style = MaterialTheme.typography.labelSmall,
                color = AttendrTextSecondary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // 6. Referral Source
            FieldLabel("How did you hear about us?")
            Spacer(Modifier.height(6.dp))
            SetupDropdown(
                value = referralSource,
                options = referralOptions,
                placeholder = "Select an option",
                expanded = referralExpanded,
                onExpandedChange = { if (!isLoading) referralExpanded = it },
                onSelect = { referralSource = it; referralError = ""; referralExpanded = false },
                errorText = referralError,
                enabled = !isLoading
            )

            Spacer(Modifier.height(32.dp))

            AttendrButton(
                text = if (isLoading) "Saving…" else "Complete Setup",
                onClick = {
                    serverError = ""
                    if (validate()) {
                        vm.submitSetup(
                            industry = industry,
                            workDays = selectedDays.toList(),
                            workStartTime = workStartTime,
                            workEndTime = workEndTime,
                            timezone = timezone,
                            referralSource = referralSource
                        )
                    }
                },
                enabled = !isLoading
            )

            Spacer(Modifier.height(24.dp))
        }
    }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = AttendrTextPrimary,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TimeButton(
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
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AttendrTextSecondary
            )
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
private fun SetupDropdown(
    value: String,
    options: List<String>,
    placeholder: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    errorText: String,
    enabled: Boolean = true
) {
    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(placeholder, color = AttendrTextSecondary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                isError = errorText.isNotEmpty(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AttendrNavy,
                    unfocusedBorderColor = AttendrBorder
                ),
                enabled = enabled
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
        if (errorText.isNotEmpty()) {
            Text(
                errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerModal(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = AttendrSurface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(20.dp))
                TimeInput(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
                }
            }
        }
    }
}
