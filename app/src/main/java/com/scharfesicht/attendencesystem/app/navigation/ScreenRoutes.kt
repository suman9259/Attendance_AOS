package com.scharfesicht.attendencesystem.app.navigation


sealed class ScreenRoutes(val route: String) {

    // Main Flow
    object Home : ScreenRoutes("home")
    object AttendanceDashboard : ScreenRoutes("attendance_dashboard")
    object AttendanceLogs : ScreenRoutes("attendance_logs")

    object FaceRecognitionSuccess : ScreenRoutes("face_recognition_success") {
        const val IS_SUCCESS = "is_success"
        fun createRoute(message: String) = "$route?$IS_SUCCESS=$message"
    }
    object FaceRecognitionFailed : ScreenRoutes("face_recognition_failed")
}
