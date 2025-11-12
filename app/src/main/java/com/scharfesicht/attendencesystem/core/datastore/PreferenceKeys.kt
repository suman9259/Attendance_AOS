package com.scharfesicht.attendencesystem.core.datastore

import androidx.datastore.preferences.core.*

/**
 * Complete Preference Keys for Production App
 * Total: 100+ keys organized by category
 */
object PreferenceKeys {

    const val APP_DATASTORE_NAME = "meri_lipi_prefs"

    // ==================== AUTHENTICATION (6 keys) ====================
    val JWT_TOKEN = stringPreferencesKey("jwt_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_ROLE = stringPreferencesKey("user_role")

    // ==================== USER PROFILE (5 keys) ====================
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_AVATAR = stringPreferencesKey("user_avatar")
    val DATE_OF_BIRTH = stringPreferencesKey("date_of_birth")
    val GRADE = stringPreferencesKey("grade")
    val IS_PROFILE_COMPLETE = booleanPreferencesKey("is_profile_complete")

    // ==================== ONBOARDING (3 keys) ====================
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    val ONBOARDING_VERSION = intPreferencesKey("onboarding_version")
    val HAS_SEEN_WELCOME_SCREEN = booleanPreferencesKey("has_seen_welcome_screen")

    // ==================== APP SETTINGS (4 keys) ====================
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    val LANGUAGE = stringPreferencesKey("language")
    val FONT_SIZE = stringPreferencesKey("font_size")

    // ==================== SUBSCRIPTION (6 keys) ====================
    val HAS_ACTIVE_SUBSCRIPTION = booleanPreferencesKey("has_active_subscription")
    val SUBSCRIPTION_TYPE = stringPreferencesKey("subscription_type")
    val SUBSCRIPTION_START_DATE = longPreferencesKey("subscription_start_date")
    val SUBSCRIPTION_END_DATE = longPreferencesKey("subscription_end_date")
    val SUBSCRIPTION_AUTO_RENEW = booleanPreferencesKey("subscription_auto_renew")
    val TRIAL_USED = booleanPreferencesKey("trial_used")

    // ==================== PARENTAL CONTROLS (6 keys) ====================
    val IS_PARENT_LOCK_ENABLED = booleanPreferencesKey("is_parent_lock_enabled")
    val PARENT_PIN = stringPreferencesKey("parent_pin")
    val IS_PIN_VERIFIED = booleanPreferencesKey("is_pin_verified")
    val PIN_VERIFICATION_TIMESTAMP = longPreferencesKey("pin_verification_timestamp")
    val SCREEN_TIME_LIMIT = intPreferencesKey("screen_time_limit")
    val ALLOWED_APPS_TIME = stringPreferencesKey("allowed_apps_time")

    // ==================== LEARNING PREFERENCES (7 keys) ====================
    val PREFERRED_LEARNING_TIME = stringPreferencesKey("preferred_learning_time")
    val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_TIME = stringPreferencesKey("reminder_time")
    val LAST_COMPLETED_LESSON = stringPreferencesKey("last_completed_lesson")
    val CURRENT_STREAK = intPreferencesKey("current_streak")
    val LONGEST_STREAK = intPreferencesKey("longest_streak")

    // ==================== CONTENT PREFERENCES (6 keys) ====================
    val AUTO_PLAY_NEXT_LESSON = booleanPreferencesKey("auto_play_next_lesson")
    val VIDEO_QUALITY = stringPreferencesKey("video_quality")
    val DOWNLOAD_OVER_WIFI_ONLY = booleanPreferencesKey("download_over_wifi_only")
    val OFFLINE_MODE_ENABLED = booleanPreferencesKey("offline_mode_enabled")
    val SUBTITLES_ENABLED = booleanPreferencesKey("subtitles_enabled")
    val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")

    // ==================== NOTIFICATIONS (6 keys) ====================
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val PUSH_NOTIFICATIONS_ENABLED = booleanPreferencesKey("push_notifications_enabled")
    val EMAIL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("email_notifications_enabled")
    val CLASS_REMINDERS_ENABLED = booleanPreferencesKey("class_reminders_enabled")
    val ACHIEVEMENT_NOTIFICATIONS_ENABLED = booleanPreferencesKey("achievement_notifications_enabled")
    val PROGRESS_REPORTS_ENABLED = booleanPreferencesKey("progress_reports_enabled")

    // ==================== PRIVACY & SECURITY (5 keys) ====================
    val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
    val LOCATION_SHARING_ENABLED = booleanPreferencesKey("location_sharing_enabled")
    val BIOMETRIC_AUTH_ENABLED = booleanPreferencesKey("biometric_auth_enabled")
    val LAST_PASSWORD_CHANGE_DATE = longPreferencesKey("last_password_change_date")

    // ==================== APP USAGE & ANALYTICS (6 keys) ====================
    val APP_OPEN_COUNT = intPreferencesKey("app_open_count")
    val LAST_APP_OPEN_DATE = longPreferencesKey("last_app_open_date")
    val TOTAL_LEARNING_TIME_MINUTES = intPreferencesKey("total_learning_time_minutes")
    val LESSONS_COMPLETED = intPreferencesKey("lessons_completed")
    val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    val APP_VERSION = stringPreferencesKey("app_version")

    // ==================== LIVE CLASS PREFERENCES (5 keys) ====================
    val AUTO_JOIN_SCHEDULED_CLASSES = booleanPreferencesKey("auto_join_scheduled_classes")
    val CAMERA_DEFAULT_ON = booleanPreferencesKey("camera_default_on")
    val MIC_DEFAULT_ON = booleanPreferencesKey("mic_default_on")
    val LAST_JOINED_CLASS_ID = stringPreferencesKey("last_joined_class_id")
    val CLASS_NOTIFICATION_LEAD_TIME = intPreferencesKey("class_notification_lead_time")

    // ==================== PAYMENT & BILLING (4 keys) ====================
    val SAVED_PAYMENT_METHOD = stringPreferencesKey("saved_payment_method")
    val DEFAULT_PAYMENT_METHOD = stringPreferencesKey("default_payment_method")
    val BILLING_ADDRESS = stringPreferencesKey("billing_address")
    val AUTO_PAY_ENABLED = booleanPreferencesKey("auto_pay_enabled")

    // ==================== ACHIEVEMENTS & REWARDS (4 keys) ====================
    val TOTAL_POINTS = intPreferencesKey("total_points")
    val LEVEL = intPreferencesKey("level")
    val BADGES = stringSetPreferencesKey("badges")
    val LAST_REWARD_CLAIMED_DATE = longPreferencesKey("last_reward_claimed_date")

    // ==================== DOWNLOAD MANAGEMENT (4 keys) ====================
    val DOWNLOADED_CONTENT_IDS = stringSetPreferencesKey("downloaded_content_ids")
    val TOTAL_DOWNLOADED_SIZE_MB = longPreferencesKey("total_downloaded_size_mb")
    val AUTO_DELETE_OLD_DOWNLOADS = booleanPreferencesKey("auto_delete_old_downloads")
    val MAX_DOWNLOAD_SIZE_MB = longPreferencesKey("max_download_size_mb")

    // ==================== ACCESSIBILITY (4 keys) ====================
    val TEXT_TO_SPEECH_ENABLED = booleanPreferencesKey("text_to_speech_enabled")
    val HIGH_CONTRAST_MODE = booleanPreferencesKey("high_contrast_mode")
    val REDUCED_MOTION_ENABLED = booleanPreferencesKey("reduced_motion_enabled")
    val SCREEN_READER_ENABLED = booleanPreferencesKey("screen_reader_enabled")

    // ==================== EXPERIMENTAL FEATURES (3 keys) ====================
    val BETA_FEATURES_ENABLED = booleanPreferencesKey("beta_features_enabled")
    val DEBUG_MODE_ENABLED = booleanPreferencesKey("debug_mode_enabled")
    val FEATURE_FLAGS = stringPreferencesKey("feature_flags")

    // ==================== CACHE & DATA (3 keys) ====================
    val LAST_CACHE_CLEAR_DATE = longPreferencesKey("last_cache_clear_date")
    val CACHE_SIZE = longPreferencesKey("cache_size")
    val DATA_SAVER_MODE = booleanPreferencesKey("data_saver_mode")

    // ==================== ADDITIONAL USEFUL KEYS ====================

    // Session Management
    val SESSION_ID = stringPreferencesKey("session_id")
    val LAST_ACTIVE_TIMESTAMP = longPreferencesKey("last_active_timestamp")
    val SESSION_TIMEOUT_MINUTES = intPreferencesKey("session_timeout_minutes")

    // Device Info
    val DEVICE_ID = stringPreferencesKey("device_id")
    val FCM_TOKEN = stringPreferencesKey("fcm_token")
    val DEVICE_NAME = stringPreferencesKey("device_name")

    // App State
    val FIRST_INSTALL_DATE = longPreferencesKey("first_install_date")
    val LAST_UPDATE_DATE = longPreferencesKey("last_update_date")
    val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count")

    // User Preferences - Display
    val SHOW_TUTORIAL_HINTS = booleanPreferencesKey("show_tutorial_hints")
    val ANIMATION_ENABLED = booleanPreferencesKey("animation_enabled")
    val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
    val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")

    // Network Preferences
    val PREFERRED_NETWORK_TYPE = stringPreferencesKey("preferred_network_type")
    val SYNC_OVER_MOBILE_DATA = booleanPreferencesKey("sync_over_mobile_data")
    val AUTO_DOWNLOAD_UPDATES = booleanPreferencesKey("auto_download_updates")

    // Backup & Sync
    val LAST_BACKUP_DATE = longPreferencesKey("last_backup_date")
    val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
    val BACKUP_FREQUENCY_DAYS = intPreferencesKey("backup_frequency_days")
    val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")

    // Privacy Settings
    val SHARE_USAGE_DATA = booleanPreferencesKey("share_usage_data")
    val PERSONALIZED_ADS = booleanPreferencesKey("personalized_ads")
    val ACTIVITY_TRACKING_ENABLED = booleanPreferencesKey("activity_tracking_enabled")

    // Lesson Progress
    val LAST_WATCHED_VIDEO_ID = stringPreferencesKey("last_watched_video_id")
    val LAST_VIDEO_POSITION_MS = longPreferencesKey("last_video_position_ms")
    val COMPLETED_LESSON_IDS = stringSetPreferencesKey("completed_lesson_ids")
    val BOOKMARKED_LESSON_IDS = stringSetPreferencesKey("bookmarked_lesson_ids")

    // Study Schedule
    val STUDY_START_TIME = stringPreferencesKey("study_start_time")
    val STUDY_END_TIME = stringPreferencesKey("study_end_time")
    val STUDY_DAYS = stringSetPreferencesKey("study_days")
    val BREAK_DURATION_MINUTES = intPreferencesKey("break_duration_minutes")

    // Performance
    val LOW_POWER_MODE = booleanPreferencesKey("low_power_mode")
    val REDUCE_BACKGROUND_ACTIVITY = booleanPreferencesKey("reduce_background_activity")
    val BATTERY_OPTIMIZATION_ENABLED = booleanPreferencesKey("battery_optimization_enabled")

    // Social Features
    val SHOW_PROFILE_TO_OTHERS = booleanPreferencesKey("show_profile_to_others")
    val ALLOW_FRIEND_REQUESTS = booleanPreferencesKey("allow_friend_requests")
    val SHARE_PROGRESS_PUBLICLY = booleanPreferencesKey("share_progress_publicly")

    // Help & Support
    val LAST_FEEDBACK_DATE = longPreferencesKey("last_feedback_date")
    val SHOW_HELP_TOOLTIPS = booleanPreferencesKey("show_help_tooltips")
    val FAQ_LAST_VIEWED_DATE = longPreferencesKey("faq_last_viewed_date")

    // Rate & Review
    val APP_RATED = booleanPreferencesKey("app_rated")
    val RATE_PROMPT_COUNT = intPreferencesKey("rate_prompt_count")
    val LAST_RATE_PROMPT_DATE = longPreferencesKey("last_rate_prompt_date")

    // A/B Testing
    val AB_TEST_VARIANT = stringPreferencesKey("ab_test_variant")
    val AB_TEST_GROUP = stringPreferencesKey("ab_test_group")

    // Referral & Marketing
    val REFERRAL_CODE = stringPreferencesKey("referral_code")
    val REFERRED_BY = stringPreferencesKey("referred_by")
    val PROMO_CODE_USED = stringPreferencesKey("promo_code_used")

    // Emergency Contact (for parental controls)
    val EMERGENCY_CONTACT_NAME = stringPreferencesKey("emergency_contact_name")
    val EMERGENCY_CONTACT_PHONE = stringPreferencesKey("emergency_contact_phone")
    val EMERGENCY_CONTACT_EMAIL = stringPreferencesKey("emergency_contact_email")

    // Content Filters
    val CONTENT_MATURITY_LEVEL = stringPreferencesKey("content_maturity_level")
    val BLOCKED_TOPICS = stringSetPreferencesKey("blocked_topics")
    val SAFE_SEARCH_ENABLED = booleanPreferencesKey("safe_search_enabled")
}