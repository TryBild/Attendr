package com.trybild.attendr.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.GeofenceItem
import com.trybild.attendr.data.repository.AuthRepository
import com.trybild.attendr.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(outerNavController: NavController) {
    val context = LocalContext.current
    val dataStore = remember { TokenDataStore(context) }
    val scope = rememberCoroutineScope()

    val employeeName by dataStore.employeeName.collectAsStateWithLifecycle(initialValue = null)
    val companyName  by dataStore.companyName.collectAsStateWithLifecycle(initialValue = null)

    val repo = remember { AuthRepository(context) }
    var geofences by remember { mutableStateOf<List<GeofenceItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        repo.getGeofences().getOrNull()?.geofences?.let { geofences = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // Avatar initials circle
        val initials = employeeName
            ?.trim()
            ?.split(" ")
            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
            ?.take(2)
            ?.joinToString("") ?: "?"

        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = AttendrNavy
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    initials,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            employeeName ?: "Employee",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = AttendrTextPrimary
        )
        if (!companyName.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                companyName!!,
                style = MaterialTheme.typography.bodyMedium,
                color = AttendrTextSecondary
            )
        }

        Spacer(Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AttendrSurface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                ProfileRow(label = "Name", value = employeeName ?: "—")
                HorizontalDivider(color = AttendrDivider, thickness = 0.5.dp)
                ProfileRow(label = "Organisation", value = companyName ?: "—")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Assigned geofences
        Text("Office Locations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AttendrTextPrimary)
        Spacer(Modifier.height(8.dp))
        if (geofences.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Text("No geofence assigned", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = AttendrTextSecondary)
            }
        } else {
            geofences.forEach { gf ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(24.dp))
                        Column {
                            Text(gf.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = AttendrTextPrimary)
                            Text("${gf.latitude}, ${gf.longitude} (${gf.radiusMeters.toInt()}m)", style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    dataStore.clearToken()
                    outerNavController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AttendrError),
            border = androidx.compose.foundation.BorderStroke(1.dp, AttendrError)
        ) {
            Text("Log Out", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AttendrTextSecondary)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = AttendrTextPrimary
        )
    }
}
