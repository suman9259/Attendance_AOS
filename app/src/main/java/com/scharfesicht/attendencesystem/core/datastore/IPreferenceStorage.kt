package com.scharfesicht.attendencesystem.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Complete Preference Storage Interface for Production App
 * Covers: Auth, User, App Settings, Subscription, Parental Controls, Learning, Notifications, etc.
 */
interface IPreferenceStorage {

    // ==================== AUTHENTICATION ====================
    val jwtToken: Flow<String?>
    val refreshToken: Flow<String?>
    val isLoggedIn: Flow<Boolean>
    val userId: Flow<String?>
    val userEmail: Flow<String?>
    val userRole: Flow<String?> // "student", "parent", "teacher"

    suspend fun saveJwtToken(token: String)
    suspend fun saveRefreshToken(token: String)
    suspend fun setLoggedIn(value: Boolean)
    suspend fun saveUserId(id: String)
    suspend fun saveUserEmail(email: String)
    suspend fun saveUserRole(role: String)

    // ==================== USER PROFILE ====================
    val userName: Flow<String?>
    val userAvatar: Flow<String?>
    val dateOfBirth: Flow<String?>
    val grade: Flow<String?>
    val isProfileComplete: Flow<Boolean>

    suspend fun saveUserName(name: String)
    suspend fun saveUserAvatar(avatarUrl: String)
    suspend fun saveDateOfBirth(dob: String)
    suspend fun saveGrade(grade: String)
    suspend fun setProfileComplete(complete: Boolean)

    // ==================== ONBOARDING ====================
    val hasCompletedOnboarding: Flow<Boolean>
    val onboardingVersion: Flow<Int>
    val hasSeenWelcomeScreen: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setOnboardingVersion(version: Int)
    suspend fun setHasSeenWelcomeScreen(seen: Boolean)

    // ==================== APP SETTINGS ====================
    val themeMode: Flow<String> // "light", "dark", "system"
    val dynamicColor: Flow<Boolean>
    val language: Flow<String> // "en", "hi", "pa" etc.
    val fontSize: Flow<String> // "small", "medium", "large"

    suspend fun setThemeMode(mode: String)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setLanguage(languageCode: String)
    suspend fun setFontSize(size: String)

    // ==================== SUBSCRIPTION ====================
    val hasActiveSubscription: Flow<Boolean>
    val subscriptionType: Flow<String?> // "free", "basic", "premium"
    val subscriptionStartDate: Flow<Long?>
    val subscriptionEndDate: Flow<Long?>
    val subscriptionAutoRenew: Flow<Boolean>
    val trialUsed: Flow<Boolean>

    suspend fun setActiveSubscription(active: Boolean)
    suspend fun setSubscriptionType(type: String)
    suspend fun setSubscriptionDates(startDate: Long, endDate: Long)
    suspend fun setSubscriptionAutoRenew(autoRenew: Boolean)
    suspend fun setTrialUsed(used: Boolean)

    // ==================== PARENTAL CONTROLS ====================
    val isParentLockEnabled: Flow<Boolean>
    val parentPin: Flow<String?>
    val isPinVerified: Flow<Boolean>
    val pinVerificationTimestamp: Flow<Long>
    val screenTimeLimit: Flow<Int> // minutes per day
    val allowedAppsTime: Flow<Map<String, Int>> // app_id to minutes

    suspend fun setParentLockEnabled(enabled: Boolean)
    suspend fun saveParentPin(pin: String)
    suspend fun setPinVerified(verified: Boolean)
    suspend fun setPinVerificationTimestamp(timestamp: Long)
    suspend fun setScreenTimeLimit(minutes: Int)
    suspend fun saveAllowedAppTime(appId: String, minutes: Int)

    // ==================== LEARNING PREFERENCES ====================
    val preferredLearningTime: Flow<String?> // "morning", "afternoon", "evening"
    val dailyGoalMinutes: Flow<Int>
    val reminderEnabled: Flow<Boolean>
    val reminderTime: Flow<String?> // HH:mm format
    val lastCompletedLesson: Flow<String?>
    val currentStreak: Flow<Int>
    val longestStreak: Flow<Int>

    suspend fun setPreferredLearningTime(time: String)
    suspend fun setDailyGoalMinutes(minutes: Int)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setReminderTime(time: String)
    suspend fun saveLastCompletedLesson(lessonId: String)
    suspend fun updateStreak(currentStreak: Int, longestStreak: Int)

    // ==================== CONTENT PREFERENCES ====================
    val autoPlayNextLesson: Flow<Boolean>
    val videoQuality: Flow<String> // "low", "medium", "high", "auto"
    val downloadOverWifiOnly: Flow<Boolean>
    val offlineModeEnabled: Flow<Boolean>
    val subtitlesEnabled: Flow<Boolean>
    val playbackSpeed: Flow<Float> // 0.5, 1.0, 1.5, 2.0

    suspend fun setAutoPlayNextLesson(autoPlay: Boolean)
    suspend fun setVideoQuality(quality: String)
    suspend fun setDownloadOverWifiOnly(wifiOnly: Boolean)
    suspend fun setOfflineModeEnabled(enabled: Boolean)
    suspend fun setSubtitlesEnabled(enabled: Boolean)
    suspend fun setPlaybackSpeed(speed: Float)

    // ==================== NOTIFICATIONS ====================
    val notificationsEnabled: Flow<Boolean>
    val pushNotificationsEnabled: Flow<Boolean>
    val emailNotificationsEnabled: Flow<Boolean>
    val classRemindersEnabled: Flow<Boolean>
    val achievementNotificationsEnabled: Flow<Boolean>
    val progressReportsEnabled: Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setPushNotificationsEnabled(enabled: Boolean)
    suspend fun setEmailNotificationsEnabled(enabled: Boolean)
    suspend fun setClassRemindersEnabled(enabled: Boolean)
    suspend fun setAchievementNotificationsEnabled(enabled: Boolean)
    suspend fun setProgressReportsEnabled(enabled: Boolean)

    // ==================== PRIVACY & SECURITY ====================
    val analyticsEnabled: Flow<Boolean>
    val crashReportingEnabled: Flow<Boolean>
    val locationSharingEnabled: Flow<Boolean>
    val biometricAuthEnabled: Flow<Boolean>
    val lastPasswordChangeDate: Flow<Long?>

    suspend fun setAnalyticsEnabled(enabled: Boolean)
    suspend fun setCrashReportingEnabled(enabled: Boolean)
    suspend fun setLocationSharingEnabled(enabled: Boolean)
    suspend fun setBiometricAuthEnabled(enabled: Boolean)
    suspend fun setLastPasswordChangeDate(timestamp: Long)

    // ==================== APP USAGE & ANALYTICS ====================
    val appOpenCount: Flow<Int>
    val lastAppOpenDate: Flow<Long>
    val totalLearningTimeMinutes: Flow<Int>
    val lessonsCompleted: Flow<Int>
    val lastSyncTimestamp: Flow<Long>
    val appVersion: Flow<String?>

    suspend fun incrementAppOpenCount()
    suspend fun setLastAppOpenDate(timestamp: Long)
    suspend fun addLearningTime(minutes: Int)
    suspend fun incrementLessonsCompleted()
    suspend fun setLastSyncTimestamp(timestamp: Long)
    suspend fun setAppVersion(version: String)

    // ==================== LIVE CLASS PREFERENCES ====================
    val autoJoinScheduledClasses: Flow<Boolean>
    val cameraDefaultOn: Flow<Boolean>
    val micDefaultOn: Flow<Boolean>
    val lastJoinedClassId: Flow<String?>
    val classNotificationLeadTime: Flow<Int> // minutes before class

    suspend fun setAutoJoinScheduledClasses(autoJoin: Boolean)
    suspend fun setCameraDefaultOn(cameraOn: Boolean)
    suspend fun setMicDefaultOn(micOn: Boolean)
    suspend fun saveLastJoinedClassId(classId: String)
    suspend fun setClassNotificationLeadTime(minutes: Int)

    // ==================== PAYMENT & BILLING ====================
    val savedPaymentMethod: Flow<String?> // "card", "upi", "wallet"
    val defaultPaymentMethod: Flow<String?>
    val billingAddress: Flow<String?>
    val autoPayEnabled: Flow<Boolean>

    suspend fun savePaymentMethod(method: String)
    suspend fun setDefaultPaymentMethod(method: String)
    suspend fun saveBillingAddress(address: String)
    suspend fun setAutoPayEnabled(enabled: Boolean)

    // ==================== ACHIEVEMENTS & REWARDS ====================
    val totalPoints: Flow<Int>
    val level: Flow<Int>
    val badges: Flow<Set<String>>
    val lastRewardClaimedDate: Flow<Long?>

    suspend fun addPoints(points: Int)
    suspend fun setLevel(level: Int)
    suspend fun addBadge(badgeId: String)
    suspend fun setLastRewardClaimedDate(timestamp: Long)

    // ==================== DOWNLOAD MANAGEMENT ====================
    val downloadedContentIds: Flow<Set<String>>
    val totalDownloadedSizeMB: Flow<Long>
    val autoDeleteOldDownloads: Flow<Boolean>
    val maxDownloadSizeMB: Flow<Long>

    suspend fun addDownloadedContent(contentId: String, sizeMB: Long)
    suspend fun removeDownloadedContent(contentId: String, sizeMB: Long)
    suspend fun setAutoDeleteOldDownloads(enabled: Boolean)
    suspend fun setMaxDownloadSize(sizeMB: Long)

    // ==================== ACCESSIBILITY ====================
    val textToSpeechEnabled: Flow<Boolean>
    val highContrastMode: Flow<Boolean>
    val reducedMotionEnabled: Flow<Boolean>
    val screenReaderEnabled: Flow<Boolean>

    suspend fun setTextToSpeechEnabled(enabled: Boolean)
    suspend fun setHighContrastMode(enabled: Boolean)
    suspend fun setReducedMotionEnabled(enabled: Boolean)
    suspend fun setScreenReaderEnabled(enabled: Boolean)

    // ==================== EXPERIMENTAL FEATURES ====================
    val betaFeaturesEnabled: Flow<Boolean>
    val debugModeEnabled: Flow<Boolean>
    val featureFlags: Flow<Map<String, Boolean>>

    suspend fun setBetaFeaturesEnabled(enabled: Boolean)
    suspend fun setDebugModeEnabled(enabled: Boolean)
    suspend fun setFeatureFlag(flagName: String, enabled: Boolean)

    // ==================== CACHE & DATA ====================
    val lastCacheClearDate: Flow<Long?>
    val cacheSize: Flow<Long> // in bytes
    val dataSaverMode: Flow<Boolean>

    suspend fun setLastCacheClearDate(timestamp: Long)
    suspend fun updateCacheSize(sizeBytes: Long)
    suspend fun setDataSaverMode(enabled: Boolean)

    // ==================== UTILITY METHODS ====================
    suspend fun clearAll()
    suspend fun clearAuthData()
    suspend fun clearUserData()
    suspend fun exportPreferences(): Map<String, Any?>
    suspend fun importPreferences(preferences: Map<String, Any?>)
}