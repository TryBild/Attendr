package com.trybild.attendr.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.R
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phone: String,
    name: String,
    orgId: String,
    navController: NavController
) {
    val vm: OtpViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var otpDigits by remember { mutableStateOf(List(6) { "" }) }
    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var resendSeconds by remember { mutableStateOf(30) }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
        while (resendSeconds > 0) {
            delay(1000L)
            resendSeconds--
        }
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is OtpUiState.Success -> {
                navController.navigate("home") {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            is OtpUiState.OtpResent -> {
                resendSeconds = 30
                vm.resetState()
            }
            is OtpUiState.Error -> {
                errorMsg = s.message
                showError = true
                vm.resetState()
            }
            else -> {}
        }
    }

    val otpFull = otpDigits.joinToString("")

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

                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = "Attendr Logo",
                    modifier = Modifier.size(64.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Verify OTP",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = AttendrTextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Enter the 6-digit code sent to +91 $phone",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AttendrTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    otpDigits.forEachIndexed { index, digit ->
                        OtpBox(
                            value = digit,
                            focusRequester = focusRequesters[index],
                            modifier = Modifier.weight(1f),
                            onValueChange = { newVal ->
                                val filtered = newVal.filter(Char::isDigit)
                                val updated = otpDigits.toMutableList()
                                if (filtered.isEmpty()) {
                                    updated[index] = ""
                                    if (index > 0) focusRequesters[index - 1].requestFocus()
                                } else {
                                    updated[index] = filtered.last().toString()
                                    if (index < 5) focusRequesters[index + 1].requestFocus()
                                }
                                otpDigits = updated
                                showError = false
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Didn't receive OTP? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AttendrTextSecondary
                    )
                    if (resendSeconds > 0) {
                        Text(
                            "Resend in ${resendSeconds}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AttendrTextSecondary
                        )
                    } else {
                        TextButton(onClick = { vm.resendOtp(name, phone, orgId) }) {
                            Text("Resend", color = AttendrNavy)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                AttendrButton(
                    text = if (state is OtpUiState.Loading) "Verifying..." else "Verify & Continue",
                    onClick = { vm.verifyOtp(phone, otpFull, orgId) },
                    enabled = otpFull.length == 6 && state !is OtpUiState.Loading
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

@Composable
private fun OtpBox(
    value: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(
                color = if (value.isNotEmpty()) AttendrNavy.copy(alpha = 0.08f) else GlassSurface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (value.isNotEmpty()) 2.dp else 1.dp,
                color = if (value.isNotEmpty()) AttendrNavy.copy(alpha = 0.5f) else GlassBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxSize()
                .wrapContentHeight(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = AttendrTextPrimary
            ),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    inner()
                    if (value.isEmpty()) {
                        Text("â€”", color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 20.sp)
                    }
                }
            }
        )
    }
}

