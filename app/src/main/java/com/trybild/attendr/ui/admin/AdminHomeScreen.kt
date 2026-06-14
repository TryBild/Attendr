package com.trybild.attendr.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {
    val vm: AdminDashboardViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        containerColor = AttendrBackground,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out", tint = AttendrTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AttendrBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Greeting
            Column {
                Text(
                    "Hello, ${state.adminName.ifEmpty { "Admin" }}!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AttendrTextPrimary
                )
                if (state.orgName.isNotEmpty()) {
                    Text(
                        state.orgName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AttendrTextSecondary
                    )
                }
            }

            // Org ID card
            if (state.orgId.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Organization ID",
                            style = MaterialTheme.typography.labelMedium,
                            color = AttendrTextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                state.orgId,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = AttendrNavy
                            )
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(state.orgId))
                                    copied = true
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (copied) "Copied!" else "Copy")
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Share this ID with employees to join your organization",
                            style = MaterialTheme.typography.bodySmall,
                            color = AttendrTextSecondary
                        )
                    }
                }
            }

            // Today's stats
            Text(
                "Today's Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AttendrTextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("Total", "--", "Employees", Modifier.weight(1f))
                StatCard("Present", "--", "Today", Modifier.weight(1f))
                StatCard("Absent", "--", "Today", Modifier.weight(1f))
            }

            // Quick actions
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AttendrTextPrimary
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    icon = Icons.Default.People,
                    title = "Manage Employees",
                    subtitle = "View and manage your team"
                )
                QuickActionCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Attendance Records",
                    subtitle = "View attendance history"
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AttendrTextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AttendrNavy
            )
            Text(sub, style = MaterialTheme.typography.labelSmall, color = AttendrTextSecondary)
        }
    }
}

@Composable
private fun QuickActionCard(icon: ImageVector, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(24.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = AttendrTextPrimary
                )
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
            }
        }
    }
}
