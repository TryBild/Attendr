package com.trybild.attendr.ui.register

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.theme.*

// Stitch redesign - "Material 3 industrial" style. Layout structure only in
// this commit (header / form / footer sections + outer scaffold); each
// section's exact visual content is filled in by subsequent commits.
@Composable
fun EmployeeLoginScreen(navController: NavController) {
    val vm: EmployeeLoginViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var orgId by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is EmployeeLoginState.Success -> {
                navController.navigate("home") {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            is EmployeeLoginState.Error -> {
                errorMsg = s.message
                showError = true
                vm.resetState()
            }
            else -> {}
        }
    }

    val isLoading = state is EmployeeLoginState.Loading
    val isValid = orgId.isNotBlank() && mobile.length == 10 && password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StitchBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header section ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Attendr", style = StitchDisplay, color = StitchPrimary)
                Spacer(Modifier.height(24.dp))
                Text(
                    "Welcome to Attendr",
                    style = StitchHeadlineLgMobile,
                    color = StitchOnSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Login to mark your attendance",
                    style = StitchBodyMd,
                    color = StitchOnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── Form section ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
            ) {
                LoginFormFields(
                    orgId = orgId,
                    onOrgIdChange = { orgId = it; showError = false },
                    mobile = mobile,
                    onMobileChange = { if (it.length <= 10 && it.all(Char::isDigit)) { mobile = it; showError = false } },
                    password = password,
                    onPasswordChange = { password = it; showError = false },
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                    isLoading = isLoading,
                    isValid = isValid,
                    onSubmit = { vm.login(mobile.trim(), orgId.trim(), password) },
                    onForgotPassword = {
                        navController.navigate(
                            "forgot_password?mobile=${Uri.encode(mobile)}&teamId=${Uri.encode(orgId.trim())}"
                        )
                    },
                    onSignUp = { navController.navigate("register") }
                )
            }

            // ── Footer section ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {}
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

@Composable
private fun LoginFormFields(
    orgId: String,
    onOrgIdChange: (String) -> Unit,
    mobile: String,
    onMobileChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    isLoading: Boolean,
    isValid: Boolean,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit
) {
    OutlinedTextField(
        value = orgId,
        onValueChange = onOrgIdChange,
        label = { Text("Company Code / Team ID") },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = mobile,
        onValueChange = onMobileChange,
        label = { Text("Mobile Number") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisible) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = onSubmit,
        enabled = isValid && !isLoading,
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text(if (isLoading) "Logging in…" else "Login")
    }
    Spacer(Modifier.height(12.dp))
    TextButton(onClick = onForgotPassword, enabled = !isLoading) {
        Text("Forgot Password?")
    }
    TextButton(onClick = onSignUp, enabled = !isLoading) {
        Text("Don't have an account? Sign Up")
    }
}
