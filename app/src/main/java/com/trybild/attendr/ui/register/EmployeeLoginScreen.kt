package com.trybild.attendr.ui.register

import android.net.Uri
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.legal.LegalFooter
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
                    "Employee Login",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = AttendrTextPrimary
                )

                Spacer(Modifier.height(32.dp))

                AttendrTextField(
                    value = orgId,
                    onValueChange = { orgId = it; showError = false },
                    label = "Organisation ID",
                    placeholder = "e.g. ATT-XXXX-XXXX",
                    enabled = !isLoading
                )

                Spacer(Modifier.height(20.dp))

                AttendrTextField(
                    value = mobile,
                    onValueChange = {
                        if (it.length <= 10 && it.all(Char::isDigit)) { mobile = it; showError = false }
                    },
                    label = "Mobile Number",
                    placeholder = "10-digit mobile number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isLoading,
                    leadingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text("+91", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outline))
                            Spacer(Modifier.width(4.dp))
                        }
                    }
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
                    text = if (isLoading) "Logging in…" else "Log In",
                    onClick = { vm.login(mobile.trim(), orgId.trim(), password) },
                    enabled = isValid && !isLoading
                )

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        navController.navigate(
                            "forgot_password?mobile=${Uri.encode(mobile)}&teamId=${Uri.encode(orgId.trim())}"
                        )
                    },
                    enabled = !isLoading
                ) {
                    Text("Forgot Password?", color = AttendrNavy)
                }

                Spacer(Modifier.height(12.dp))

                LegalFooter(modifier = Modifier.padding(bottom = 16.dp))
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
