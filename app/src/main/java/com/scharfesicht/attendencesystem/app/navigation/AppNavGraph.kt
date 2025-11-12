package com.scharfesicht.attendencesystem.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceDashboardScreen
import com.scharfesicht.attendencesystem.features.home.ui.AbsherUserScreen
import com.scharfesicht.attendencesystem.features.home.ui.HomeScreen

@Composable
fun AppNavGraph(
    isAbsherEnabled: Boolean = false,
    isLaunchedFromSuperApp: Boolean = false
) {
    val navController = rememberNavController()

    // Determine start destination
    val startDestination = if (isLaunchedFromSuperApp && isAbsherEnabled) {
        "absher_user"
    } else {
        "home"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable("home") {
            HomeScreen(
                navController = navController,
                isAbsherEnabled = isAbsherEnabled
            )
        }

        // Absher User Info Screen
        composable("absher_user") {
            AbsherUserScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        // Attendance Dashboard Screen
        composable("attendance_dashboard") {
            AttendanceDashboardScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Reports Screen (placeholder)
        composable("reports") {
            // TODO: Implement ReportsScreen
            // ReportsScreen(navController = navController)
        }

        // Settings Screen (placeholder)
        composable("settings") {
            // TODO: Implement SettingsScreen
            // SettingsScreen(navController = navController)
        }

        // Profile Screen (placeholder)
        composable("profile") {
            // TODO: Implement ProfileScreen
            // ProfileScreen(navController = navController)
        }
    }
}

// Navigation Routes (sealed class for type safety)
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AbsherUser : Screen("absher_user")
    object AttendanceDashboard : Screen("attendance_dashboard")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
}