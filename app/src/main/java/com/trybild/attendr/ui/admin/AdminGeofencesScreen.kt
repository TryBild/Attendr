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
import com.trybild.attendr.data.model.GeofenceItem
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGeofencesScreen(navController: NavController) {
    val vm: AdminGeofencesViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<GeofenceItem?>(null) }
    var deleteItem by remember { mutableStateOf<GeofenceItem?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    if (showAddDialog || editItem != null) {
        GeofenceFormDialog(
            initial = editItem,
            saving = state.saving,
            onDismiss = { showAddDialog = false; editItem = null },
            onSave = { name, lat, lng, radius ->
                val e = editItem
                if (e?._id != null) {
                    vm.update(e._id, name, lat, lng, radius)
                } else {
                    vm.create(name, lat, lng, radius)
                }
                showAddDialog = false; editItem = null
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
                    onClick = { showAddDialog = true },
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
                                onEdit = { editItem = gf },
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
private fun GeofenceCard(gf: GeofenceItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(gf.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = AttendrTextPrimary)
                Text("Lat: ${gf.latitude}, Lng: ${gf.longitude}", style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                Text("Radius: ${gf.radiusMeters.toInt()}m", style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                if (!gf.address.isNullOrBlank()) {
                    Text(gf.address, style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AttendrNavy, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AttendrError, modifier = Modifier.size(20.dp))
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
        title = { Text(if (initial != null) "Edit Geofence" else "Add Geofence") },
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
