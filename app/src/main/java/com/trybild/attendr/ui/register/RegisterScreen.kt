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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.R
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrTextField
import com.trybild.attendr.ui.components.ErrorToast
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.legal.LegalConsentText
import com.trybild.attendr.ui.theme.AttendrTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val vm: RegisterViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var orgId by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is RegisterState.OtpSent -> {
                navController.navigate(
                    "otp/${Uri.encode(s.mobile)}?name=${Uri.encode(name)}&orgId=${Uri.encode(orgId)}"
                )
                vm.resetState()
            }
            is RegisterState.Error -> {
                errorMsg = s.message
                showError = true
                vm.resetState()
            }
            else -> {}
        }
    }

    val isValid = name.isNotBlank() && phone.length == 10 && orgId.isNotBlank()

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                    stringResource(R.string.register_title),
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = AttendrTextPrimary
                )
                Spacer(Modifier.height(32.dp))

                AttendrTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.label_full_name),
                    placeholder = stringResource(R.string.placeholder_full_name)
                )
                Spacer(Modifier.height(20.dp))

                AttendrTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) phone = it },
                    label = stringResource(R.string.label_mobile_number),
                    placeholder = stringResource(R.string.placeholder_mobile_number),
                    helperText = stringResource(R.string.helper_mobile_otp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(stringResource(R.string.country_code), style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outline))
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )
                Spacer(Modifier.height(20.dp))

                AttendrTextField(
                    value = orgId,
                    onValueChange = { orgId = it; showError = false },
                    label = stringResource(R.string.label_org_id),
                    placeholder = stringResource(R.string.placeholder_org_id)
                )
                Spacer(Modifier.height(24.dp))

                AttendrButton(
                    text = if (state is RegisterState.Loading)
                        stringResource(R.string.btn_sending_otp)
                    else
                        stringResource(R.string.btn_continue),
                    onClick = { vm.requestOtp(name.trim(), phone, orgId.trim()) },
                    enabled = isValid && state !is RegisterState.Loading
                )

                Spacer(Modifier.height(12.dp))

                LegalConsentText(modifier = Modifier.fillMaxWidth())
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
