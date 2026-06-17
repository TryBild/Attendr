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
import com.trybild.attendr.ui.admin.AdminEmployeesScreen
import com.trybild.attendr.ui.admin.AdminHomeScreen
import com.trybild.attendr.ui.admin.AdminLoginScreen
import com.trybild.attendr.ui.admin.AdminRegisterScreen
import com.trybild.attendr.ui.admin.AdminSetupScreen
import com.trybild.attendr.ui.admin.AttendanceCalendarScreen
import com.trybild.attendr.ui.help.ArticleScreen
import com.trybild.attendr.ui.help.HelpCenterScreen
import com.trybild.attendr.ui.home.HomeScreen
import com.trybild.attendr.ui.myattendance.MyAttendanceScreen
import com.trybild.attendr.ui.legal.DataUsageScreen
import com.trybild.attendr.ui.legal.PrivacyScreen
import com.trybild.attendr.ui.legal.TermsScreen
import com.trybild.attendr.ui.register.OtpScreen
import com.trybild.attendr.ui.register.RegisterScreen
import com.trybild.attendr.ui.roleselection.RoleSelectionScreen
import com.trybild.attendr.ui.support.ContactSupportScreen
import com.trybild.attendr.ui.theme.AttendrTheme
import com.trybild.attendr.ui.welcome.WelcomeScreen
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
        val kind = dataStore.userKind.first()
        val setup = dataStore.setupComplete.first()
        startDestination = when {
            token == null -> "welcome"
            kind == "admin" && !setup -> "admin_setup"
            kind == "admin" -> "admin_home"
            else -> "home"
        }
    }

    if (startDestination == null) return

    val navController = rememberNavController()
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
            RoleSelectionScreen(navController = navController)
        }

        composable("admin_login") {
            AdminLoginScreen(navController = navController)
        }

        composable("admin_register") {
            AdminRegisterScreen(navController = navController)
        }

        composable("admin_setup") {
            AdminSetupScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable(
            route = "otp/{phone}?name={name}&orgId={orgId}",
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("name") { defaultValue = "" },
                navArgument("orgId") { defaultValue = "" }
            )
        ) { backStackEntry ->
            OtpScreen(
                phone = backStackEntry.arguments?.getString("phone") ?: "",
                name = backStackEntry.arguments?.getString("name") ?: "",
                orgId = backStackEntry.arguments?.getString("orgId") ?: "",
                navController = navController
            )
        }

        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("my_attendance") {
            MyAttendanceScreen(navController = navController)
        }

        composable("admin_home") {
            AdminHomeScreen(navController = navController)
        }

        composable("admin_employees") {
            AdminEmployeesScreen(navController = navController)
        }

        composable("admin_attendance") {
            AttendanceCalendarScreen(navController = navController)
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
