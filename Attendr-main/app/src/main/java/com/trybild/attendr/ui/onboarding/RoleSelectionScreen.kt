package com.trybild.attendr.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrButtonVariant
import com.trybild.attendr.ui.theme.*

private enum class Role { Admin, Employee }

@Composable
fun RoleSelectionScreen(
    onContinueAsAdmin: () -> Unit,
    onContinueAsEmployee: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(Role.Admin) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AttendrBackground)
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(48.dp))

        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(AttendrNavy, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Attendr",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AttendrTextPrimary
            )
        }

        Spacer(Modifier.height(48.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                "Who are you?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Choose your role to get started",
                fontSize = 16.sp,
                color = AttendrTextSecondary
            )
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoleCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Work,
                title = "Admin",
                subtitle = "I manage a team or organisation",
                selected = selectedRole == Role.Admin,
                onClick = { selectedRole = Role.Admin }
            )
            RoleCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Person,
                title = "Employee",
                subtitle = "I mark my daily attendance",
                selected = selectedRole == Role.Employee,
                onClick = { selectedRole = Role.Employee }
            )
        }

        Spacer(Modifier.weight(1f))

        Column(modifier = Modifier.padding(24.dp)) {
            AttendrButton(
                text = if (selectedRole == Role.Admin) "Continue as Admin" else "Continue as Employee",
                onClick = {
                    if (selectedRole == Role.Admin) onContinueAsAdmin() else onContinueAsEmployee()
                }
            )
            Spacer(Modifier.height(16.dp))
            AttendrButton(
                text = if (selectedRole == Role.Admin) "Continue as Employee" else "Continue as Admin",
                variant = AttendrButtonVariant.Outlined,
                onClick = {
                    if (selectedRole == Role.Admin) onContinueAsEmployee() else onContinueAsAdmin()
                }
            )
        }
    }
}

@Composable
private fun RoleCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AttendrNavy else AttendrBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (selected) AttendrSelectedBg else AttendrBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AttendrNavy,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AttendrTextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            fontSize = 13.sp,
            color = AttendrTextSecondary
        )
    }
}
