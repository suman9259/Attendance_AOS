package com.scharfesicht.attendencesystem.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceDashboardScreen
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceListScreen
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AbsherViewModel
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceDashboardViewModel
import com.scharfesicht.attendencesystem.features.facecompare.presentation.viewmodel.FaceCompareViewModel

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
            val absherViewModel: AbsherViewModel = hiltViewModel(navBackStackEntry)
            val attendanceViewModel: AttendanceDashboardViewModel = hiltViewModel(navBackStackEntry)
            val faceCompareViewModel: FaceCompareViewModel = hiltViewModel(navBackStackEntry)
            AttendanceListScreen()
//            FaceCompareScreen(
//                oldImageUrl = "https://hrmpro.time-365.com/storage/images/profile/time-365_188264/537304871511132025095607691581079ddb1.jpg",
//                viewModel = viewModel,
//
//
//            )
        }


    }
}
