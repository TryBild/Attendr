package com.trybild.attendr.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    val vm: AdminRegisterViewModel = viewModel()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val createdTeamId by vm.createdTeamId.collectAsState()

    var companyName by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val passwordError = if (password.isNotEmpty() && password.length < 8)
        "Password must be at least 8 characters" else ""
    val confirmError = if (confirmPassword.isNotEmpty() && confirmPassword != password)
        "Passwords do not match" else ""
    val formValid = companyName.isNotBlank() && adminEmail.isNotBlank() &&
            password.length >= 8 && confirmPassword == password

    Box(Modifier.fillMaxSize().background(AttendrBackground)) {
        Scaffold(
            containerColor = AttendrBackground,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = AttendrTextPrimary
                            )
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    "Create your organisation",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AttendrTextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text("Set up your Attendr workspace", color = AttendrTextSecondary)

                if (createdTeamId != null) {
                    Spacer(Modifier.height(32.dp))
                    SuccessCard(teamId = createdTeamId.orEmpty(), onContinue = onRegistered)
                    Spacer(Modifier.height(24.dp))
                } else {
                    Spacer(Modifier.height(32.dp))
                    AttendrTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = "Company Name",
                        placeholder = "Your company name"
                    )
                    Spacer(Modifier.height(16.dp))
                    AttendrTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = "Admin Email",
                        placeholder = "admin@company.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(Modifier.height(16.dp))
                    AttendrTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Min 8 characters",
                        errorText = passwordError,
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
                    Spacer(Modifier.height(16.dp))
                    AttendrTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        placeholder = "Re-enter your password",
                        errorText = confirmError,
                        visualTransformation = if (confirmVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingContent = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    if (confirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (confirmVisible) "Hide password" else "Show password",
                                    tint = AttendrTextSecondary
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    AttendrTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City",
                        placeholder = "Optional"
                    )
                    Spacer(Modifier.height(16.dp))
                    AttendrTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = "State",
                        placeholder = "Optional"
                    )

                    Spacer(Modifier.height(32.dp))
                    AttendrButton(
                        text = "Create Organisation",
                        onClick = {
                            vm.register(companyName, adminEmail, password, city, state)
                        },
                        loading = loading,
                        enabled = formValid
                    )
                    Spacer(Modifier.height(24.dp))
                }
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

@Composable
private fun SuccessCard(teamId: String, onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AttendrBorder, RoundedCornerShape(12.dp))
            .background(AttendrSuccessBg, RoundedCornerShape(12.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = AttendrSuccess,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Organisation created!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AttendrTextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text("Share this Team ID with your employees:", color = AttendrTextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            teamId.ifEmpty { "—" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AttendrNavy
        )
        Spacer(Modifier.height(16.dp))
        AttendrButton(text = "Go to Dashboard", onClick = onContinue)
    }
}
