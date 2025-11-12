package com.scharfesicht.attendencesystem.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceDashboardScreen
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceDashboardViewModel

@Composable
fun AppNavGraph(
    isAbsherEnabled: Boolean = false,
    isLaunchedFromSuperApp: Boolean = false
) {
    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = "attendance_dashboard"
    ) {
        // Attendance Dashboard Screen
        composable("attendance_dashboard") {navBackStackEntry ->
            val viewModelAbsher: AttendanceDashboardViewModel = hiltViewModel(navBackStackEntry)
            AttendanceDashboardScreen(
                isAbsherEnabled = isAbsherEnabled,
                viewModel = viewModelAbsher,

            )
        }


    }
}
