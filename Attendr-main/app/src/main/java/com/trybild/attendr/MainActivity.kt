package com.trybild.attendr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trybild.attendr.data.local.TokenDataStore
import com.trybild.attendr.ui.admin.AdminLoginScreen
import com.trybild.attendr.ui.admin.AdminRegisterScreen
import com.trybild.attendr.ui.help.ArticleScreen
import com.trybild.attendr.ui.help.HelpCenterScreen
import com.trybild.attendr.ui.home.AdminDashboardScreen
import com.trybild.attendr.ui.home.EmployeeHomeScreen
import com.trybild.attendr.ui.legal.DataUsageScreen
import com.trybild.attendr.ui.legal.PrivacyScreen
import com.trybild.attendr.ui.legal.TermsScreen
import com.trybild.attendr.ui.onboarding.RoleSelectionScreen
import com.trybild.attendr.ui.register.EmployeeRegisterScreen
import com.trybild.attendr.ui.support.ContactSupportScreen
import com.trybild.attendr.ui.theme.AttendrTheme
import com.trybild.attendr.ui.welcome.WelcomeScreen
import com.trybild.attendr.utils.JwtUtils
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttendrTheme {
                Surface { AppNav() }
            }
        }
    }
}

@Composable
fun AppNav() {
    val context = LocalContext.current
    val dataStore = remember { TokenDataStore(context) }
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = dataStore.token.first()
        startDestination = when {
            token == null -> "role_selection"
            JwtUtils.decodeTokenKind(token) == "admin" -> "admin_dashboard"
            else -> "employee_home"
        }
    }

    if (startDestination == null) return

    val navController = rememberNavController()

    fun navigateClearingBackStack(route: String) {
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = startDestination!!) {

        composable("welcome") {
            WelcomeScreen(
                onContinue = { navController.navigate("role_selection") },
                onNavigateToSupport = { navController.navigate("support") },
                onNavigateToHelp = { navController.navigate("help") },
                onNavigateToTerms = { navController.navigate("legal/terms") },
                onNavigateToPrivacy = { navController.navigate("legal/privacy") },
                onNavigateToDataUsage = { navController.navigate("legal/data-usage") }
            )
        }

        composable("role_selection") {
            RoleSelectionScreen(
                onContinueAsAdmin = { navController.navigate("admin_login") },
                onContinueAsEmployee = { navController.navigate("employee_register") }
            )
        }

        composable("employee_register") {
            EmployeeRegisterScreen(
                onBackFromFirstStep = { navController.popBackStack() },
                onRegistered = { navigateClearingBackStack("employee_home") }
            )
        }

        composable("admin_login") {
            AdminLoginScreen(
                onLoggedIn = { navigateClearingBackStack("admin_dashboard") },
                onCreateAccount = { navController.navigate("admin_register") }
            )
        }

        composable("admin_register") {
            AdminRegisterScreen(
                onBack = { navController.popBackStack() },
                onRegistered = { navigateClearingBackStack("admin_dashboard") }
            )
        }

        composable("admin_dashboard") {
            AdminDashboardScreen()
        }

        composable("employee_home") {
            EmployeeHomeScreen()
        }

        composable("help") {
            HelpCenterScreen(navController = navController)
        }

        composable(
            route = "help/article/{slug}",
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStackEntry ->
            ArticleScreen(
                slug = backStackEntry.arguments?.getString("slug") ?: "",
                navController = navController
            )
        }

        composable(
            route = "support?issue={issue}",
            arguments = listOf(navArgument("issue") { defaultValue = "" })
        ) { backStackEntry ->
            ContactSupportScreen(
                navController = navController,
                prefillIssue = backStackEntry.arguments?.getString("issue") ?: ""
            )
        }

        composable("legal/terms") {
            TermsScreen(navController = navController)
        }

        composable("legal/privacy") {
            PrivacyScreen(navController = navController)
        }

        composable("legal/data-usage") {
            DataUsageScreen(navController = navController)
        }
    }
}
