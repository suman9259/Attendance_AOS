package com.scharfesicht.attendencesystem.app.navigation

sealed class ScreenRoutes(val route: String) {

    // Graphs
    object Auth : ScreenRoutes(RoutesConst.AUTH_GRAPH)
    object Main : ScreenRoutes(RoutesConst.MAIN_GRAPH)
    object SplashGraph : ScreenRoutes(RoutesConst.SPLASH_GRAPH)
    object StaffGraph : ScreenRoutes(RoutesConst.STAFF_GRAPH)
    object ReportGraph : ScreenRoutes(RoutesConst.REPORT_GRAPH)

    // Splash & Onboarding
    object Splash : ScreenRoutes(RoutesConst.SPLASH_SCREEN)
    object Onboarding : ScreenRoutes(RoutesConst.ONBOARDING_SCREEN)

    object Welcome : ScreenRoutes(RoutesConst.WELCOME_SCREEN)
    // Auth Screens
    object Login : ScreenRoutes(RoutesConst.LOGIN_SCREEN)
    object SignUp : ScreenRoutes(RoutesConst.SIGNUP_SCREEN)
    object ForgotPassword : ScreenRoutes(RoutesConst.FORGOT_PASSWORD_SCREEN)
    object ResetPassword : ScreenRoutes(RoutesConst.RESET_PASSWORD_SCREEN)
    object VerifyEmail : ScreenRoutes(RoutesConst.VERIFY_EMAIL_SCREEN)
    object VerifyOTP : ScreenRoutes(RoutesConst.VERIFY_OTP_SCREEN)
    object ChangePassword : ScreenRoutes(RoutesConst.CHANGE_PASSWORD_SCREEN)
    object SetNewPassword : ScreenRoutes(RoutesConst.SET_NEW_PASSWORD_SCREEN)

    // Main
    object Home : ScreenRoutes(RoutesConst.HOME_SCREEN)
    object Library : ScreenRoutes(RoutesConst.LIBRARY_SCREEN)
    object Classes : ScreenRoutes(RoutesConst.CLASSES_SCREEN)
    object Progress : ScreenRoutes(RoutesConst.PROGRESS_SCREEN)
    object Profile : ScreenRoutes(RoutesConst.PROFILE_SCREEN)

    // Progress
    object Achievement : ScreenRoutes(RoutesConst.ACHIEVEMENT_SCREEN)
    object Trophy : ScreenRoutes(RoutesConst.TROPHY_SCREEN)


    // Library
    object LibraryGraph : ScreenRoutes(RoutesConst.LIBRARY_GRAPH)
    object LearnList : ScreenRoutes(RoutesConst.LEARN_LIST_SCREEN)
    object LearningLesson : ScreenRoutes(RoutesConst.LEARNING_LESSON_SCREEN)
    object LearnDetail : ScreenRoutes(RoutesConst.LEARN_DETAIL_SCREEN)

    object DrawList : ScreenRoutes(RoutesConst.DRAW_LIST_SCREEN)
    object DrawDetail : ScreenRoutes(RoutesConst.DRAW_DETAIL_SCREEN)




    // Live Class
    object ClassDetail : ScreenRoutes(RoutesConst.CLASS_DETAILS_SCREEN)
    object LiveClass : ScreenRoutes(RoutesConst.LIVE_CLASS_SCREEN)

    object LiveClassesSchedule : ScreenRoutes(RoutesConst.LIVE_CLASSES_SCHEDULE_SCREEN)
//    object LiveClassDetail : ScreenRoutes(RoutesConst.LIVE_CLASS_DETAIL_SCREEN)



    // Profile & Settings

    object EditChildProfile : ScreenRoutes(RoutesConst.EDIT_CHILD_PROFILE_SCREEN)
    object EditParentProfile : ScreenRoutes(RoutesConst.EDIT_PARENT_PROFILE_SCREEN)
    object AddChildProfile : ScreenRoutes(RoutesConst.ADD_CHILD_PROFILE_SCREEN)

    object ParentProfile : ScreenRoutes(RoutesConst.PARENT_PROFILE_SCREEN)
    object EnterPinCode : ScreenRoutes(RoutesConst.ENTER_PIN_CODE_SCREEN)
    object ManagerChildren : ScreenRoutes(RoutesConst.MANAGER_CHILDREN_SCREEN)
    object BusinessDetails : ScreenRoutes(RoutesConst.BUSINESS_DETAILS_SCREEN)
    object EditBusinessDetails : ScreenRoutes(RoutesConst.EDIT_BUSINESS_DETAILS_SCREEN)
    object Settings : ScreenRoutes(RoutesConst.SETTINGS_SCREEN)
    object Backup : ScreenRoutes(RoutesConst.BACKUP_SCREEN)
    object Subscription : ScreenRoutes(RoutesConst.SUBSCRIPTION_SCREEN)

    // Payment Module

    object BillingHistory : ScreenRoutes(RoutesConst.BILLING_HISTORY_SCREEN)
    object Payment : ScreenRoutes(RoutesConst.PAYMENT_SCREEN)
    object PaymentMethod : ScreenRoutes(RoutesConst.PAYMENT_METHOD_SCREEN)
    object SubscriptionPlan : ScreenRoutes(RoutesConst.SUBSCRIPTION_PLAN_SCREEN)


    // Support
    object Help : ScreenRoutes(RoutesConst.HELP_SCREEN)
    object FAQ : ScreenRoutes(RoutesConst.FAQ_SCREEN)
    object ContactUs : ScreenRoutes(RoutesConst.CONTACT_US_SCREEN)
    object AboutUs : ScreenRoutes(RoutesConst.ABOUT_US_SCREEN)
    object TermsAndConditions : ScreenRoutes(RoutesConst.TERMS_AND_CONDITIONS_SCREEN)
    object PrivacyPolicy : ScreenRoutes(RoutesConst.PRIVACY_POLICY_SCREEN)
    object Feedback : ScreenRoutes(RoutesConst.FEEDBACK_SCREEN)

    // Support module routes
    object SupportCenter : ScreenRoutes("support_center")
    object ChatSupport : ScreenRoutes("chat_support")
    object VideoTutorials : ScreenRoutes("video_tutorials_screen")
    object ContactSupport : ScreenRoutes("contact_support")
}
