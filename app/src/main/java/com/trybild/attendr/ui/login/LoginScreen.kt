package com.trybild.attendr.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val vm: AuthViewModel = viewModel()
    val state by vm.state.collectAsState()
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
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
        Text("Attendr", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("Digital Attendance Register", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = phone, onValueChange = { phone = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            enabled = !otpSent
        )
        Spacer(Modifier.height(12.dp))

        if (!otpSent) {
            Button(
                onClick = { vm.requestOtp(phone) },
                modifier = Modifier.fillMaxWidth(),
                enabled = phone.length >= 10 && state !is AuthState.Loading
            ) {
                if (state is AuthState.Loading) CircularProgressIndicator(Modifier.size(20.dp))
                else Text("Send OTP")
            }
        } else {
            OutlinedTextField(
                value = otp, onValueChange = { otp = it },
                label = { Text("Enter OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = companyName, onValueChange = { companyName = it },
                label = { Text("Company Name (new admin only)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { vm.verifyOtp(phone, otp, companyName) },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 6 && state !is AuthState.Loading
            ) {
                if (state is AuthState.Loading) CircularProgressIndicator(Modifier.size(20.dp))
                else Text("Verify & Login")
            }
        }

        if (errorMsg.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }
    }
}
