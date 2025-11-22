package com.scharfesicht.attendencesystem.app.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scharfesicht.attendencesystem.core.di.NavManagerEntryPoint
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.*
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.*
import dagger.hilt.android.EntryPointAccessors

@Composable
fun AppNavGraph() {
    val context = LocalContext.current.applicationContext
    val navManager = remember {
        EntryPointAccessors.fromApplication(
            context,
            NavManagerEntryPoint::class.java
        ).navManager()
    }

    val navController = rememberNavController()

    // Set NavController in NavManager
    DisposableEffect(navController) {
        navManager.setNavController(navController)
        onDispose {
            navManager.clearNavController()
        }
    }


    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.AttendanceDashboard.route
    ) {

        // ========== ATTENDANCE DASHBOARD ==========
        composable(ScreenRoutes.AttendanceDashboard.route) { navBackStackEntry ->
            val viewModel: AttendanceDashboardViewModel = hiltViewModel(navBackStackEntry)

            AttendanceDashboardScreen(
                navManager = navManager,
                viewModel = viewModel
            )
        }

        // ========== ATTENDANCE LOGS ==========
        composable(ScreenRoutes.AttendanceLogs.route) { navBackStackEntry ->
            val viewModel: AttendanceLogsViewModel = hiltViewModel(navBackStackEntry)

            AttendanceLogsScreen(
                navManager = navManager,
                viewModel = viewModel
            )
        }


        // ========== FACE RECOGNITION SUCCESS ==========
        composable(
            route = "${ScreenRoutes.FaceRecognitionSuccess.route}?${ScreenRoutes.FaceRecognitionSuccess.ARG_MESSAGE}={message}",
            arguments = listOf(
                navArgument(ScreenRoutes.FaceRecognitionSuccess.ARG_MESSAGE) {
                    type = NavType.StringType
                    defaultValue = "Successfully punched in"
                }
            )
        ) { navBackStackEntry ->
            val message = navBackStackEntry.arguments
                ?.getString(ScreenRoutes.FaceRecognitionSuccess.ARG_MESSAGE)
                ?: "Successfully punched in"

            FaceRecognitionResultScreen(
                navController = navController,
                isSuccess = true,
                message = message,
                onDismiss = {
                    navManager.navigateBack()
                }
            )
        }

        // ========== FACE RECOGNITION FAILED ==========
        composable(ScreenRoutes.FaceRecognitionFailed.route) {
            FaceNotRecognizedScreen(
                navController = navController,
                onTryAgain = {
                    navManager.navigateBack()
                }
            )
        }

    }
}