package com.trybild.attendr.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen() {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()
    val logs by vm.logs.collectAsState()
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var locationGranted by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        permLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is HomeState.CheckedIn  -> { successMsg = "Checked in ✅"; errorMsg = ""; vm.resetState() }
            is HomeState.CheckedOut -> { successMsg = "Checked out 🔴"; errorMsg = ""; vm.resetState() }
            is HomeState.Error -> { errorMsg = s.message; vm.resetState() }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text("Attendr", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))

        if (!locationGranted) {
            Button(onClick = {
                permLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Allow Location Permission")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { vm.markAttendance("in") },
                    modifier = Modifier.weight(1f),
                    enabled = state !is HomeState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) { Text("Check In") }

                Button(
                    onClick = { vm.markAttendance("out") },
                    modifier = Modifier.weight(1f),
                    enabled = state !is HomeState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Check Out") }
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
        Text("Today's Log", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (logs.isEmpty()) {
            Text("No attendance marked today", color = Color.Gray)
        } else {
            LazyColumn {
                items(logs) { log ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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
        }
    }
}
