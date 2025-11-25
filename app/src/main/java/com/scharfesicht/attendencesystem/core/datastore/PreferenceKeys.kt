package com.scharfesicht.attendencesystem.core.datastore

import androidx.datastore.preferences.core.*

/**
 * Complete Preference Keys for Attendance App
 */
object PreferenceKeys {
    const val APP_DATASTORE_NAME = "attendance_app_prefs"

    // ==================== AUTHENTICATION (6 keys) ====================
    val JWT_TOKEN = stringPreferencesKey("jwt_token")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_ROLE = stringPreferencesKey("user_role")

    // ==================== USER PROFILE (5 keys) ====================
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_AVATAR = stringPreferencesKey("user_avatar")
    val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
    val DATE_OF_BIRTH = stringPreferencesKey("date_of_birth")
    val GRADE = stringPreferencesKey("grade")

    // ==================== ATTENDANCE DATA ====================
    // Current Shift Data (JSON string)
    val CURRENT_SHIFT_JSON = stringPreferencesKey("current_shift_json")

    // Zone/Location Data
    val ZONE_LATITUDE = stringPreferencesKey("zone_latitude")
    val ZONE_LONGITUDE = stringPreferencesKey("zone_longitude")
    val ZONE_RADIUS = stringPreferencesKey("zone_radius")
    val ZONE_NAME = stringPreferencesKey("zone_name")
    val ZONE_ID = intPreferencesKey("zone_id")

    // Check-in Status
    val IS_CHECKED_IN = booleanPreferencesKey("is_checked_in")
    val LAST_CHECKIN_TIME = longPreferencesKey("last_checkin_time")

    // User Shifts Data (JSON array string)
    val USER_SHIFTS_JSON = stringPreferencesKey("user_shifts_json")
    val SHIFTS_LAST_UPDATED = longPreferencesKey("shifts_last_updated")

    // ==================== APP SETTINGS (4 keys) ====================
    val LANGUAGE = stringPreferencesKey("language")
}