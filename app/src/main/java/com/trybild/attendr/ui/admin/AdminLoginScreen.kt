package com.trybild.attendr.ui.admin

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(navController: NavController) {
    val vm: AdminLoginViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is AdminLoginState.Success -> {
                val dest = if (s.setupComplete) "admin_home" else "admin_setup"
                navController.navigate(dest) {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            is AdminLoginState.Error -> {
                errorMsg = s.message
                showError = true
                vm.resetState()
            }
            else -> {}
        }
    }

    val isLoading = state is AdminLoginState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
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
                Spacer(Modifier.height(8.dp))

                LogoIcon(size = 64.dp)

                Spacer(Modifier.height(16.dp))

                Text(
                    "Admin Login",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = AttendrTextPrimary
                )

                Spacer(Modifier.height(32.dp))

                AttendrTextField(
                    value = email,
                    onValueChange = { email = it; showError = false },
                    label = "Email",
                    placeholder = "admin@company.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isLoading
                )

                Spacer(Modifier.height(20.dp))

                AttendrTextField(
                    value = password,
                    onValueChange = { password = it; showError = false },
                    label = "Password",
                    placeholder = "Enter your password",
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

                Spacer(Modifier.height(24.dp))

                AttendrButton(
                    text = if (isLoading) "Logging in…" else "Login",
                    onClick = { vm.login(email.trim(), password) },
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
                )

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { navController.navigate("admin_register") }) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = AttendrTextSecondary)) {
                                append("Don't have an account? ")
                            }
                            withStyle(SpanStyle(color = AttendrNavy, fontWeight = FontWeight.SemiBold)) {
                                append("Register")
                            }
                        }
                    )
                }
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
