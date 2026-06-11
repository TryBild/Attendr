package com.trybild.attendr.ui.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trybild.attendr.R
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(navController: NavController) {
    LegalContentScreen(
        title = stringResource(R.string.terms_title),
        navController = navController,
        sections = listOf(
            "" to "By using Attendr, you agree to these terms. Please read them carefully.",
            "1. Acceptance of Terms" to "By downloading, installing, or using Attendr, you agree to be bound by these Terms of Service. If you do not agree, please do not use the application.",
            "2. Use of the App" to "Attendr is provided for use by registered employees of participating organizations. You must not misuse the app, including but not limited to falsifying attendance records or using mock GPS locations.",
            "3. Account Responsibility" to "You are responsible for maintaining the confidentiality of your account. Report any unauthorized access to your organization administrator immediately.",
            "4. Termination" to "Your access to Attendr may be terminated by your organization at any time. Trybild reserves the right to suspend service for violations of these terms.",
            "5. Limitation of Liability" to "Attendr is provided as-is. Trybild shall not be liable for any indirect, incidental, or consequential damages arising from use of the app.",
            "6. Changes to Terms" to "These terms may be updated from time to time. Continued use of the app after changes constitutes acceptance of the revised terms."
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LegalContentScreen(
    title: String,
    navController: NavController,
    sections: List<Pair<String, String>>
) {
    Scaffold(
        containerColor = AttendrBackground,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AttendrBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            sections.forEach { (heading, body) ->
                if (heading.isNotEmpty()) {
                    Text(heading, style = MaterialTheme.typography.headlineMedium, color = AttendrTextPrimary)
                    Spacer(Modifier.height(8.dp))
                }
                Text(body, style = MaterialTheme.typography.bodyLarge, color = AttendrTextSecondary)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
