package com.trybild.attendr.ui.register

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.CorporateFare
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.focus.onFocusChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.legal.LegalFooter
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
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(StitchSurfaceContainerLow)
                        .border(1.dp, StitchOutlineVariant.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = StitchSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Trusted by 10,000+ businesses across India",
                        style = StitchLabelSm,
                        color = StitchOnSurfaceVariant
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ENGLISH", style = StitchLabelSm, color = StitchPrimary)
                    Text("|", style = StitchLabelSm, color = StitchOutline)
                    Text("हिंदी", style = StitchLabelSm, color = StitchOnSurfaceVariant.copy(alpha = 0.6f))
                    Text("|", style = StitchLabelSm, color = StitchOutline)
                    Text("मराठी", style = StitchLabelSm, color = StitchOnSurfaceVariant.copy(alpha = 0.6f))
                }
                Spacer(Modifier.height(16.dp))
                LegalFooter()
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
    Column {
        StitchFieldLabel("Company Code / Team ID")
        Spacer(Modifier.height(4.dp))
        StitchTextField(
            value = orgId,
            onValueChange = onOrgIdChange,
            placeholder = "Ex: FACTORY01",
            enabled = !isLoading,
            leading = { Icon(Icons.Outlined.CorporateFare, contentDescription = null, tint = StitchOutline) }
        )
    }
    Spacer(Modifier.height(16.dp))
    Column {
        StitchFieldLabel("Mobile Number")
        Spacer(Modifier.height(4.dp))
        StitchTextField(
            value = mobile,
            onValueChange = onMobileChange,
            placeholder = "Enter 10-digit number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isLoading,
            leading = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("+91", style = StitchLabelBold, color = StitchOnSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier
                            .width(1.5.dp)
                            .height(24.dp)
                            .background(StitchOutlineVariant)
                    )
                }
            }
        )
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StitchFieldLabel("Password")
            TextButton(onClick = onForgotPassword, enabled = !isLoading, contentPadding = PaddingValues(0.dp)) {
                Text(
                    "FORGOT PASSWORD?",
                    style = StitchLabelSm.copy(letterSpacing = 0.025.em),
                    color = StitchPrimary
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        StitchTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "••••••••",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !isLoading,
            leading = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = StitchOutline) },
            trailing = {
                IconButton(onClick = onTogglePasswordVisible, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = StitchOutline
                    )
                }
            }
        )
    }
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = onSubmit,
        enabled = isValid && !isLoading,
        shape = StitchShapeLg,
        colors = ButtonDefaults.buttonColors(
            containerColor = StitchPrimary,
            contentColor = StitchOnPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text(
            if (isLoading) "LOGGING IN…" else "LOGIN",
            style = StitchLabelBold.copy(letterSpacing = 0.1.em)
        )
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Outlined.Login, contentDescription = null)
    }

    Spacer(Modifier.height(24.dp))
    HorizontalDivider(color = StitchOutlineVariant, thickness = 1.5.dp)
    Spacer(Modifier.height(24.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text("Don't have an account? ", style = StitchBodyMd, color = StitchOnSurfaceVariant)
        Text(
            "Sign Up",
            style = StitchBodyMd.copy(
                color = StitchPrimary,
                textDecoration = TextDecoration.Underline
            ),
            modifier = Modifier.clickable(onClick = onSignUp)
        )
    }
}

@Composable
private fun StitchFieldLabel(text: String) {
    Text(
        text.uppercase(),
        style = StitchLabelBold,
        color = StitchOnSurfaceVariant
    )
}

@Composable
private fun StitchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = if (focused) StitchPrimary else StitchOutlineVariant
    val borderWidth = if (focused) 2.dp else 1.5.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(StitchSurfaceContainerLowest, StitchShapeLg)
            .border(borderWidth, borderColor, StitchShapeLg)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            textStyle = StitchBodyMd.copy(color = StitchOnSurface),
            cursorBrush = SolidColor(StitchPrimary),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leading != null) {
                        leading()
                        Spacer(Modifier.width(8.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(placeholder, style = StitchBodyMd, color = StitchOutline.copy(alpha = 0.5f))
                        }
                        inner()
                    }
                    if (trailing != null) {
                        Spacer(Modifier.width(8.dp))
                        trailing()
                    }
                }
            }
        )
    }
}
