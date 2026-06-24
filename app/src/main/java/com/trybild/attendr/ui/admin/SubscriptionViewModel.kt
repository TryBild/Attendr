package com.trybild.attendr.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionState(
    val loading: Boolean = true,
    val plan: String = "free",
    val status: String = "none",
    val trialDaysLeft: Int = 0,
    val upgradeLoading: Boolean = false,
    val checkoutUrl: String? = null,
    val error: String? = null
)

class SubscriptionViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _state = MutableStateFlow(SubscriptionState())
    val state: StateFlow<SubscriptionState> = _state

    init { loadStatus() }

    fun loadStatus() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = repo.getBillingStatus()
            if (result.isSuccess) {
                val b = result.getOrNull()!!
                _state.update {
                    it.copy(
                        loading = false,
                        plan = b.plan ?: "free",
                        status = b.status ?: "none",
                        trialDaysLeft = b.trialDaysLeft
                    )
                }
            } else {
                _state.update { it.copy(loading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun initiateUpgrade() {
        _state.update { it.copy(upgradeLoading = true, error = null) }
        viewModelScope.launch {
            val result = repo.createSubscription()
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                _state.update {
                    it.copy(upgradeLoading = false, checkoutUrl = body.shortUrl)
                }
            } else {
                _state.update {
                    it.copy(upgradeLoading = false, error = result.exceptionOrNull()?.message)
                }
            }
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            loadStatus()
        }
    }

    fun clearCheckoutUrl() {
        _state.update { it.copy(checkoutUrl = null) }
    }
}
