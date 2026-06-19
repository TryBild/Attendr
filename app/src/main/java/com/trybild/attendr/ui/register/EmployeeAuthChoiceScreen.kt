package com.trybild.attendr.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeAuthChoiceScreen(navController: NavController) {
    Scaffold(
        containerColor = AttendrBackground,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            LogoIcon(size = 64.dp)

            Spacer(Modifier.height(24.dp))

            Text(
                "Employee",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = AttendrTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "How would you like to continue?",
                style = MaterialTheme.typography.bodyLarge,
                color = AttendrTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            AttendrButton(
                text = "Create Account",
                onClick = { navController.navigate("register") }
            )

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("employee_login") }) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = AttendrTextSecondary)) {
                            append("Already have an account? ")
                        }
                        withStyle(SpanStyle(color = AttendrNavy, fontWeight = FontWeight.SemiBold)) {
                            append("Log in")
                        }
                    }
                )
            }
        }
    }
}
