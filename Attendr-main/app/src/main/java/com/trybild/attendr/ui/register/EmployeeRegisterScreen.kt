package com.trybild.attendr.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.components.OtpInputRow
import com.trybild.attendr.ui.components.StepDotsIndicator
import com.trybild.attendr.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeRegisterScreen(
    onBackFromFirstStep: () -> Unit,
    onRegistered: () -> Unit
) {
    val vm: EmployeeRegisterViewModel = viewModel()
    val step by vm.step.collectAsState()
    val fullName by vm.fullName.collectAsState()
    val mobile by vm.mobile.collectAsState()
    val teamId by vm.teamId.collectAsState()
    val otp by vm.otp.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    Box(Modifier.fillMaxSize().background(AttendrBackground)) {
        Scaffold(
            containerColor = AttendrBackground,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = {
                            if (step > 1) vm.goBack() else onBackFromFirstStep()
                        }) {
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
                    .padding(horizontal = 24.dp)
            ) {
                StepDotsIndicator(total = 4, current = step)
                Spacer(Modifier.height(24.dp))

                when (step) {
                    1 -> DetailsStep(
                        fullName = fullName,
                        mobile = mobile,
                        teamId = teamId,
                        loading = loading,
                        onFullNameChange = vm::onFullNameChange,
                        onMobileChange = vm::onMobileChange,
                        onTeamIdChange = vm::onTeamIdChange,
                        onSendOtp = { vm.sendOtp() }
                    )
                    else -> OtpStep(
                        mobile = mobile,
                        otp = otp,
                        loading = loading,
                        onOtpChange = vm::onOtpChange,
                        onResend = { vm.sendOtp() },
                        onVerify = { vm.verifyOtp(onVerified = onRegistered) }
                    )
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
private fun ColumnScope.DetailsStep(
    fullName: String,
    mobile: String,
    teamId: String,
    loading: Boolean,
    onFullNameChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onTeamIdChange: (String) -> Unit,
    onSendOtp: () -> Unit
) {
    Text("Your details", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AttendrTextPrimary)
    Spacer(Modifier.height(4.dp))
    Text("Step 1 of 4", color = AttendrTextSecondary)

    Spacer(Modifier.height(24.dp))
    AttendrTextField(
        value = fullName,
        onValueChange = onFullNameChange,
        label = "Full Name",
        placeholder = "Enter your full name"
    )
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            "+91 |",
            color = AttendrNavy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 12.dp, bottom = 18.dp)
        )
        AttendrTextField(
            value = mobile,
            onValueChange = onMobileChange,
            label = "Mobile Number",
            placeholder = "10-digit mobile number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(16.dp))
    AttendrTextField(
        value = teamId,
        onValueChange = onTeamIdChange,
        label = "Team ID",
        placeholder = "e.g. TRY190"
    )

    Spacer(Modifier.weight(1f))
    AttendrButton(
        text = "Send OTP",
        onClick = onSendOtp,
        loading = loading,
        enabled = fullName.isNotBlank() && mobile.length == 10 && teamId.isNotBlank()
    )
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun ColumnScope.OtpStep(
    mobile: String,
    otp: String,
    loading: Boolean,
    onOtpChange: (String) -> Unit,
    onResend: () -> Unit,
    onVerify: () -> Unit
) {
    var resendKey by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(60) }

    LaunchedEffect(resendKey) {
        seconds = 60
        while (seconds > 0) {
            delay(1000)
            seconds--
        }
    }

    Text("Verify your number", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AttendrTextPrimary)
    Spacer(Modifier.height(4.dp))
    Text(
        "We sent a 6-digit OTP to +91 XXXXX${mobile.takeLast(2)}",
        color = AttendrTextSecondary
    )

    Spacer(Modifier.height(32.dp))
    OtpInputRow(otp = otp, onOtpChange = onOtpChange)

    Spacer(Modifier.height(16.dp))
    if (seconds > 0) {
        Text(
            "Resend in 00:${"%02d".format(seconds)}",
            color = AttendrTextSecondary,
            fontSize = 14.sp
        )
    } else {
        Text(
            "Resend OTP",
            color = AttendrNavy,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(enabled = !loading) {
                onResend()
                resendKey++
            }
        )
    }

    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AttendrInfoBg, RoundedCornerShape(12.dp))
            .border(1.dp, AttendrInfoBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Email,
            contentDescription = null,
            tint = AttendrNavy,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Check your email & SMS both", fontSize = 13.sp, color = AttendrTextPrimary)
    }

    Spacer(Modifier.weight(1f))
    AttendrButton(
        text = "Verify & Continue",
        onClick = onVerify,
        loading = loading,
        enabled = otp.length == 6
    )
    Spacer(Modifier.height(24.dp))
}
