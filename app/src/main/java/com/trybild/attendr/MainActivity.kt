package com.trybild.attendr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.ui.home.HomeScreen
import com.trybild.attendr.ui.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface { AppNav() }
            }
        }
    }
}

@Composable
fun AppNav() {
    val context = LocalContext.current
    val dataStore = remember { TokenDataStore(context) }
    val token by dataStore.token.collectAsState(initial = null)
    var checked by remember { mutableStateOf(false) }
    LaunchedEffect(token) { checked = true }
    if (checked) {
        if (token != null) HomeScreen()
        else LoginScreen(onLoginSuccess = {})
    }
}
