package com.trybild.attendr.ui.roleselection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrCard
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(navController: NavController) {
    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            LogoIcon(size = 64.dp)

            Spacer(Modifier.height(24.dp))

            Text(
                "Who are you?",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = AttendrTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Choose your role to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = AttendrTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            RoleCard(
                icon = Icons.Default.Work,
                title = "Admin",
                subtitle = "I manage a team or organisation",
                onClick = { navController.navigate("admin_login") }
            )

            Spacer(Modifier.height(16.dp))

            RoleCard(
                icon = Icons.Default.Person,
                title = "Employee",
                subtitle = "I mark my daily attendance",
                onClick = { navController.navigate("employee_auth") }
            )
        }
    }
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    AttendrCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AttendrNavy.copy(alpha = 0.08f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AttendrNavy,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = AttendrTextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AttendrTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AttendrTextSecondary
            )
        }
    }
}
