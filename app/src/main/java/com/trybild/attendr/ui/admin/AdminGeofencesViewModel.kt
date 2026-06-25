package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.model.GeofenceCreateRequest
import com.trybild.attendr.data.model.GeofenceItem
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeofencesState(
    val loading: Boolean = true,
    val geofences: List<GeofenceItem> = emptyList(),
    val error: String? = null,
    val saving: Boolean = false,
    val message: String? = null
)

class AdminGeofencesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val _state = MutableStateFlow(GeofencesState())
    val state: StateFlow<GeofencesState> = _state

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = repo.getAdminGeofences()
            if (result.isSuccess) {
                _state.update { it.copy(loading = false, geofences = result.getOrNull()?.geofences ?: emptyList()) }
            } else {
                _state.update { it.copy(loading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun create(name: String, lat: Double, lng: Double, radius: Double) {
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = repo.createGeofence(GeofenceCreateRequest(name, lat, lng, radius))
            if (result.isSuccess) {
                _state.update { it.copy(saving = false, message = "Geofence added") }
                load()
            } else {
                _state.update { it.copy(saving = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun update(id: String, name: String, lat: Double, lng: Double, radius: Double) {
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = repo.updateGeofence(id, GeofenceCreateRequest(name, lat, lng, radius))
            if (result.isSuccess) {
                _state.update { it.copy(saving = false, message = "Geofence updated") }
                load()
            } else {
                _state.update { it.copy(saving = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun delete(id: String) {
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = repo.deleteGeofence(id)
            if (result.isSuccess) {
                _state.update { it.copy(saving = false, message = "Geofence deleted") }
                load()
            } else {
                _state.update { it.copy(saving = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}
