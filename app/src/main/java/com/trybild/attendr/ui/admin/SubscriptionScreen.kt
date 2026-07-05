package com.trybild.attendr.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.legal.AttendrUrls
import com.trybild.attendr.ui.legal.openUrl
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(navController: NavController) {
    val vm: SubscriptionViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.checkoutUrl) {
        state.checkoutUrl?.let { url ->
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            vm.clearCheckoutUrl()
        }
    }

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Subscription") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AttendrNavy)
                    }
                } else {
                    // Status card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val statusColor = when (state.status) {
                                "active" -> AttendrSuccess
                                "trialing" -> Color(0xFFD97706)
                                else -> AttendrError
                            }
                            val statusLabel = when (state.status) {
                                "active" -> "Active"
                                "trialing" -> "Trial"
                                "expired" -> "Expired"
                                "cancelled" -> "Cancelled"
                                else -> "No Plan"
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = statusColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    statusLabel,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = statusColor
                                )
                            }

                            Text(
                                state.plan.replaceFirstChar { it.uppercase() } + " Plan",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = AttendrTextPrimary
                            )

                            if (state.status == "trialing" && state.trialDaysLeft > 0) {
                                Text(
                                    "${state.trialDaysLeft} day${if (state.trialDaysLeft != 1) "s" else ""} remaining",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AttendrTextSecondary
                                )
                            }
                        }
                    }

                    // Pro plan card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Attendr Pro",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = AttendrNavy
                            )
                            ProFeatureRow("Unlimited employees")
                            ProFeatureRow("Advanced reports & CSV export")
                            ProFeatureRow("Geofence management")
                            ProFeatureRow("Priority support")
                            ProFeatureRow("WhatsApp daily digest")
                        }
                    }

                    if (state.status != "active") {
                        AttendrButton(
                            text = if (state.upgradeLoading) "Processing..." else "Upgrade to Pro",
                            onClick = { vm.initiateUpgrade() },
                            enabled = !state.upgradeLoading,
                            isLoading = state.upgradeLoading
                        )
                    }

                    Text(
                        "View full pricing",
                        style = MaterialTheme.typography.labelLarge,
                        color = AttendrNavy,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { context.openUrl(AttendrUrls.PRICING) }
                    )

                    if (state.status == "active") {
                        OutlinedButton(
                            onClick = { vm.cancelSubscription() },
                            enabled = !state.cancelLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                if (state.cancelLoading) "Cancelling..." else "Cancel Subscription",
                                color = AttendrTextSecondary
                            )
                        }
                    }

                    if (state.error != null) {
                        Text(
                            state.error!!,
                            color = AttendrError,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProFeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AttendrSuccess,
            modifier = Modifier.size(20.dp)
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, color = AttendrTextPrimary)
    }
}
