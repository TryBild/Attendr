package com.trybild.attendr.ui.home

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.trybild.attendr.data.api.RetrofitClient
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.AttendanceLog
import com.trybild.attendr.data.model.MarkAttendanceBody
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class CheckedIn(val at: String) : HomeState()
    data class CheckedOut(val at: String) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = TokenDataStore(app)
    private val api = RetrofitClient.api
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(app)

    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)
    val state: StateFlow<HomeState> = _state

    private val _logs = MutableStateFlow<List<AttendanceLog>>(emptyList())
    val logs: StateFlow<List<AttendanceLog>> = _logs

    init { loadTodayLogs() }

    fun loadTodayLogs() {
        viewModelScope.launch {
            try {
                val t = dataStore.token.firstOrNull() ?: return@launch
                val res = api.getTodayLogs("Bearer $t")
                if (res.isSuccessful) _logs.value = res.body()?.logs ?: emptyList()
            } catch (_: Exception) {}
        }
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
                val res = api.markAttendance(
                    "Bearer $t",
                    MarkAttendanceBody(type, location?.latitude, location?.longitude)
                )
                if (res.isSuccessful && res.body()?.ok == true) {
                    _state.value = if (type == "in")
                        HomeState.CheckedIn(res.body()?.at ?: "")
                    else HomeState.CheckedOut(res.body()?.at ?: "")
                    loadTodayLogs()
                } else {
                    _state.value = HomeState.Error(res.body()?.error ?: "Failed")
                }
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Error")
            }
        }
    }

    fun resetState() { _state.value = HomeState.Idle }
}
