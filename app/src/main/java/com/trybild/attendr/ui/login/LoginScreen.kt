package com.trybild.attendr.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trybild.attendr.ui.components.LogoIcon

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val vm: AuthViewModel = viewModel()
    val state by vm.state.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var teamId by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.OtpSent -> { otpSent = true; errorMsg = "" }
            is AuthState.Error -> { errorMsg = s.message; vm.resetState() }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoIcon(size = 64.dp)
        Spacer(Modifier.height(4.dp))
        Text("Digital Attendance Register", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !otpSent,
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Text("+91", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = mobile,
                onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) mobile = it },
                label = { Text("Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                enabled = !otpSent,
                singleLine = true
            )
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = teamId,
            onValueChange = { teamId = it.uppercase() },
            label = { Text("Team ID") },
            placeholder = { Text("e.g. TRY190") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !otpSent,
            singleLine = true
        )
        Spacer(Modifier.height(20.dp))

        if (!otpSent) {
            Button(
                onClick = { vm.requestOtp(fullName.trim(), mobile, teamId.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = fullName.isNotBlank() && mobile.length == 10 && teamId.isNotBlank()
                        && state !is AuthState.Loading
            ) {
                if (state is AuthState.Loading)
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else
                    Text("Send OTP")
            }
        } else {
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) otp = it },
                label = { Text("Enter 6-digit OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { vm.verifyOtp(mobile, otp, teamId.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 6 && state !is AuthState.Loading
            ) {
                if (state is AuthState.Loading)
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else
                    Text("Verify & Login")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {
                otpSent = false; otp = ""; errorMsg = ""; vm.resetState()
            }) {
                Text("Change details", textAlign = TextAlign.Center)
            }
        }

        if (errorMsg.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
    }
}
