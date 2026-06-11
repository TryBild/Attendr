package com.trybild.attendr.ui.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.trybild.attendr.R

@Composable
fun DataUsageScreen(navController: NavController) {
    LegalContentScreen(
        title = stringResource(R.string.data_usage_title),
        navController = navController,
        sections = listOf(
            "" to "This policy describes what data Attendr collects and how it is used.",
            "Location Data" to "GPS coordinates are captured only at the moment of check-in or check-out. No continuous or background location tracking is performed.",
            "Phone Number" to "Used as a unique identifier for authentication via OTP. Not shared with third parties.",
            "Device ID" to "A randomly generated device identifier is stored to associate your device with your account for security purposes.",
            "Attendance Logs" to "Check-in and check-out timestamps are recorded and visible to your organization's administrator.",
            "Mock Location Detection" to "The app detects and flags attempts to use mock or spoofed GPS locations. This is shared with your organization as part of the attendance record.",
            "Data Security" to "All data is transmitted over HTTPS. We use industry-standard security practices to protect your information."
        )
    )
}
