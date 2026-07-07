package com.trybild.attendr.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.theme.AttendrTextPrimary
import com.trybild.attendr.ui.theme.AttendrTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPasswordScreen(
    pendingToken: String,
    fullName: String,
    navController: NavController
) {
    val vm: SetPasswordViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is SetPasswordState.Success -> {
                val dest = if (s.isAdmin) {
                    if (s.setupComplete) "admin_home" else "admin_setup"
                } else "home"
                navController.navigate(dest) {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            is SetPasswordState.Error -> {
                errorMsg = s.message
                showError = true
                vm.resetState()
            }
            else -> {}
        }
    }

    val isLoading = state is SetPasswordState.Loading
    val mismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val isValid = password.length >= 6 && password == confirmPassword

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                Spacer(Modifier.height(8.dp))

                LogoIcon(size = 64.dp)

                Spacer(Modifier.height(16.dp))

                Text(
                    "Set Password",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = AttendrTextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    if (fullName.isNotBlank()) "Hi $fullName, create a password to secure your account."
                    else "Create a password to secure your account.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AttendrTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                AttendrTextField(
                    value = password,
                    onValueChange = { password = it; showError = false },
                    label = "New Password",
                    placeholder = "At least 6 characters",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    enabled = !isLoading,
                    trailingContent = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = AttendrTextSecondary
                            )
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                AttendrTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; showError = false },
                    label = "Confirm Password",
                    placeholder = "Re-enter your password",
                    errorText = if (mismatch) "Passwords do not match" else "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (confirmVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    enabled = !isLoading,
                    trailingContent = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = if (confirmVisible) "Hide password" else "Show password",
                                tint = AttendrTextSecondary
                            )
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                AttendrButton(
                    text = if (isLoading) "Setting Password…" else "Set Password & Continue",
                    onClick = { vm.setPassword(pendingToken, password, confirmPassword) },
                    enabled = isValid && !isLoading,
                    isLoading = isLoading
                )
            }
        }

        ErrorToast(
            message = errorMsg,
            visible = showError,
            onDismiss = { showError = false },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp, start = 16.dp, end = 16.dp)
        )
    }
    }
}
