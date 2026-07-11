package com.trybild.attendr.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.trybild.attendr.data.model.AdminEmployeeItem
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEmployeesScreen(navController: NavController) {
    val vm: AdminEmployeesViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var selectedEmployee by remember { mutableStateOf<AdminEmployeeItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Employees",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!state.loading) {
                        Text(
                            "${state.employees.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AttendrTextSecondary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->

        when {
            state.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AttendrNavy)
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null,
                        tint = AttendrError, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(state.error!!, color = AttendrTextSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { vm.load() }) { Text("Retry") }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { vm.onQuery(it) },
                        placeholder = { Text("Search by name, department…", color = AttendrTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null,
                                tint = AttendrTextSecondary)
                        },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { vm.onQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear",
                                        tint = AttendrTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendrNavy,
                            unfocusedBorderColor = AttendrBorder,
                            focusedContainerColor = AttendrSurface,
                            unfocusedContainerColor = AttendrSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (state.filtered.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.People, contentDescription = null,
                                    tint = AttendrBorder, modifier = Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    if (state.query.isNotEmpty()) "No results for \"${state.query}\""
                                    else "No employees yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AttendrTextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.filtered, key = { it.id }) { emp ->
                                EmployeeRow(emp = emp, onClick = { selectedEmployee = emp })
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
    }

    if (selectedEmployee != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedEmployee = null },
            sheetState = sheetState,
            containerColor = AttendrSurface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AttendrBorder)
                )
            }
        ) {
            EmployeeDetailSheet(
                emp = selectedEmployee!!,
                onResetDevice = {
                    vm.resetDevice(selectedEmployee!!.id)
                    selectedEmployee = selectedEmployee!!.copy(deviceBound = false)
                }
            )
        }
    }
}

@Composable
private fun EmployeeRow(emp: AdminEmployeeItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvatarCircle(name = emp.fullName, size = 40)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    emp.fullName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = AttendrTextPrimary
                )
                val sub = listOfNotNull(
                    emp.department?.name,
                    emp.designation
                ).joinToString(" · ")
                if (sub.isNotEmpty()) {
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                }
            }
            if (!emp.employeeCode.isNullOrBlank() && emp.employeeCode != "-") {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AttendrNavy.copy(alpha = 0.08f)
                ) {
                    Text(
                        emp.employeeCode,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = AttendrNavy
                    )
                }
            }
        }
    }
}

@Composable
private fun EmployeeDetailSheet(emp: AdminEmployeeItem, onResetDevice: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarCircle(name = emp.fullName, size = 52)
            Column {
                Text(
                    emp.fullName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AttendrTextPrimary
                )
                if (!emp.designation.isNullOrBlank()) {
                    Text(emp.designation, style = MaterialTheme.typography.bodyMedium,
                        color = AttendrTextSecondary)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(
                        label = if (emp.isActive) "Active" else "Inactive",
                        color = if (emp.isActive) AttendrSuccess else AttendrError
                    )
                    if (emp.isVerified) {
                        StatusChip(label = "Verified", color = AttendrNavy)
                    }
                    StatusChip(
                        label = if (emp.deviceBound) "Device Bound" else "No Device",
                        color = if (emp.deviceBound) AttendrSuccess else AttendrTextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = AttendrDivider)
        Spacer(Modifier.height(16.dp))

        DetailRow(label = "Department",
            value = emp.department?.name?.takeIf { it.isNotBlank() } ?: "Not assigned")
        DetailRow(label = "Employee Code",
            value = emp.employeeCode?.takeIf { it.isNotBlank() && it != "-" } ?: "Not assigned")
        DetailRow(label = "Mobile", value = emp.mobile)
        DetailRow(label = "Joined",
            value = formatJoinDate(emp.joinedAt))

        if (emp.deviceBound) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onResetDevice,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AttendrError)
            ) {
                Icon(Icons.Default.PhonelinkErase, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reset Device Binding")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AttendrTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = AttendrTextPrimary)
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = color
        )
    }
}

@Composable
fun AvatarCircle(name: String, size: Int, photoUrl: String? = null) {
    val initials = name.trim().split("\\s+".toRegex())
        .filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it[0].uppercaseChar().toString() }
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(AttendrNavy.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                initials.ifEmpty { "?" },
                style = if (size >= 48) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        else MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = AttendrNavy
            )
        }
    }
}

private fun formatJoinDate(isoString: String?): String {
    if (isoString == null) return "—"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoString) ?: return isoString
        SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(date)
    } catch (e: Exception) {
        isoString.take(10)
    }
}
