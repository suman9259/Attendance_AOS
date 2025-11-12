package com.scharfesicht.attendencesystem.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceStorageImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : IPreferenceStorage {

    private val json = Json { ignoreUnknownKeys = true }

    // Helper function to handle IO exceptions
    private fun <T> Flow<Preferences>.mapWithCatch(
        default: T,
        mapper: (Preferences) -> T
    ): Flow<T> = this.catch { exception ->
        if (exception is IOException) {
            Log.e(TAG, "Error reading preferences", exception)
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map(mapper)

    // ==================== AUTHENTICATION ====================

    override val jwtToken: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.JWT_TOKEN] }

    override val refreshToken: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.REFRESH_TOKEN] }

    override val isLoggedIn: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.IS_LOGGED_IN] ?: false }

    override val userId: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.USER_ID] }

    override val userEmail: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.USER_EMAIL] }

    override val userRole: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.USER_ROLE] }

    override suspend fun saveJwtToken(token: String) {
        safeEdit { it[PreferenceKeys.JWT_TOKEN] = token }
    }

    override suspend fun saveRefreshToken(token: String) {
        safeEdit { it[PreferenceKeys.REFRESH_TOKEN] = token }
    }

    override suspend fun setLoggedIn(value: Boolean) {
        safeEdit { it[PreferenceKeys.IS_LOGGED_IN] = value }
    }

    override suspend fun saveUserId(id: String) {
        safeEdit { it[PreferenceKeys.USER_ID] = id }
    }

    override suspend fun saveUserEmail(email: String) {
        safeEdit { it[PreferenceKeys.USER_EMAIL] = email }
    }

    override suspend fun saveUserRole(role: String) {
        safeEdit { it[PreferenceKeys.USER_ROLE] = role }
    }

    // ==================== USER PROFILE ====================

    override val userName: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.USER_NAME] }

    override val userAvatar: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.USER_AVATAR] }

    override val dateOfBirth: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.DATE_OF_BIRTH] }

    override val grade: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.GRADE] }

    override val isProfileComplete: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.IS_PROFILE_COMPLETE] ?: false }

    override suspend fun saveUserName(name: String) {
        safeEdit { it[PreferenceKeys.USER_NAME] = name }
    }

    override suspend fun saveUserAvatar(avatarUrl: String) {
        safeEdit { it[PreferenceKeys.USER_AVATAR] = avatarUrl }
    }

    override suspend fun saveDateOfBirth(dob: String) {
        safeEdit { it[PreferenceKeys.DATE_OF_BIRTH] = dob }
    }

    override suspend fun saveGrade(grade: String) {
        safeEdit { it[PreferenceKeys.GRADE] = grade }
    }

    override suspend fun setProfileComplete(complete: Boolean) {
        safeEdit { it[PreferenceKeys.IS_PROFILE_COMPLETE] = complete }
    }

    // ==================== ONBOARDING ====================

    override val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.HAS_COMPLETED_ONBOARDING] ?: false }

    override val onboardingVersion: Flow<Int> = dataStore.data
        .mapWithCatch(0) { it[PreferenceKeys.ONBOARDING_VERSION] ?: 0 }

    override val hasSeenWelcomeScreen: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.HAS_SEEN_WELCOME_SCREEN] ?: false }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        safeEdit { it[PreferenceKeys.HAS_COMPLETED_ONBOARDING] = completed }
    }

    override suspend fun setOnboardingVersion(version: Int) {
        safeEdit { it[PreferenceKeys.ONBOARDING_VERSION] = version }
    }

    override suspend fun setHasSeenWelcomeScreen(seen: Boolean) {
        safeEdit { it[PreferenceKeys.HAS_SEEN_WELCOME_SCREEN] = seen }
    }

    // ==================== APP SETTINGS ====================

    override val themeMode: Flow<String> = dataStore.data
        .mapWithCatch("system") { it[PreferenceKeys.THEME_MODE] ?: "system" }

    override val dynamicColor: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.DYNAMIC_COLOR] ?: true }

    override val language: Flow<String> = dataStore.data
        .mapWithCatch("en") { it[PreferenceKeys.LANGUAGE] ?: "en" }

    override val fontSize: Flow<String> = dataStore.data
        .mapWithCatch("medium") { it[PreferenceKeys.FONT_SIZE] ?: "medium" }

    override suspend fun setThemeMode(mode: String) {
        safeEdit { it[PreferenceKeys.THEME_MODE] = mode }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setLanguage(languageCode: String) {
        safeEdit { it[PreferenceKeys.LANGUAGE] = languageCode }
    }

    override suspend fun setFontSize(size: String) {
        safeEdit { it[PreferenceKeys.FONT_SIZE] = size }
    }

    // ==================== SUBSCRIPTION ====================

    override val hasActiveSubscription: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.HAS_ACTIVE_SUBSCRIPTION] ?: false }

    override val subscriptionType: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.SUBSCRIPTION_TYPE] }

    override val subscriptionStartDate: Flow<Long?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.SUBSCRIPTION_START_DATE] }

    override val subscriptionEndDate: Flow<Long?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.SUBSCRIPTION_END_DATE] }

    override val subscriptionAutoRenew: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.SUBSCRIPTION_AUTO_RENEW] ?: false }

    override val trialUsed: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.TRIAL_USED] ?: false }

    override suspend fun setActiveSubscription(active: Boolean) {
        safeEdit { it[PreferenceKeys.HAS_ACTIVE_SUBSCRIPTION] = active }
    }

    override suspend fun setSubscriptionType(type: String) {
        safeEdit { it[PreferenceKeys.SUBSCRIPTION_TYPE] = type }
    }

    override suspend fun setSubscriptionDates(startDate: Long, endDate: Long) {
        safeEdit {
            it[PreferenceKeys.SUBSCRIPTION_START_DATE] = startDate
            it[PreferenceKeys.SUBSCRIPTION_END_DATE] = endDate
        }
    }

    override suspend fun setSubscriptionAutoRenew(autoRenew: Boolean) {
        safeEdit { it[PreferenceKeys.SUBSCRIPTION_AUTO_RENEW] = autoRenew }
    }

    override suspend fun setTrialUsed(used: Boolean) {
        safeEdit { it[PreferenceKeys.TRIAL_USED] = used }
    }

    // ==================== PARENTAL CONTROLS ====================

    override val isParentLockEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.IS_PARENT_LOCK_ENABLED] ?: false }

    override val parentPin: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.PARENT_PIN] }
    override val isPinVerified: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.IS_PIN_VERIFIED] ?: false }

    override val pinVerificationTimestamp: Flow<Long> = dataStore.data
        .mapWithCatch(0L) { it[PreferenceKeys.PIN_VERIFICATION_TIMESTAMP] ?: 0L }

    override val screenTimeLimit: Flow<Int> = dataStore.data
        .mapWithCatch(0) { it[PreferenceKeys.SCREEN_TIME_LIMIT] ?: 0 }

    override val allowedAppsTime: Flow<Map<String, Int>> = dataStore.data
        .mapWithCatch(emptyMap()) {
            val jsonString = it[PreferenceKeys.ALLOWED_APPS_TIME]
            jsonString?.let { json.decodeFromString(it) } ?: emptyMap()
        }

    override suspend fun setParentLockEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.IS_PARENT_LOCK_ENABLED] = enabled }
    }

    override suspend fun saveParentPin(pin: String) {
        safeEdit { it[PreferenceKeys.PARENT_PIN] = pin }
    }

    override suspend fun setPinVerified(verified: Boolean) {
        safeEdit {
            it[PreferenceKeys.IS_PIN_VERIFIED] = verified
            if (verified) {
                it[PreferenceKeys.PIN_VERIFICATION_TIMESTAMP] = System.currentTimeMillis()
            }
        }
    }

    override suspend fun setPinVerificationTimestamp(timestamp: Long) {
        safeEdit { it[PreferenceKeys.PIN_VERIFICATION_TIMESTAMP] = timestamp }
    }

    override suspend fun setScreenTimeLimit(minutes: Int) {
        safeEdit { it[PreferenceKeys.SCREEN_TIME_LIMIT] = minutes }
    }

    override suspend fun saveAllowedAppTime(appId: String, minutes: Int) {
        val current = allowedAppsTime.first().toMutableMap()
        current[appId] = minutes
        safeEdit { it[PreferenceKeys.ALLOWED_APPS_TIME] = json.encodeToString(current) }
    }

    // ==================== LEARNING PREFERENCES ====================

    override val preferredLearningTime: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.PREFERRED_LEARNING_TIME] }

    override val dailyGoalMinutes: Flow<Int> = dataStore.data
        .mapWithCatch(30) { it[PreferenceKeys.DAILY_GOAL_MINUTES] ?: 30 }

    override val reminderEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.REMINDER_ENABLED] ?: true }

    override val reminderTime: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.REMINDER_TIME] }

    override val lastCompletedLesson: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.LAST_COMPLETED_LESSON] }

    override val currentStreak: Flow<Int> = dataStore.data
        .mapWithCatch(0) { it[PreferenceKeys.CURRENT_STREAK] ?: 0 }

    override val longestStreak: Flow<Int> = dataStore.data
        .mapWithCatch(0) { it[PreferenceKeys.LONGEST_STREAK] ?: 0 }

    override suspend fun setPreferredLearningTime(time: String) {
        safeEdit { it[PreferenceKeys.PREFERRED_LEARNING_TIME] = time }
    }

    override suspend fun setDailyGoalMinutes(minutes: Int) {
        safeEdit { it[PreferenceKeys.DAILY_GOAL_MINUTES] = minutes }
    }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.REMINDER_ENABLED] = enabled }
    }

    override suspend fun setReminderTime(time: String) {
        safeEdit { it[PreferenceKeys.REMINDER_TIME] = time }
    }

    override suspend fun saveLastCompletedLesson(lessonId: String) {
        safeEdit { it[PreferenceKeys.LAST_COMPLETED_LESSON] = lessonId }
    }

    override suspend fun updateStreak(currentStreak: Int, longestStreak: Int) {
        safeEdit {
            it[PreferenceKeys.CURRENT_STREAK] = currentStreak
            it[PreferenceKeys.LONGEST_STREAK] = longestStreak
        }
    }

    // ==================== CONTENT PREFERENCES ====================

    override val autoPlayNextLesson: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.AUTO_PLAY_NEXT_LESSON] ?: true }

    override val videoQuality: Flow<String> = dataStore.data
        .mapWithCatch("auto") { it[PreferenceKeys.VIDEO_QUALITY] ?: "auto" }

    override val downloadOverWifiOnly: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.DOWNLOAD_OVER_WIFI_ONLY] ?: true }

    override val offlineModeEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.OFFLINE_MODE_ENABLED] ?: false }

    override val subtitlesEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.SUBTITLES_ENABLED] ?: false }

    override val playbackSpeed: Flow<Float> = dataStore.data
        .mapWithCatch(1.0f) { it[PreferenceKeys.PLAYBACK_SPEED] ?: 1.0f }

    override suspend fun setAutoPlayNextLesson(autoPlay: Boolean) {
        safeEdit { it[PreferenceKeys.AUTO_PLAY_NEXT_LESSON] = autoPlay }
    }

    override suspend fun setVideoQuality(quality: String) {
        safeEdit { it[PreferenceKeys.VIDEO_QUALITY] = quality }
    }

    override suspend fun setDownloadOverWifiOnly(wifiOnly: Boolean) {
        safeEdit { it[PreferenceKeys.DOWNLOAD_OVER_WIFI_ONLY] = wifiOnly }
    }

    override suspend fun setOfflineModeEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.OFFLINE_MODE_ENABLED] = enabled }
    }

    override suspend fun setSubtitlesEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.SUBTITLES_ENABLED] = enabled }
    }

    override suspend fun setPlaybackSpeed(speed: Float) {
        safeEdit { it[PreferenceKeys.PLAYBACK_SPEED] = speed }
    }

    // ==================== NOTIFICATIONS ====================

    override val notificationsEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true }

    override val pushNotificationsEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.PUSH_NOTIFICATIONS_ENABLED] ?: true }

    override val emailNotificationsEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.EMAIL_NOTIFICATIONS_ENABLED] ?: true }

    override val classRemindersEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.CLASS_REMINDERS_ENABLED] ?: true }

    override val achievementNotificationsEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.ACHIEVEMENT_NOTIFICATIONS_ENABLED] ?: true }

    override val progressReportsEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.PROGRESS_REPORTS_ENABLED] ?: true }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.PUSH_NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setEmailNotificationsEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.EMAIL_NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setClassRemindersEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.CLASS_REMINDERS_ENABLED] = enabled }
    }

    override suspend fun setAchievementNotificationsEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.ACHIEVEMENT_NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setProgressReportsEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.PROGRESS_REPORTS_ENABLED] = enabled }
    }

    // ==================== PRIVACY & SECURITY ====================

    override val analyticsEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.ANALYTICS_ENABLED] ?: true }

    override val crashReportingEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.CRASH_REPORTING_ENABLED] ?: true }

    override val locationSharingEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.LOCATION_SHARING_ENABLED] ?: false }

    override val biometricAuthEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.BIOMETRIC_AUTH_ENABLED] ?: false }

    override val lastPasswordChangeDate: Flow<Long?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.LAST_PASSWORD_CHANGE_DATE] }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.ANALYTICS_ENABLED] = enabled }
    }

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.CRASH_REPORTING_ENABLED] = enabled }
    }

    override suspend fun setLocationSharingEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.LOCATION_SHARING_ENABLED] = enabled }
    }

    override suspend fun setBiometricAuthEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.BIOMETRIC_AUTH_ENABLED] = enabled }
    }

    override suspend fun setLastPasswordChangeDate(timestamp: Long) {
        safeEdit { it[PreferenceKeys.LAST_PASSWORD_CHANGE_DATE] = timestamp }
    }

    // ==================== APP USAGE & ANALYTICS ====================

    override val appOpenCount: Flow<Int> = dataStore.data
        .mapWithCatch(0) { it[PreferenceKeys.APP_OPEN_COUNT] ?: 0 }

    override val lastAppOpenDate: Flow<Long> = dataStore.data
        .mapWithCatch(0L) { it[PreferenceKeys.LAST_APP_OPEN_DATE] ?: 0L }

    override val totalLearningTimeMinutes: Flow<Int> = dataStore.data
        .mapWithCatch(0) { it[PreferenceKeys.TOTAL_LEARNING_TIME_MINUTES] ?: 0 }

    override val lessonsCompleted: Flow<Int> = dataStore.data
        .mapWithCatch(0) { it[PreferenceKeys.LESSONS_COMPLETED] ?: 0 }

    override val lastSyncTimestamp: Flow<Long> = dataStore.data
        .mapWithCatch(0L) { it[PreferenceKeys.LAST_SYNC_TIMESTAMP] ?: 0L }

    override val appVersion: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.APP_VERSION] }

    override suspend fun incrementAppOpenCount() {
        val current = appOpenCount.first()
        safeEdit { it[PreferenceKeys.APP_OPEN_COUNT] = current + 1 }
    }

    override suspend fun setLastAppOpenDate(timestamp: Long) {
        safeEdit { it[PreferenceKeys.LAST_APP_OPEN_DATE] = timestamp }
    }

    override suspend fun addLearningTime(minutes: Int) {
        val current = totalLearningTimeMinutes.first()
        safeEdit { it[PreferenceKeys.TOTAL_LEARNING_TIME_MINUTES] = current + minutes }
    }

    override suspend fun incrementLessonsCompleted() {
        val current = lessonsCompleted.first()
        safeEdit { it[PreferenceKeys.LESSONS_COMPLETED] = current + 1 }
    }

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        safeEdit { it[PreferenceKeys.LAST_SYNC_TIMESTAMP] = timestamp }
    }

    override suspend fun setAppVersion(version: String) {
        safeEdit { it[PreferenceKeys.APP_VERSION] = version }
    }

    // ==================== LIVE CLASS PREFERENCES ====================

    override val autoJoinScheduledClasses: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.AUTO_JOIN_SCHEDULED_CLASSES] ?: false }

    override val cameraDefaultOn: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.CAMERA_DEFAULT_ON] ?: false }

    override val micDefaultOn: Flow<Boolean> = dataStore.data
        .mapWithCatch(true) { it[PreferenceKeys.MIC_DEFAULT_ON] ?: true }

    override val lastJoinedClassId: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.LAST_JOINED_CLASS_ID] }

    override val classNotificationLeadTime: Flow<Int> = dataStore.data
        .mapWithCatch(5) { it[PreferenceKeys.CLASS_NOTIFICATION_LEAD_TIME] ?: 5 }

    override suspend fun setAutoJoinScheduledClasses(autoJoin: Boolean) {
        safeEdit { it[PreferenceKeys.AUTO_JOIN_SCHEDULED_CLASSES] = autoJoin }
    }

    override suspend fun setCameraDefaultOn(cameraOn: Boolean) {
        safeEdit { it[PreferenceKeys.CAMERA_DEFAULT_ON] = cameraOn }
    }

    override suspend fun setMicDefaultOn(micOn: Boolean) {
        safeEdit { it[PreferenceKeys.MIC_DEFAULT_ON] = micOn }
    }

    override suspend fun saveLastJoinedClassId(classId: String) {
        safeEdit { it[PreferenceKeys.LAST_JOINED_CLASS_ID] = classId }
    }

    override suspend fun setClassNotificationLeadTime(minutes: Int) {
        safeEdit { it[PreferenceKeys.CLASS_NOTIFICATION_LEAD_TIME] = minutes }
    }

    // ==================== PAYMENT & BILLING ====================

    override val savedPaymentMethod: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.SAVED_PAYMENT_METHOD] }

    override val defaultPaymentMethod: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.DEFAULT_PAYMENT_METHOD] }

    override val billingAddress: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.BILLING_ADDRESS] }

    override val autoPayEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.AUTO_PAY_ENABLED] ?: false }

    override suspend fun savePaymentMethod(method: String) {
        safeEdit { it[PreferenceKeys.SAVED_PAYMENT_METHOD] = method }
    }

    override suspend fun setDefaultPaymentMethod(method: String) {
        safeEdit { it[PreferenceKeys.DEFAULT_PAYMENT_METHOD] = method }
    }

    override suspend fun saveBillingAddress(address: String) {
        safeEdit { it[PreferenceKeys.BILLING_ADDRESS] = address }
    }

    override suspend fun setAutoPayEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.AUTO_PAY_ENABLED] = enabled }
    }

    // ==================== ACHIEVEMENTS & REWARDS ====================

    override val totalPoints: Flow<Int> = dataStore.data
        .mapWithCatch(0) { it[PreferenceKeys.TOTAL_POINTS] ?: 0 }

    override val level: Flow<Int> = dataStore.data
        .mapWithCatch(1) { it[PreferenceKeys.LEVEL] ?: 1 }

    override val badges: Flow<Set<String>> = dataStore.data
        .mapWithCatch(emptySet()) { it[PreferenceKeys.BADGES] ?: emptySet() }

    override val lastRewardClaimedDate: Flow<Long?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.LAST_REWARD_CLAIMED_DATE] }

    override suspend fun addPoints(points: Int) {
        val current = totalPoints.first()
        safeEdit { it[PreferenceKeys.TOTAL_POINTS] = current + points }
    }

    override suspend fun setLevel(level: Int) {
        safeEdit { it[PreferenceKeys.LEVEL] = level }
    }

    override suspend fun addBadge(badgeId: String) {
        val current = badges.first().toMutableSet()
        current.add(badgeId)
        safeEdit { it[PreferenceKeys.BADGES] = current }
    }

    override suspend fun setLastRewardClaimedDate(timestamp: Long) {
        safeEdit { it[PreferenceKeys.LAST_REWARD_CLAIMED_DATE] = timestamp }
    }

    // ==================== DOWNLOAD MANAGEMENT ====================

    override val downloadedContentIds: Flow<Set<String>> = dataStore.data
        .mapWithCatch(emptySet()) { it[PreferenceKeys.DOWNLOADED_CONTENT_IDS] ?: emptySet() }

    override val totalDownloadedSizeMB: Flow<Long> = dataStore.data
        .mapWithCatch(0L) { it[PreferenceKeys.TOTAL_DOWNLOADED_SIZE_MB] ?: 0L }

    override val autoDeleteOldDownloads: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.AUTO_DELETE_OLD_DOWNLOADS] ?: false }

    override val maxDownloadSizeMB: Flow<Long> = dataStore.data
        .mapWithCatch(1000L) { it[PreferenceKeys.MAX_DOWNLOAD_SIZE_MB] ?: 1000L }

    override suspend fun addDownloadedContent(contentId: String, sizeMB: Long) {
        val currentIds = downloadedContentIds.first().toMutableSet()
        val currentSize = totalDownloadedSizeMB.first()

        currentIds.add(contentId)
        safeEdit {
            it[PreferenceKeys.DOWNLOADED_CONTENT_IDS] = currentIds
            it[PreferenceKeys.TOTAL_DOWNLOADED_SIZE_MB] = currentSize + sizeMB
        }
    }

    override suspend fun removeDownloadedContent(contentId: String, sizeMB: Long) {
        val currentIds = downloadedContentIds.first().toMutableSet()
        val currentSize = totalDownloadedSizeMB.first()

        currentIds.remove(contentId)
        safeEdit {
            it[PreferenceKeys.DOWNLOADED_CONTENT_IDS] = currentIds
            it[PreferenceKeys.TOTAL_DOWNLOADED_SIZE_MB] = (currentSize - sizeMB).coerceAtLeast(0)
        }
    }

    override suspend fun setAutoDeleteOldDownloads(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.AUTO_DELETE_OLD_DOWNLOADS] = enabled }
    }

    override suspend fun setMaxDownloadSize(sizeMB: Long) {
        safeEdit { it[PreferenceKeys.MAX_DOWNLOAD_SIZE_MB] = sizeMB }
    }

    // ==================== ACCESSIBILITY ====================

    override val textToSpeechEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.TEXT_TO_SPEECH_ENABLED] ?: false }

    override val highContrastMode: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.HIGH_CONTRAST_MODE] ?: false }

    override val reducedMotionEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.REDUCED_MOTION_ENABLED] ?: false }

    override val screenReaderEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.SCREEN_READER_ENABLED] ?: false }

    override suspend fun setTextToSpeechEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.TEXT_TO_SPEECH_ENABLED] = enabled }
    }

    override suspend fun setHighContrastMode(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.HIGH_CONTRAST_MODE] = enabled }
    }

    override suspend fun setReducedMotionEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.REDUCED_MOTION_ENABLED] = enabled }
    }

    override suspend fun setScreenReaderEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.SCREEN_READER_ENABLED] = enabled }
    }

    // ==================== EXPERIMENTAL FEATURES ====================

    override val betaFeaturesEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.BETA_FEATURES_ENABLED] ?: false }

    override val debugModeEnabled: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.DEBUG_MODE_ENABLED] ?: false }

    override val featureFlags: Flow<Map<String, Boolean>> = dataStore.data
        .mapWithCatch(emptyMap()) {
            val jsonString = it[PreferenceKeys.FEATURE_FLAGS]
            jsonString?.let { json.decodeFromString(it) } ?: emptyMap()
        }

    override suspend fun setBetaFeaturesEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.BETA_FEATURES_ENABLED] = enabled }
    }

    override suspend fun setDebugModeEnabled(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.DEBUG_MODE_ENABLED] = enabled }
    }

    override suspend fun setFeatureFlag(flagName: String, enabled: Boolean) {
        val current = featureFlags.first().toMutableMap()
        current[flagName] = enabled
        safeEdit { it[PreferenceKeys.FEATURE_FLAGS] = json.encodeToString(current) }
    }

    // ==================== CACHE & DATA ====================

    override val lastCacheClearDate: Flow<Long?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.LAST_CACHE_CLEAR_DATE] }

    override val cacheSize: Flow<Long> = dataStore.data
        .mapWithCatch(0L) { it[PreferenceKeys.CACHE_SIZE] ?: 0L }

    override val dataSaverMode: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.DATA_SAVER_MODE] ?: false }

    override suspend fun setLastCacheClearDate(timestamp: Long) {
        safeEdit { it[PreferenceKeys.LAST_CACHE_CLEAR_DATE] = timestamp }
    }

    override suspend fun updateCacheSize(sizeBytes: Long) {
        safeEdit { it[PreferenceKeys.CACHE_SIZE] = sizeBytes }
    }

    override suspend fun setDataSaverMode(enabled: Boolean) {
        safeEdit { it[PreferenceKeys.DATA_SAVER_MODE] = enabled }
    }

    // ==================== UTILITY METHODS ====================

    override suspend fun clearAll() {
        safeEdit { it.clear() }
    }

    override suspend fun clearAuthData() {
        safeEdit {
            it.remove(PreferenceKeys.JWT_TOKEN)
            it.remove(PreferenceKeys.REFRESH_TOKEN)
            it.remove(PreferenceKeys.IS_LOGGED_IN)
            it.remove(PreferenceKeys.USER_ID)
            it.remove(PreferenceKeys.USER_EMAIL)
        }
    }

    override suspend fun clearUserData() {
        safeEdit {
            // Clear user profile
            it.remove(PreferenceKeys.USER_NAME)
            it.remove(PreferenceKeys.USER_AVATAR)
            it.remove(PreferenceKeys.DATE_OF_BIRTH)
            it.remove(PreferenceKeys.GRADE)
            it.remove(PreferenceKeys.IS_PROFILE_COMPLETE)

            // Clear learning data
            it.remove(PreferenceKeys.CURRENT_STREAK)
            it.remove(PreferenceKeys.TOTAL_POINTS)
            it.remove(PreferenceKeys.LEVEL)
            it.remove(PreferenceKeys.BADGES)
        }
    }

    override suspend fun exportPreferences(): Map<String, Any?> {
        val prefs = dataStore.data.first()
        return prefs.asMap().mapKeys { it.key.name }
    }

    override suspend fun importPreferences(preferences: Map<String, Any?>) {
        safeEdit { mutablePrefs ->
            preferences.forEach { (key, value) ->
                when (value) {
                    is Boolean -> mutablePrefs[booleanPreferencesKey(key)] = value
                    is Int -> mutablePrefs[intPreferencesKey(key)] = value
                    is Long -> mutablePrefs[longPreferencesKey(key)] = value
                    is Float -> mutablePrefs[floatPreferencesKey(key)] = value
                    is String -> mutablePrefs[stringPreferencesKey(key)] = value
                    is Set<*> -> @Suppress("UNCHECKED_CAST")
                    mutablePrefs[stringSetPreferencesKey(key)] = value as Set<String>
                }
            }
        }
    }

    // Helper method for safe editing
    private suspend fun safeEdit(block: suspend (MutablePreferences) -> Unit) {
        try {
            dataStore.edit(block)
        } catch (e: IOException) {
            Log.e(TAG, "Error editing preferences", e)
        }
    }

    companion object {
        private const val TAG = "PreferenceStorage"
    }
}


