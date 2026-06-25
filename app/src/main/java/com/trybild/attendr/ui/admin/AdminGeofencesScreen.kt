package com.trybild.attendr.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.trybild.attendr.data.model.GeofenceItem
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.theme.*
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGeofencesScreen(navController: NavController) {
    val vm: AdminGeofencesViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val mapsAvailable = remember { isMapsApiKeyValid(context) }

    var showManualDialog by remember { mutableStateOf(false) }
    var manualEditItem by remember { mutableStateOf<GeofenceItem?>(null) }
    var deleteItem by remember { mutableStateOf<GeofenceItem?>(null) }
    var showAddOptions by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    if (showManualDialog || manualEditItem != null) {
        GeofenceFormDialog(
            initial = manualEditItem,
            saving = state.saving,
            onDismiss = { showManualDialog = false; manualEditItem = null },
            onSave = { name, lat, lng, radius ->
                val e = manualEditItem
                if (e?._id != null) {
                    vm.update(e._id, name, lat, lng, radius)
                } else {
                    vm.create(name, lat, lng, radius)
                }
                showManualDialog = false; manualEditItem = null
            }
        )
    }

    if (deleteItem != null) {
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Delete Geofence") },
            text = { Text("Delete \"${deleteItem!!.name}\"? Employees won't be able to check in from this location.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteItem?._id?.let { vm.delete(it) }
                    deleteItem = null
                }) { Text("Delete", color = AttendrError) }
            },
            dismissButton = {
                TextButton(onClick = { deleteItem = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddOptions) {
        AlertDialog(
            onDismissRequest = { showAddOptions = false },
            title = { Text("Add Geofence") },
            text = {
                Column {
                    Text("How would you like to add a geofence?")
                    if (!mapsAvailable) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Map picker requires a Google Maps API key.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AttendrTextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAddOptions = false
                        navController.navigate("geofence_map_picker")
                    },
                    enabled = mapsAvailable
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (mapsAvailable) "Pick on Map" else "Pick on Map (unavailable)")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddOptions = false
                    showManualDialog = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enter Manually")
                }
            }
        )
    }

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Geofences") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddOptions = true },
                    containerColor = AttendrNavy,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add geofence")
                }
            }
        ) { padding ->
            when {
                state.loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AttendrNavy)
                    }
                }
                state.geofences.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrBorder, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No geofences yet", style = MaterialTheme.typography.bodyLarge, color = AttendrTextSecondary)
                        Text("Tap + to add your first office location", style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(padding)
                    ) {
                        items(state.geofences, key = { it._id ?: it.name }) { gf ->
                            GeofenceCard(
                                gf = gf,
                                mapsAvailable = mapsAvailable,
                                onEdit = {
                                    if (mapsAvailable) {
                                        val n = URLEncoder.encode(gf.name, "UTF-8")
                                        navController.navigate(
                                            "geofence_map_picker?lat=${gf.latitude}&lng=${gf.longitude}&name=$n&radius=${gf.radiusMeters.toInt()}&id=${gf._id}"
                                        )
                                    } else {
                                        manualEditItem = gf
                                    }
                                },
                                onDelete = { deleteItem = gf }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeofenceCard(gf: GeofenceItem, mapsAvailable: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(28.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(gf.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = AttendrTextPrimary)
                    Text("%.6f, %.6f".format(gf.latitude, gf.longitude), style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                    Text("Radius: ${gf.radiusMeters.toInt()}m", style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                    if (!gf.address.isNullOrBlank()) {
                        Text(gf.address, style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (mapsAvailable) Icons.Default.Map else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (mapsAvailable) "Edit on Map" else "Edit", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AttendrError),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AttendrError),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun GeofenceFormDialog(
    initial: GeofenceItem?,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, lat: Double, lng: Double, radius: Double) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var lat by remember { mutableStateOf(initial?.latitude?.toString() ?: "") }
    var lng by remember { mutableStateOf(initial?.longitude?.toString() ?: "") }
    var radius by remember { mutableStateOf(initial?.radiusMeters?.toInt()?.toString() ?: "100") }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(if (initial != null) "Edit Geofence" else "Add Geofence (Manual)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AttendrTextField(value = name, onValueChange = { name = it }, label = "Name (e.g. Main Office)")
                AttendrTextField(value = lat, onValueChange = { lat = it }, label = "Latitude", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                AttendrTextField(value = lng, onValueChange = { lng = it }, label = "Longitude", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                AttendrTextField(value = radius, onValueChange = { radius = it }, label = "Radius (meters)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val latD = lat.toDoubleOrNull() ?: return@TextButton
                    val lngD = lng.toDoubleOrNull() ?: return@TextButton
                    val radD = radius.toDoubleOrNull() ?: 100.0
                    onSave(name.trim(), latD, lngD, radD)
                },
                enabled = name.isNotBlank() && lat.toDoubleOrNull() != null && lng.toDoubleOrNull() != null && !saving
            ) { Text(if (saving) "Saving..." else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
        }
    )
}
