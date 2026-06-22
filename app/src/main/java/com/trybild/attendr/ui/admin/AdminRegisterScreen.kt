package com.trybild.attendr.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRegisterScreen(navController: NavController) {
    val vm: AdminRegisterViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    var orgName by remember { mutableStateOf("") }
    var adminName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var orgSize by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var orgSizeExpanded by remember { mutableStateOf(false) }

    var orgNameError by remember { mutableStateOf("") }
    var adminNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var cityError by remember { mutableStateOf("") }
    var orgSizeError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var serverError by remember { mutableStateOf("") }

    var showOrgIdDialog by remember { mutableStateOf(false) }
    var registeredOrgId by remember { mutableStateOf("") }
    var copiedToClipboard by remember { mutableStateOf(false) }

    val orgSizeOptions = listOf("1-10", "11-50", "51-200", "200+")
    val orgSizeLabels = listOf(
        "1–10 employees", "11–50 employees", "51–200 employees", "200+ employees"
    )

    LaunchedEffect(state) {
        when (val s = state) {
            is AdminRegisterState.Success -> {
                registeredOrgId = s.orgId
                showOrgIdDialog = true
                vm.resetState()
            }
            is AdminRegisterState.Error -> {
                serverError = s.message
                vm.resetState()
            }
            else -> {}
        }
    }

    fun validate(): Boolean {
        var valid = true
        orgNameError = if (orgName.trim().length < 2) { valid = false; "Min 2 characters" } else ""
        adminNameError = if (adminName.isBlank()) { valid = false; "Required" } else ""
        emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            valid = false; "Invalid email format"
        } else ""
        phoneError = if (phone.length != 10 || !phone.all(Char::isDigit)) {
            valid = false; "Enter exactly 10 digits"
        } else ""
        cityError = if (city.isBlank()) { valid = false; "Required" } else ""
        orgSizeError = if (orgSize.isBlank()) { valid = false; "Select an option" } else ""
        passwordError = if (password.length < 8) { valid = false; "Min 8 characters" } else ""
        confirmPasswordError = if (confirmPassword != password) {
            valid = false; "Passwords don't match"
        } else ""
        return valid
    }

    if (showOrgIdDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Registration Successful!", style = MaterialTheme.typography.headlineMedium) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Your Organization ID",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AttendrTextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        registeredOrgId,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AttendrNavy,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(registeredOrgId))
                            copiedToClipboard = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (copiedToClipboard) "Copied!" else "Copy Org ID")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Save this Org ID — employees will need it to join.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AttendrTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                AttendrButton(
                    text = "Continue to Setup",
                    onClick = {
                        showOrgIdDialog = false
                        navController.navigate("admin_setup") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                )
            }
        )
    }

    val isLoading = state is AdminRegisterState.Loading

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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                LogoIcon(size = 64.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Create Admin Account",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = AttendrTextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Set up your organization on Attendr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AttendrTextSecondary,
                    textAlign = TextAlign.Center
                )

                if (serverError.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AttendrErrorBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            serverError,
                            color = AttendrError,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 1. Organization Name
                AttendrTextField(
                    value = orgName,
                    onValueChange = { orgName = it; orgNameError = ""; serverError = "" },
                    label = "Organization Name",
                    placeholder = "e.g. Acme Corp",
                    errorText = orgNameError,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(16.dp))

                // 2. Your Full Name
                AttendrTextField(
                    value = adminName,
                    onValueChange = { adminName = it; adminNameError = ""; serverError = "" },
                    label = "Your Full Name",
                    placeholder = "e.g. Rahul Sharma",
                    errorText = adminNameError,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(16.dp))

                // 3. Email
                AttendrTextField(
                    value = email,
                    onValueChange = { email = it; emailError = ""; serverError = "" },
                    label = "Email",
                    placeholder = "admin@company.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    errorText = emailError,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(16.dp))

                // 4. Phone Number
                AttendrTextField(
                    value = phone,
                    onValueChange = {
                        if (it.length <= 10 && it.all(Char::isDigit)) phone = it
                        phoneError = ""
                        serverError = ""
                    },
                    label = "Phone Number",
                    placeholder = "10-digit mobile number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    errorText = phoneError,
                    enabled = !isLoading,
                    leadingContent = {
                        Text(
                            "+91",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AttendrTextSecondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                )

                Spacer(Modifier.height(16.dp))

                // 5. City
                AttendrTextField(
                    value = city,
                    onValueChange = { city = it; cityError = ""; serverError = "" },
                    label = "City",
                    placeholder = "e.g. Mumbai",
                    errorText = cityError,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(16.dp))

                // 6. Organization Size (dropdown)
                Column {
                    ExposedDropdownMenuBox(
                        expanded = orgSizeExpanded,
                        onExpandedChange = { if (!isLoading) orgSizeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (orgSize.isEmpty()) "" else orgSizeLabels[orgSizeOptions.indexOf(orgSize)],
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Organization Size") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(orgSizeExpanded) },
                            isError = orgSizeError.isNotEmpty(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AttendrNavy,
                                unfocusedBorderColor = AttendrBorder
                            ),
                            enabled = !isLoading
                        )
                        ExposedDropdownMenu(
                            expanded = orgSizeExpanded,
                            onDismissRequest = { orgSizeExpanded = false }
                        ) {
                            orgSizeOptions.forEachIndexed { i, value ->
                                DropdownMenuItem(
                                    text = { Text(orgSizeLabels[i]) },
                                    onClick = {
                                        orgSize = value
                                        orgSizeError = ""
                                        orgSizeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (orgSizeError.isNotEmpty()) {
                        Text(
                            orgSizeError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 7. Password
                AttendrTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = ""; serverError = "" },
                    label = "Password",
                    placeholder = "Min 8 characters",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    errorText = passwordError,
                    enabled = !isLoading,
                    trailingContent = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide" else "Show",
                                tint = AttendrTextSecondary
                            )
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // 8. Confirm Password
                AttendrTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmPasswordError = ""; serverError = "" },
                    label = "Confirm Password",
                    placeholder = "Re-enter your password",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    errorText = confirmPasswordError,
                    enabled = !isLoading,
                    trailingContent = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide" else "Show",
                                tint = AttendrTextSecondary
                            )
                        }
                    }
                )

                Spacer(Modifier.height(28.dp))

                AttendrButton(
                    text = if (isLoading) "Registering…" else "Register",
                    onClick = {
                        serverError = ""
                        if (validate()) {
                            vm.register(
                                orgName.trim(), adminName.trim(), email.trim(),
                                phone, city.trim(), orgSize, password
                            )
                        }
                    },
                    enabled = !isLoading
                )

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = AttendrTextSecondary)) {
                                append("Already registered? ")
                            }
                            withStyle(SpanStyle(color = AttendrNavy, fontWeight = FontWeight.SemiBold)) {
                                append("Login")
                            }
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
    }
}
