package com.trybild.attendr.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen() {
    Scaffold(
        containerColor = AttendrBackground,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AttendrBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoIcon(size = 64.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                "Welcome, Admin",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Admin dashboard coming soon.",
                style = MaterialTheme.typography.bodyLarge,
                color = AttendrTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
