package com.trybild.attendr.ui.admin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.*
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

private const val TAG = "AttendrMap"

fun isMapsApiKeyValid(context: android.content.Context): Boolean {
    val key = getMapsApiKey(context)
    return !key.isNullOrBlank() && key != "PLACEHOLDER_KEY"
}

fun getMapsApiKey(context: android.content.Context): String? {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA
        )
        appInfo.metaData?.getString("com.google.android.geo.API_KEY")
    } catch (_: Exception) {
        null
    }
}

@SuppressLint("MissingPermission")
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

    if (!isMapsApiKeyValid(context)) {
        MapsNotConfiguredScreen(onBack = { navController.navigateUp() })
        return
    }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = context as? Activity

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (!granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Location permission denied. Enable it in Settings to center the map.",
                    actionLabel = "Settings",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val key = getMapsApiKey(context)
        Log.i(TAG, "API key: ${key?.take(8)}...${key?.takeLast(4)} | package: ${context.packageName}")

        if (!hasLocationPermission) {
            permLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

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
    var mapLoaded by remember { mutableStateOf(false) }
    var mapTimedOut by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf(emptyList<AutocompletePrediction>()) }
    var searchError by remember { mutableStateOf<String?>(null) }

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, getMapsApiKey(context) ?: "")
        }
        Places.createClient(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pinPosition ?: defaultIndia, if (pinPosition != null) 17f else 5f)
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission || pinPosition != null) return@LaunchedEffect
        try {
            val loc = fusedLocation.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
            ).await()
            if (loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)
                pinPosition = pos
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 17f))
                Log.i(TAG, "Location acquired: ${pos.latitude}, ${pos.longitude}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Location fetch failed: ${e.message}")
        }
    }

    // If map tiles don't load after 8 seconds, show the fallback banner
    LaunchedEffect(Unit) {
        delay(8000)
        if (!mapLoaded) {
            mapTimedOut = true
            Log.w(TAG, "Map tiles did not load within 8s — likely API key/billing issue")
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length < 3) {
            predictions = emptyList()
            searchError = null
            return@LaunchedEffect
        }
        delay(300)
        try {
            val biasCenter = pinPosition ?: cameraPositionState.position.target
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(searchQuery)
                .setCountries("IN")
                .setLocationBias(
                    RectangularBounds.newInstance(
                        LatLng(biasCenter.latitude - 0.5, biasCenter.longitude - 0.5),
                        LatLng(biasCenter.latitude + 0.5, biasCenter.longitude + 0.5)
                    )
                )
                .build()
            val response = placesClient.findAutocompletePredictions(request).await()
            predictions = response.autocompletePredictions
            searchError = null
        } catch (e: Exception) {
            predictions = emptyList()
            searchError = "Couldn't find places. Check your internet connection."
            Log.w(TAG, "Autocomplete failed: ${e.message}")
        }
    }

    val isEdit = geofenceId != null
    val effectivePin = pinPosition ?: cameraPositionState.position.target

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            ),
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            onMapClick = { latLng ->
                pinPosition = latLng
                scope.launch(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)?.firstOrNull()
                        val label = address?.let {
                            listOfNotNull(it.featureName, it.subLocality ?: it.locality).distinct().joinToString(", ")
                        }
                        if (!label.isNullOrBlank()) {
                            withContext(Dispatchers.Main) {
                                if (name.isBlank()) name = label
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Reverse geocode failed: ${e.message}")
                    }
                }
            },
            onMapLoaded = {
                mapLoaded = true
                mapTimedOut = false
                Log.i(TAG, "Map tiles loaded successfully")
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

        // Search bar + warning banner overlay
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 56.dp, end = 8.dp, top = 8.dp)
                .align(Alignment.TopStart)
        ) {
            if (mapTimedOut && !mapLoaded) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            "Map tiles not loading",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF92400E)
                        )
                        Text(
                            "Check Google Cloud Console: enable Maps SDK for Android, verify API key restrictions, enable billing.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            "You can still save using \"Use map center\" below.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                AttendrTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "",
                    placeholder = "Search location",
                    leadingContent = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AttendrTextHint, modifier = Modifier.size(20.dp))
                    },
                    trailingContent = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = { searchQuery = ""; predictions = emptyList(); searchError = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = AttendrTextHint, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null
                )
            }

            if (searchError != null && predictions.isEmpty()) {
                Card(
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        searchError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = AttendrTextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            if (predictions.isNotEmpty()) {
                Card(
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        predictions.take(5).forEach { prediction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            try {
                                                val placeFields = listOf(Place.Field.LAT_LNG)
                                                val fetchRequest = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                                                val place = placesClient.fetchPlace(fetchRequest).await().place
                                                place.latLng?.let { latLng ->
                                                    pinPosition = latLng
                                                    searchQuery = prediction.getPrimaryText(null).toString()
                                                    if (name.isBlank()) name = prediction.getPrimaryText(null).toString()
                                                    predictions = emptyList()
                                                    focusManager.clearFocus()
                                                    cameraPositionState.animate(
                                                        CameraUpdateFactory.newLatLngZoom(latLng, 17f)
                                                    )
                                                }
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrTextHint, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        prediction.getPrimaryText(null).toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AttendrTextPrimary
                                    )
                                    Text(
                                        prediction.getSecondaryText(null).toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AttendrTextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (hasLocationPermission) {
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
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 300.dp)
        )

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

                // Coordinates display with fallback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(18.dp))
                        Text(
                            "%.6f, %.6f".format(effectivePin.latitude, effectivePin.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (pinPosition != null) AttendrTextSecondary else AttendrTextHint
                        )
                    }
                    if (pinPosition == null) {
                        TextButton(onClick = { pinPosition = cameraPositionState.position.target }) {
                            Text("Use map center", style = MaterialTheme.typography.labelSmall, color = AttendrNavy)
                        }
                    }
                }

                AttendrTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Location Name",
                    placeholder = "e.g. Main Office"
                )

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
                        saving = true
                        val pos = pinPosition ?: cameraPositionState.position.target
                        if (isEdit && geofenceId != null) {
                            vm.update(geofenceId, name.trim(), pos.latitude, pos.longitude, radius.toDouble())
                        } else {
                            vm.create(name.trim(), pos.latitude, pos.longitude, radius.toDouble())
                        }
                        navController.navigateUp()
                    },
                    enabled = name.isNotBlank() && !saving
                )
            }
        }
    }
}

@Composable
private fun MapsNotConfiguredScreen(onBack: () -> Unit) {
    AttendrBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Map, contentDescription = null, tint = AttendrBorder, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                "Google Maps not configured",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AttendrTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "A valid Google Maps API key is required. Use \"Enter Manually\" to add geofences.",
                style = MaterialTheme.typography.bodyMedium,
                color = AttendrTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            AttendrButton(text = "Go Back", onClick = onBack)
        }
    }
}
