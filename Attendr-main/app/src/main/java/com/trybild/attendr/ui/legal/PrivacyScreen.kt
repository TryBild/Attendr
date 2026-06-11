package com.trybild.attendr.ui.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.trybild.attendr.R

@Composable
fun PrivacyScreen(navController: NavController) {
    LegalContentScreen(
        title = stringResource(R.string.privacy_title),
        navController = navController,
        sections = listOf(
            "" to "Your privacy is important to us. This policy explains how we collect, use, and protect your data.",
            "Information We Collect" to "We collect your phone number, name, device ID, and GPS coordinates at the time of attendance marking. We do not collect background location data.",
            "How We Use Your Data" to "Your data is used solely to record and verify attendance for your organization. We do not sell your personal information to third parties.",
            "Data Storage" to "Your data is stored securely on servers managed by Trybild. Access is restricted to authorized personnel and your organization's administrators.",
            "Data Retention" to "Attendance records are retained for the duration of your employment with the organization. You may request deletion of your data by contacting support.",
            "Your Rights" to "You have the right to access, correct, and request deletion of your personal data. Contact your organization administrator or Attendr support for assistance.",
            "Contact Us" to "For privacy-related inquiries, please use the Contact Support feature within the app."
        )
    )
}
