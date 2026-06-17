package com.trybild.attendr.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.trybild.attendr.data.api.RetrofitClient
import com.trybild.attendr.data.local.TokenDataStore
import com.google.gson.Gson
import com.trybild.attendr.data.model.AttendanceLog
import com.trybild.attendr.data.model.AttendanceResponse
import com.trybild.attendr.data.model.GeofenceItem
import com.trybild.attendr.data.model.MarkAttendanceBody
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.*

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class CheckedIn(val at: String) : HomeState()
    data class CheckedOut(val at: String) : HomeState()
    data class Error(val message: String) : HomeState()
}

sealed class GeofenceBadge {
    object Loading : GeofenceBadge()
    object InsideZone : GeofenceBadge()
    data class DistanceAway(val meters: Int) : GeofenceBadge()
    object Denied : GeofenceBadge()
    object Unavailable : GeofenceBadge()
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = TokenDataStore(app)
    private val api = RetrofitClient.api
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(app)

    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)
    val state: StateFlow<HomeState> = _state

    private val _logs = MutableStateFlow<List<AttendanceLog>>(emptyList())
    val logs: StateFlow<List<AttendanceLog>> = _logs

    private val _badge = MutableStateFlow<GeofenceBadge>(GeofenceBadge.Loading)
    val badge: StateFlow<GeofenceBadge> = _badge

    private var permissionGranted = false
    private var loadedGeofences: List<GeofenceItem> = emptyList()
    private var locationCallback: LocationCallback? = null

    init {
        loadTodayLogs()
        loadGeofences()
    }

    fun loadTodayLogs() {
        viewModelScope.launch {
            try {
                val t = dataStore.token.firstOrNull() ?: return@launch
                val res = api.getTodayLogs("Bearer $t")
                if (res.isSuccessful) {
                    val body = res.body()
                    val entries = mutableListOf<AttendanceLog>()
                    if (body?.checkInTime != null)  entries.add(AttendanceLog("in",  body.checkInTime))
                    if (body?.checkOutTime != null) entries.add(AttendanceLog("out", body.checkOutTime))
                    _logs.value = entries
                }
            } catch (_: Exception) {}
        }
    }

    fun onLocationPermissionGranted() {
        permissionGranted = true
        if (loadedGeofences.isNotEmpty()) startLocationUpdates()
    }

    fun onLocationPermissionDenied() {
        permissionGranted = false
        _badge.value = GeofenceBadge.Denied
    }

    private fun loadGeofences() {
        viewModelScope.launch {
            try {
                val t = dataStore.token.firstOrNull() ?: return@launch
                val res = api.getGeofences("Bearer $t")
                if (res.isSuccessful && res.body()?.ok == true) {
                    loadedGeofences = res.body()?.geofences ?: emptyList()
                    if (loadedGeofences.isEmpty()) {
                        _badge.value = GeofenceBadge.Unavailable
                    } else if (permissionGranted) {
                        startLocationUpdates()
                    }
                } else {
                    _badge.value = GeofenceBadge.Unavailable
                }
            } catch (_: Exception) {
                _badge.value = GeofenceBadge.Unavailable
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .build()
        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                updateBadge(loc.latitude, loc.longitude)
            }
        }
        locationCallback = cb
        try {
            fusedLocation.requestLocationUpdates(request, cb, Looper.getMainLooper())
        } catch (_: SecurityException) {
            _badge.value = GeofenceBadge.Denied
        }
    }

    private fun updateBadge(lat: Double, lng: Double) {
        var nearestDist = Double.MAX_VALUE
        var inside = false
        for (gf in loadedGeofences) {
            val dist = haversineDistance(lat, lng, gf.latitude, gf.longitude)
            if (dist <= gf.radiusMeters) { inside = true; break }
            if (dist < nearestDist) nearestDist = dist
        }
        _badge.value = if (inside) GeofenceBadge.InsideZone
                       else GeofenceBadge.DistanceAway(nearestDist.roundToInt())
    }

    // Mirrors server's haversine in backend/src/utils/geo.js (R = 6_371_000 m)
    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2.0 * R * asin(sqrt(a))
    }

    @SuppressLint("MissingPermission")
    fun markAttendance(type: String) {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val t = dataStore.token.firstOrNull() ?: run {
                    _state.value = HomeState.Error("Not logged in")
                    return@launch
                }
                val cts = CancellationTokenSource()
                val location = fusedLocation.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, cts.token
                ).await()
                val action = if (type == "in") "checkin" else "checkout"
                val res = api.markAttendance(
                    "Bearer $t",
                    MarkAttendanceBody(action, location?.latitude, location?.longitude)
                )
                if (res.isSuccessful && res.body()?.ok == true) {
                    val time = res.body()?.time ?: ""
                    _state.value = if (type == "in") HomeState.CheckedIn(time)
                                   else HomeState.CheckedOut(time)
                    loadTodayLogs()
                } else {
                    // 4xx bodies are in errorBody(), not body()
                    val errMsg = runCatching {
                        Gson().fromJson(res.errorBody()?.string(), AttendanceResponse::class.java)?.error
                    }.getOrNull() ?: "Failed (${res.code()})"
                    _state.value = HomeState.Error(errMsg)
                }
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Error")
            }
        }
    }

    fun resetState() { _state.value = HomeState.Idle }

    override fun onCleared() {
        super.onCleared()
        locationCallback?.let { fusedLocation.removeLocationUpdates(it) }
    }
}
