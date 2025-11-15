package com.scharfesicht.attendencesystem.app.navigation


sealed class ScreenRoutes(val route: String) {
    // Auth Flow
    object Login : ScreenRoutes("login")
    object Splash : ScreenRoutes("splash")

    // Main Flow
    object Home : ScreenRoutes("home")
    object AttendanceDashboard : ScreenRoutes("attendance_dashboard")
    object AttendanceLogs : ScreenRoutes("attendance_logs")
    object FaceRecognition : ScreenRoutes("face_recognition") {
        const val ARG_IS_CHECK_IN = "is_check_in"
        fun createRoute(isCheckIn: Boolean) = "$route?$ARG_IS_CHECK_IN=$isCheckIn"
    }
    object FaceRecognitionSuccess : ScreenRoutes("face_recognition_success") {
        const val ARG_MESSAGE = "message"
        fun createRoute(message: String) = "$route?$ARG_MESSAGE=$message"
    }
    object FaceRecognitionFailed : ScreenRoutes("face_recognition_failed")

    // Settings
    object Settings : ScreenRoutes("settings")
    object Profile : ScreenRoutes("profile")

    // Face Compare (for testing)
    object FaceCompare : ScreenRoutes("face_compare") {
        const val ARG_IMAGE_URL = "image_url"
        fun createRoute(imageUrl: String) = "$route?$ARG_IMAGE_URL=$imageUrl"
    }
}
