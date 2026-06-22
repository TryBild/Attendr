package com.trybild.attendr.ui.employee

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.home.HomeScreen
import com.trybild.attendr.ui.home.HomeViewModel
import com.trybild.attendr.ui.myattendance.MyAttendanceScreen
import com.trybild.attendr.ui.theme.*

private enum class EmployeeTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("tab_home", "Home", Icons.Default.Home),
    DASHBOARD("tab_dashboard", "Dashboard", Icons.Default.Dashboard),
    CALENDAR("tab_calendar", "Calendar", Icons.Default.CalendarToday),
    PROFILE("tab_profile", "Profile", Icons.Default.Person)
}

@Composable
fun EmployeeShell(outerNavController: NavController) {
    // Single HomeViewModel instance for the shell — shared between Home and Dashboard
    // so there is only one location-tracking listener for the entire employee session.
    val homeVm: HomeViewModel = viewModel()
    val innerNav = rememberNavController()
    val backStackEntry by innerNav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = GlassSurface,
                tonalElevation = 0.dp
            ) {
                EmployeeTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                innerNav.navigate(tab.route) {
                                    popUpTo("tab_home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AttendrNavy,
                            selectedTextColor = AttendrNavy,
                            indicatorColor = AttendrNavy.copy(alpha = 0.12f),
                            unselectedIconColor = AttendrTextSecondary,
                            unselectedTextColor = AttendrTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = innerNav,
                startDestination = EmployeeTab.HOME.route
            ) {
                composable(EmployeeTab.HOME.route) {
                    HomeScreen(
                        navController = innerNav,
                        vm = homeVm,
                        onViewCalendar = {
                            innerNav.navigate(EmployeeTab.CALENDAR.route) {
                                popUpTo("tab_home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onOpenProfile = {
                            innerNav.navigate(EmployeeTab.PROFILE.route) {
                                popUpTo("tab_home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(EmployeeTab.DASHBOARD.route) {
                    EmployeeDashboardScreen(
                        innerNav = innerNav,
                        badge = homeVm.badge
                    )
                }
                composable(EmployeeTab.CALENDAR.route) {
                    MyAttendanceScreen(
                        navController = innerNav,
                        showBackButton = false
                    )
                }
                composable(EmployeeTab.PROFILE.route) {
                    ProfileScreen(outerNavController = outerNavController)
                }
            }
        }
    }
    }
}
