package com.trybild.attendr.ui.employee

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.data.model.GeofenceItem
import com.trybild.attendr.data.repository.AuthRepository
import com.trybild.attendr.ui.components.AttendrEmptyState
import com.trybild.attendr.ui.components.IndustrialCard
import com.trybild.attendr.ui.components.IndustrialSectionLabel
import com.trybild.attendr.ui.legal.AttendrUrls
import com.trybild.attendr.ui.legal.LegalMenuRow
import com.trybild.attendr.ui.theme.*
import com.trybild.attendr.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(outerNavController: NavController) {
    val context = LocalContext.current
    val dataStore = remember { TokenDataStore(context) }
    val scope = rememberCoroutineScope()

    val employeeName by dataStore.employeeName.collectAsStateWithLifecycle(initialValue = null)
    val companyName  by dataStore.companyName.collectAsStateWithLifecycle(initialValue = null)
    val photoUrl     by dataStore.photoUrl.collectAsStateWithLifecycle(initialValue = null)

    val repo = remember { AuthRepository(context) }
    var geofences by remember { mutableStateOf<List<GeofenceItem>>(emptyList()) }
    var uploadingPhoto by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        repo.getGeofences().getOrNull()?.geofences?.let { geofences = it }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploadingPhoto = true
                try {
                    val bytes = withContext(Dispatchers.Default) { ImageUtils.compressImageFromUri(context, uri) }
                    val result = repo.uploadProfilePhoto(bytes)
                    if (result.isFailure) {
                        Toast.makeText(context, result.exceptionOrNull()?.message ?: "Could not upload photo", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ImageUtils.ImageReadException) {
                    Toast.makeText(context, e.message ?: "Could not read selected image", Toast.LENGTH_SHORT).show()
                }
                uploadingPhoto = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialPageBackground)
    ) {
        // Employee Profile is a bottom-tab root (no back stack to pop, unlike
        // the admin variant which is pushed) - title only, no back arrow.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(StitchSurface)
                .drawBehind {
                    drawLine(
                        color = StitchOutlineVariant,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Settings", style = StitchHeadlineMd, color = StitchPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(Modifier.height(40.dp))

        // Avatar initials circle
        val initials = employeeName
            ?.trim()
            ?.split(" ")
            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
            ?.take(2)
            ?.joinToString("") ?: "?"

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable(enabled = !uploadingPhoto) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(StitchSurfaceContainer)
                    .border(1.5.dp, StitchOutlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(initials, style = StitchHeadlineLg, color = StitchPrimary)
                }
                if (uploadingPhoto) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(StitchPrimary)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = "Change photo",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            employeeName ?: "Employee",
            style = StitchHeadlineLgMobile,
            color = StitchOnSurface
        )
        if (!companyName.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                companyName!!,
                style = StitchBodyMd,
                color = StitchOnSurfaceVariant
            )
        }

        Spacer(Modifier.height(32.dp))

        IndustrialSectionLabel("Personal Information", modifier = Modifier.align(Alignment.Start))
        IndustrialCard {
            ProfileRow(label = "Name", value = employeeName ?: "—")
            HorizontalDivider(color = StitchOutlineVariant, thickness = 1.dp)
            ProfileRow(label = "Organisation", value = companyName ?: "—")
        }

        Spacer(Modifier.height(24.dp))

        // Assigned geofences
        Text("Office Locations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AttendrTextPrimary)
        Spacer(Modifier.height(8.dp))
        if (geofences.isEmpty()) {
            AttendrEmptyState(
                icon = Icons.Default.LocationOn,
                title = "No geofence assigned",
                description = "Contact your admin to set up office locations"
            )
        } else {
            geofences.forEach { gf ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AttendrSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(24.dp))
                        Column {
                            Text(gf.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = AttendrTextPrimary)
                            Text("Radius: ${gf.radiusMeters.toInt()}m", style = MaterialTheme.typography.bodySmall, color = AttendrTextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Legal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AttendrTextPrimary)
        LegalMenuRow(label = "Privacy Policy", url = AttendrUrls.PRIVACY)
        LegalMenuRow(label = "Terms of Service", url = AttendrUrls.TERMS)
        LegalMenuRow(label = "Contact Support", url = AttendrUrls.CONTACT)
        LegalMenuRow(label = "Delete Account", url = AttendrUrls.DELETE_ACCOUNT, destructive = true)

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    dataStore.clearToken()
                    outerNavController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AttendrError),
            border = androidx.compose.foundation.BorderStroke(1.dp, AttendrError)
        ) {
            Text("Log Out", style = MaterialTheme.typography.labelLarge)
        }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(label.uppercase(), style = StitchLabelSm, color = StitchOutline)
        Text(value, style = StitchBodyLg, color = StitchOnSurface)
    }
}
