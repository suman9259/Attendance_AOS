package com.scharfesicht.attendencesystem.app.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object  RoutesConst {

    // Graphs
    const val AUTH_GRAPH = "AUTH_GRAPH"
    const val MAIN_GRAPH = "MAIN_GRAPH"
    const val SPLASH_GRAPH = "SPLASH_GRAPH"
    const val LIBRARY_GRAPH = "LIBRARY_GRAPH"
    const val STAFF_GRAPH = "STAFF_GRAPH"
    const val REPORT_GRAPH = "REPORT_GRAPH"

    // Splash & Onboarding
    const val SPLASH_SCREEN = "SPLASH_SCREEN"
    const val ONBOARDING_SCREEN = "ONBOARDING_SCREEN"

    const val WELCOME_SCREEN = "WELCOME_SCREEN"
    // Auth Screens
    const val LOGIN_SCREEN = "LOGIN_SCREEN"
    const val REGISTER_SCREEN = "REGISTER_SCREEN"
    const val SIGNUP_SCREEN = "SIGNUP_SCREEN"
    const val LOGIN_WITH_OTP_SCREEN = "LOGIN_WITH_OTP_SCREEN"
    const val SIGNUP_WITH_OTP_SCREEN = "SIGNUP_WITH_OTP_SCREEN"
    const val FORGOT_PASSWORD_SCREEN ="FORGOT_PASSWORD_SCREEN"
    const val RESET_PASSWORD_SCREEN = "RESET_PASSWORD_SCREEN"
    const val VERIFY_EMAIL_SCREEN = "VERIFY_EMAIL_SCREEN"
    const val VERIFY_OTP_SCREEN = "VERIFY_OTP_SCREEN"
    const val CHANGE_PASSWORD_SCREEN = "CHANGE_PASSWORD_SCREEN"
    const val SET_NEW_PASSWORD_SCREEN = "SET_NEW_PASSWORD_SCREEN"

    // Main Screens With BottomBarNavigation
    const val HOME_SCREEN = "HOME_SCREEN"
    const val LIBRARY_SCREEN = "LIBRARY_SCREEN"
    const val CLASSES_SCREEN = "CLASSES_SCREEN"
    const val PROGRESS_SCREEN = "PROGRESS_SCREEN"
    const val PROFILE_SCREEN = "PROFILE_SCREEN"

    // Progress
    const val ACHIEVEMENT_SCREEN = "ACHIEVEMENT_SCREEN"
    const val TROPHY_SCREEN = "TROPHY_SCREEN"

    // Library

    const val LEARN_LIST_SCREEN = "LEARN_LIST_SCREEN"
    const val LEARNING_LESSON_SCREEN = "LEARNING_LESSON_SCREEN"
    const val LEARN_DETAIL_SCREEN = "LEARN_DETAIL_SCREEN"
    const val DRAW_LIST_SCREEN = "DRAW_LIST_SCREEN"
    const val DRAW_DETAIL_SCREEN = "DRAW_DETAIL_SCREEN"

    // Live Class

    const val  CLASS_DETAILS_SCREEN = "CLASS_DETAILS_SCREEN"
    const val LIVE_CLASS_SCREEN = "LIVE_CLASS_SCREEN"

    const val LIVE_CLASSES_SCHEDULE_SCREEN = "LIVE_CLASSES_SCHEDULE_SCREEN"
//    const val LIVE_CLASSES_SCREEN = "LIVE_CLASSES_SCREEN"

    // Library Module

    // Profile & Settings
    const val EDIT_CHILD_PROFILE_SCREEN = "EDIT_CHILD_PROFILE_SCREEN"
    const val EDIT_PARENT_PROFILE_SCREEN = "EDIT_PARENT_PROFILE_SCREEN"
    const val ADD_CHILD_PROFILE_SCREEN = "ADD_CHILD_PROFILE_SCREEN"

    const val PARENT_PROFILE_SCREEN = "PARENT_PROFILE_SCREEN"
    const val ENTER_PIN_CODE_SCREEN = "ENTER_PIN_CODE_SCREEN"
    const val MANAGER_CHILDREN_SCREEN = "MANAGER_CHILDREN_SCREEN"
    const val BUSINESS_DETAILS_SCREEN = "BUSINESS_DETAILS_SCREEN"
    const val EDIT_BUSINESS_DETAILS_SCREEN = "EDIT_BUSINESS_DETAILS_SCREEN"
    const val SETTINGS_SCREEN = "SETTINGS_SCREEN"
    const val BACKUP_SCREEN = "BACKUP_SCREEN"
    const val SUBSCRIPTION_SCREEN = "SUBSCRIPTION_SCREEN"

    // Payment Module
    const val BILLING_HISTORY_SCREEN = "BILLING_HISTORY_SCREEN"
    const val PAYMENT_SCREEN = "PAYMENT_SCREEN"
    const val PAYMENT_METHOD_SCREEN = "PAYMENT_METHOD_SCREEN"
    const val SUBSCRIPTION_PLAN_SCREEN = "SUBSCRIPTION_PLAN_SCREEN"

    // Support & Others
    const val HELP_SCREEN = "HELP_SCREEN"
    const val FAQ_SCREEN = "FAQ_SCREEN"
    const val CONTACT_US_SCREEN = "CONTACT_US_SCREEN"
    const val ABOUT_US_SCREEN = "ABOUT_US_SCREEN"
    const val TERMS_AND_CONDITIONS_SCREEN = "TERMS_AND_CONDITIONS_SCREEN"
    const val PRIVACY_POLICY_SCREEN = "PRIVACY_POLICY_SCREEN"
    const val FEEDBACK_SCREEN = "FEEDBACK_SCREEN"

    // Arguments
    const val EXPENSE_ID = "expense_id"
    const val STAFF_ID = "staff_id"

    val EXPENSE_DETAIL_ARGUMENT = listOf(
        navArgument(EXPENSE_ID) { type = NavType.StringType }
    )

    val STAFF_DETAIL_ARGUMENT = listOf(
        navArgument(STAFF_ID) { type = NavType.StringType }
    )
}
