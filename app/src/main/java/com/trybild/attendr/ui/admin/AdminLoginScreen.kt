package com.trybild.attendr.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.theme.*

@Composable
fun AdminLoginScreen(
    onLoggedIn: () -> Unit,
    onCreateAccount: () -> Unit
) {
    val vm: AdminLoginViewModel = viewModel()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(AttendrBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
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

            Spacer(Modifier.height(64.dp))

            Text(
                "Welcome back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text("Sign in to your admin account", color = AttendrTextSecondary)

            Spacer(Modifier.height(32.dp))
            AttendrTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "admin@company.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(16.dp))
            AttendrTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter your password",
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingContent = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = AttendrTextSecondary
                        )
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Forgot password?",
                color = AttendrNavy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(Modifier.height(32.dp))
            AttendrButton(
                text = "Sign In",
                onClick = { vm.login(email, password, onSuccess = onLoggedIn) },
                loading = loading,
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account?", color = AttendrTextSecondary, fontSize = 14.sp)
                Text(
                    " Create one",
                    color = AttendrNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onCreateAccount() }
                )
            }
        }

        ErrorToast(
            message = error,
            visible = error.isNotEmpty(),
            onDismiss = { vm.clearError() },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
        )
    }
}
