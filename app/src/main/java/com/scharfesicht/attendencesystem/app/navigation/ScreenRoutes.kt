package com.scharfesicht.attendencesystem.app.navigation


sealed class ScreenRoutes(val route: String) {

    // Main Flow
    object Home : ScreenRoutes("home")
    object AttendanceDashboard : ScreenRoutes("attendance_dashboard")
    object AttendanceLogs : ScreenRoutes("attendance_logs")

    object FaceRecognitionSuccess : ScreenRoutes("face_recognition_success") {
        const val IS_SUCCESS = "is_success"
        const val IS_IN = "is_in"

        fun createRoute(isSuccess: Boolean, isIn: Boolean): String {
            return "$route?$IS_SUCCESS=$isSuccess&$IS_IN=$isIn"
        }
    }
    object FaceRecognitionFailed : ScreenRoutes("face_recognition_failed")
}
