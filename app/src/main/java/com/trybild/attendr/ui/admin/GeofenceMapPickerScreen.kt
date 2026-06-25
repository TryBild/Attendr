package com.trybild.attendr.ui.admin

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceMapPickerScreen(
    navController: NavController,
    initialLat: Double?,
    initialLng: Double?,
    initialName: String?,
    initialRadius: Float?,
    geofenceId: String?,
    vm: AdminGeofencesViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }

    val defaultIndia = LatLng(20.5937, 78.9629)
    var pinPosition by remember {
        mutableStateOf(
            if (initialLat != null && initialLng != null) LatLng(initialLat, initialLng)
            else null
        )
    }
    var name by remember { mutableStateOf(initialName ?: "") }
    var radius by remember { mutableFloatStateOf(initialRadius ?: 100f) }
    var saving by remember { mutableStateOf(false) }
    var locationLoaded by remember { mutableStateOf(pinPosition != null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pinPosition ?: defaultIndia, if (pinPosition != null) 17f else 5f)
    }

    LaunchedEffect(Unit) {
        if (pinPosition != null) return@LaunchedEffect
        try {
            val loc = fusedLocation.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
            ).await()
            if (loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)
                pinPosition = pos
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 17f))
            }
        } catch (_: Exception) {}
        locationLoaded = true
    }

    val isEdit = geofenceId != null

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            ),
            properties = MapProperties(isMyLocationEnabled = true),
            onMapClick = { latLng ->
                pinPosition = latLng
            }
        ) {
            pinPosition?.let { pos ->
                Marker(
                    state = MarkerState(position = pos),
                    title = name.ifBlank { "Office Location" },
                    draggable = true,
                    onClick = { false }
                )
                Circle(
                    center = pos,
                    radius = radius.toDouble(),
                    strokeColor = AttendrNavy.copy(alpha = 0.6f),
                    strokeWidth = 2f,
                    fillColor = AttendrNavy.copy(alpha = 0.12f)
                )
            }
        }

        // Back button
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .size(40.dp)
                .background(AttendrSurface, CircleShape)
                .border(1.dp, AttendrBorder, CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AttendrTextPrimary, modifier = Modifier.size(20.dp))
        }

        // My Location button
        FloatingActionButton(
            onClick = {
                scope.launch {
                    try {
                        val loc = fusedLocation.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
                        ).await()
                        if (loc != null) {
                            val pos = LatLng(loc.latitude, loc.longitude)
                            pinPosition = pos
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 17f))
                        }
                    } catch (_: Exception) {}
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 320.dp),
            containerColor = AttendrSurface,
            contentColor = AttendrNavy,
            shape = CircleShape
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location")
        }

        // Bottom sheet
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = AttendrSurface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(AttendrBorder, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    if (isEdit) "Edit Geofence" else "Add Geofence",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AttendrTextPrimary
                )

                // Coordinates display
                pinPosition?.let { pos ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(18.dp))
                        Text(
                            "%.6f, %.6f".format(pos.latitude, pos.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = AttendrTextSecondary
                        )
                    }
                } ?: Text(
                    "Tap on the map to place a pin",
                    style = MaterialTheme.typography.bodySmall,
                    color = AttendrTextHint
                )

                AttendrTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Location Name",
                    placeholder = "e.g. Main Office"
                )

                // Radius slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Radius", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = AttendrTextPrimary)
                        Text("${radius.toInt()}m", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = AttendrNavy)
                    }
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 50f..1000f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = AttendrNavy,
                            activeTrackColor = AttendrNavy,
                            inactiveTrackColor = AttendrBorder
                        )
                    )
                }

                AttendrButton(
                    text = if (saving) "Saving..." else "Save Geofence",
                    onClick = {
                        val pos = pinPosition ?: return@AttendrButton
                        saving = true
                        if (isEdit && geofenceId != null) {
                            vm.update(geofenceId, name.trim(), pos.latitude, pos.longitude, radius.toDouble())
                        } else {
                            vm.create(name.trim(), pos.latitude, pos.longitude, radius.toDouble())
                        }
                        navController.navigateUp()
                    },
                    enabled = pinPosition != null && name.isNotBlank() && !saving
                )
            }
        }
    }
}
