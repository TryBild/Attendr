package com.trybild.attendr.ui.register

import androidx.compose.foundation.background
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
import com.trybild.attendr.ui.theme.*

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
    var fieldError by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is SetPasswordUiState.Success -> {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            }
            is SetPasswordUiState.Error -> {
                errorMsg = s.message
                showError = true
                vm.resetState()
            }
            else -> {}
        }
    }

    val isLoading = state is SetPasswordUiState.Loading

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
                    "Create Password",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = AttendrTextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    if (fullName.isNotBlank()) "Welcome, $fullName. Set a password for your account."
                    else "Set a password for your account.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AttendrTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                AttendrTextField(
                    value = password,
                    onValueChange = { password = it; fieldError = ""; showError = false },
                    label = "Create Password",
                    placeholder = "At least 8 characters",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    enabled = !isLoading,
                    trailingContent = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password"
                                                     else "Show password",
                                tint = AttendrTextSecondary
                            )
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                AttendrTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; fieldError = ""; showError = false },
                    label = "Confirm Password",
                    placeholder = "Re-enter your password",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (confirmVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    enabled = !isLoading,
                    errorText = fieldError,
                    trailingContent = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = if (confirmVisible) "Hide password"
                                                     else "Show password",
                                tint = AttendrTextSecondary
                            )
                        }
                    }
                )

                Spacer(Modifier.weight(1f))

                AttendrButton(
                    text = if (isLoading) "Setting Password…" else "Set Password",
                    onClick = {
                        when {
                            password.length < 8 -> fieldError = "Password must be at least 8 characters"
                            !password.any { it.isUpperCase() } -> fieldError = "Password must contain at least 1 uppercase letter"
                            !password.any { it.isDigit() } -> fieldError = "Password must contain at least 1 number"
                            !password.any { it in "!@#$%^&*" } -> fieldError = "Password must contain at least 1 special character (!@#$%^&*)"
                            password != confirmPassword -> fieldError = "Passwords do not match"
                            else -> {
                                fieldError = ""
                                vm.setPassword(pendingToken, password)
                            }
                        }
                    },
                    enabled = password.isNotBlank() && confirmPassword.isNotBlank() && !isLoading
                )

                Spacer(Modifier.height(24.dp))
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
