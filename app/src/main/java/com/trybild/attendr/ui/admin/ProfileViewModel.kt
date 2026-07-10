package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.BuildConfig
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.GeofenceItem
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminProfileUiState(
    val loading: Boolean = true,
    val loadError: String? = null,

    // Section 1 — account
    val adminName: String = "",
    val mobile: String = "",
    val email: String = "",

    // Section 2 — company (read-only)
    val orgName: String = "",
    val orgId: String = "",

    val photoUrl: String? = null,
    val uploadingPhoto: Boolean = false,

    // Section 3 — company settings
    val workDays: Set<String> = emptySet(),
    val workStartTime: String = "09:00",
    val workEndTime: String = "18:00",

    // snapshot of last-saved values for dirty checks
    val initialAdminName: String = "",
    val initialWorkDays: Set<String> = emptySet(),
    val initialWorkStartTime: String = "09:00",
    val initialWorkEndTime: String = "18:00",

    val geofences: List<GeofenceItem> = emptyList(),

    val saving: Boolean = false,
    val saveError: String? = null,
    val saveMessage: String? = null,

    val appVersion: String = BuildConfig.VERSION_NAME
) {
    val accountDirty: Boolean
        get() = adminName.trim() != initialAdminName.trim() && adminName.isNotBlank()

    val settingsDirty: Boolean
        get() = workDays != initialWorkDays ||
                workStartTime != initialWorkStartTime ||
                workEndTime != initialWorkEndTime

    val settingsValid: Boolean
        get() = workDays.isNotEmpty() && workEndTime > workStartTime
}

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)
    private val dataStore = TokenDataStore(app)

    private val _state = MutableStateFlow(AdminProfileUiState())
    val state: StateFlow<AdminProfileUiState> = _state

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            val cachedOrgName = dataStore.companyName.firstOrNull() ?: ""
            val cachedOrgId = dataStore.orgId.firstOrNull() ?: ""

            val result = repo.adminProfile()
            if (result.isSuccess) {
                val p = result.getOrNull()!!
                val days = p.workDays?.toSet() ?: emptySet()
                val start = p.workStartTime ?: "09:00"
                val end = p.workEndTime ?: "18:00"
                _state.update {
                    it.copy(
                        loading = false,
                        adminName = p.adminName ?: "",
                        mobile = p.phone ?: "",
                        email = p.adminEmail ?: "",
                        orgName = p.orgName ?: cachedOrgName,
                        orgId = p.orgId ?: cachedOrgId,
                        photoUrl = p.photoUrl,
                        workDays = days,
                        workStartTime = start,
                        workEndTime = end,
                        initialAdminName = p.adminName ?: "",
                        initialWorkDays = days,
                        initialWorkStartTime = start,
                        initialWorkEndTime = end
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        loading = false,
                        loadError = result.exceptionOrNull()?.message ?: "Could not load profile",
                        orgName = cachedOrgName,
                        orgId = cachedOrgId
                    )
                }
            }

            val geoResult = repo.getAdminGeofences()
            if (geoResult.isSuccess) {
                _state.update { it.copy(geofences = geoResult.getOrNull()?.geofences ?: emptyList()) }
            }
        }
    }

    fun setAdminName(v: String) = _state.update { it.copy(adminName = v) }

    fun toggleWorkDay(day: String) = _state.update {
        it.copy(workDays = if (it.workDays.contains(day)) it.workDays - day else it.workDays + day)
    }

    fun setWorkStartTime(v: String) = _state.update { it.copy(workStartTime = v) }
    fun setWorkEndTime(v: String) = _state.update { it.copy(workEndTime = v) }

    fun consumeMessages() = _state.update { it.copy(saveError = null, saveMessage = null) }

    /** Surface an informational message through the snackbar channel. */
    fun notify(message: String) = _state.update { it.copy(saveMessage = message) }

    /**
     * Persists the current account + company-settings state via the existing
     * adminSetup endpoint (which is an idempotent findByIdAndUpdate). Both the
     * account and company-settings Save buttons funnel through here; sending the
     * full current state is harmless since unchanged fields are written back as-is.
     */
    fun save(successMessage: String) {
        val s = _state.value
        if (!s.settingsValid) {
            _state.update {
                it.copy(saveError = if (it.workDays.isEmpty()) "Select at least one work day"
                                    else "End time must be after start time")
            }
            return
        }
        _state.update { it.copy(saving = true, saveError = null, saveMessage = null) }
        viewModelScope.launch {
            val result = repo.adminSetup(
                workDays = s.workDays.toList(),
                workStartTime = s.workStartTime,
                workEndTime = s.workEndTime,
                adminName = s.adminName.trim()
            )
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        saving = false,
                        saveMessage = successMessage,
                        initialAdminName = it.adminName.trim(),
                        initialWorkDays = it.workDays,
                        initialWorkStartTime = it.workStartTime,
                        initialWorkEndTime = it.workEndTime
                    )
                }
            } else {
                _state.update {
                    it.copy(saving = false, saveError = result.exceptionOrNull()?.message ?: "Could not save changes")
                }
            }
        }
    }

    fun uploadPhoto(imageBytes: ByteArray) {
        _state.update { it.copy(uploadingPhoto = true) }
        viewModelScope.launch {
            val result = repo.uploadProfilePhoto(imageBytes)
            if (result.isSuccess) {
                _state.update { it.copy(uploadingPhoto = false, photoUrl = result.getOrNull(), saveMessage = "Photo updated") }
            } else {
                _state.update { it.copy(uploadingPhoto = false, saveError = result.exceptionOrNull()?.message ?: "Could not upload photo") }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            dataStore.clearToken()
            onDone()
        }
    }
}
