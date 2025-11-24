package com.scharfesicht.attendencesystem.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Complete Preference Storage Interface for Attendance App
 */
interface IPreferenceStorage {

    // ==================== AUTHENTICATION ====================
    val jwtToken: Flow<String?>
    val isLoggedIn: Flow<Boolean>
    val userId: Flow<String?>
    val userEmail: Flow<String?>
    val userRole: Flow<String?>

    suspend fun saveJwtToken(token: String)
    suspend fun setLoggedIn(value: Boolean)
    suspend fun saveUserId(id: String)
    suspend fun saveUserEmail(email: String)
    suspend fun saveUserRole(role: String)

    // ==================== USER PROFILE ====================
    val userName: Flow<String?>
    val userAvatar: Flow<String?>
    val profileImageUrl: Flow<String?>
    val dateOfBirth: Flow<String?>
    val grade: Flow<String?>

    suspend fun saveUserName(name: String)
    suspend fun saveUserAvatar(avatarUrl: String)
    suspend fun saveProfileImageUrl(url: String)
    suspend fun saveDateOfBirth(dob: String)
    suspend fun saveGrade(grade: String)

    // ==================== ATTENDANCE DATA ====================
    val currentShiftJson: Flow<String?>
    val userShiftsJson: Flow<String?>
    val shiftsLastUpdated: Flow<Long?>

    val zoneLatitude: Flow<String?>
    val zoneLongitude: Flow<String?>
    val zoneRadius: Flow<String?>
    val zoneName: Flow<String?>
    val zoneId: Flow<Int?>

    val isCheckedIn: Flow<Boolean>
    val lastCheckinTime: Flow<Long?>

    suspend fun saveCurrentShift(shiftJson: String)
    suspend fun saveUserShifts(shiftsJson: String)
    suspend fun saveShiftsLastUpdated(timestamp: Long)

    suspend fun saveZoneData(latitude: String, longitude: String, radius: String, name: String, zoneId: Int)
    suspend fun clearZoneData()

    suspend fun setCheckedIn(value: Boolean)
    suspend fun saveLastCheckinTime(timestamp: Long)

    // ==================== APP SETTINGS ====================
    val language: Flow<String>
    suspend fun setLanguage(languageCode: String)

    // ==================== UTILITY METHODS ====================
    suspend fun clearAll()
    suspend fun clearAuthData()
    suspend fun clearUserData()
    suspend fun clearAttendanceData()
    suspend fun exportPreferences(): Map<String, Any?>
    suspend fun importPreferences(preferences: Map<String, Any?>)
}