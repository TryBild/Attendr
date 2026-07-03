package com.trybild.attendr.ui.register

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
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
fun ForgotPasswordScreen(
    navController: NavController,
    initialMobile: String = "",
    initialTeamId: String = ""
) {
    val vm: ForgotPasswordViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var mobile by remember { mutableStateOf(initialMobile) }
    var teamId by remember { mutableStateOf(initialTeamId) }
    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is ForgotPasswordState.OtpSent -> {
                navController.navigate(
                    "otp/${Uri.encode(s.mobile)}?orgId=${Uri.encode(teamId.trim())}&purpose=forgot"
                )
                vm.resetState()
            }
            is ForgotPasswordState.Error -> {
                errorMsg = s.message
                showError = true
                vm.resetState()
            }
            else -> {}
        }
    }

    val isLoading = state is ForgotPasswordState.Loading
    val isValid = mobile.length == 10 && teamId.isNotBlank()

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
                    "Forgot Password",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = AttendrTextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "We'll send an OTP to your registered mobile number to reset your password.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AttendrTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

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
                    value = teamId,
                    onValueChange = { teamId = it; showError = false },
                    label = "Team ID",
                    placeholder = "e.g. TRY190",
                    enabled = !isLoading
                )

                Spacer(Modifier.height(24.dp))

                AttendrButton(
                    text = if (isLoading) "Sending OTP…" else "Send OTP",
                    onClick = { vm.requestOtp(mobile.trim(), teamId.trim()) },
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
