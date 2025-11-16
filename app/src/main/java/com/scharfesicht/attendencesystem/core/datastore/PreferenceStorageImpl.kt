package com.scharfesicht.attendencesystem.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceStorageImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : IPreferenceStorage {

    companion object {
        private const val TAG = "PreferenceStorage"
    }

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

    override val profileImageUrl: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.PROFILE_IMAGE_URL] }

    override val dateOfBirth: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.DATE_OF_BIRTH] }

    override val grade: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.GRADE] }

    override suspend fun saveUserName(name: String) {
        safeEdit { it[PreferenceKeys.USER_NAME] = name }
    }

    override suspend fun saveUserAvatar(avatarUrl: String) {
        safeEdit { it[PreferenceKeys.USER_AVATAR] = avatarUrl }
    }

    override suspend fun saveProfileImageUrl(url: String) {
        safeEdit { it[PreferenceKeys.PROFILE_IMAGE_URL] = url }
    }

    override suspend fun saveDateOfBirth(dob: String) {
        safeEdit { it[PreferenceKeys.DATE_OF_BIRTH] = dob }
    }

    override suspend fun saveGrade(grade: String) {
        safeEdit { it[PreferenceKeys.GRADE] = grade }
    }

    // ==================== ATTENDANCE DATA ====================

    override val currentShiftJson: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.CURRENT_SHIFT_JSON] }

    override val userShiftsJson: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.USER_SHIFTS_JSON] }

    override val shiftsLastUpdated: Flow<Long?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.SHIFTS_LAST_UPDATED] }

    override val zoneLatitude: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.ZONE_LATITUDE] }

    override val zoneLongitude: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.ZONE_LONGITUDE] }

    override val zoneRadius: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.ZONE_RADIUS] }

    override val zoneName: Flow<String?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.ZONE_NAME] }

    override val isCheckedIn: Flow<Boolean> = dataStore.data
        .mapWithCatch(false) { it[PreferenceKeys.IS_CHECKED_IN] ?: false }

    override val lastCheckinTime: Flow<Long?> = dataStore.data
        .mapWithCatch(null) { it[PreferenceKeys.LAST_CHECKIN_TIME] }

    override suspend fun saveCurrentShift(shiftJson: String) {
        safeEdit { it[PreferenceKeys.CURRENT_SHIFT_JSON] = shiftJson }
    }

    override suspend fun saveUserShifts(shiftsJson: String) {
        safeEdit {
            it[PreferenceKeys.USER_SHIFTS_JSON] = shiftsJson
            it[PreferenceKeys.SHIFTS_LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    override suspend fun saveShiftsLastUpdated(timestamp: Long) {
        safeEdit { it[PreferenceKeys.SHIFTS_LAST_UPDATED] = timestamp }
    }

    override suspend fun saveZoneData(latitude: String, longitude: String, radius: String, name: String) {
        safeEdit {
            it[PreferenceKeys.ZONE_LATITUDE] = latitude
            it[PreferenceKeys.ZONE_LONGITUDE] = longitude
            it[PreferenceKeys.ZONE_RADIUS] = radius
            it[PreferenceKeys.ZONE_NAME] = name
        }
    }

    override suspend fun clearZoneData() {
        safeEdit {
            it.remove(PreferenceKeys.ZONE_LATITUDE)
            it.remove(PreferenceKeys.ZONE_LONGITUDE)
            it.remove(PreferenceKeys.ZONE_RADIUS)
            it.remove(PreferenceKeys.ZONE_NAME)
        }
    }

    override suspend fun setCheckedIn(value: Boolean) {
        safeEdit {
            it[PreferenceKeys.IS_CHECKED_IN] = value
            if (value) {
                it[PreferenceKeys.LAST_CHECKIN_TIME] = System.currentTimeMillis()
            }
        }
    }

    override suspend fun saveLastCheckinTime(timestamp: Long) {
        safeEdit { it[PreferenceKeys.LAST_CHECKIN_TIME] = timestamp }
    }

    // ==================== APP SETTINGS ====================

    override val language: Flow<String> = dataStore.data
        .mapWithCatch("en") { it[PreferenceKeys.LANGUAGE] ?: "en" }

    override suspend fun setLanguage(languageCode: String) {
        safeEdit { it[PreferenceKeys.LANGUAGE] = languageCode }
    }

    // ==================== UTILITY METHODS ====================

    override suspend fun clearAll() {
        safeEdit { it.clear() }
    }

    override suspend fun clearAuthData() {
        safeEdit {
            it.remove(PreferenceKeys.JWT_TOKEN)
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
            it.remove(PreferenceKeys.PROFILE_IMAGE_URL)
            it.remove(PreferenceKeys.DATE_OF_BIRTH)
            it.remove(PreferenceKeys.GRADE)
        }
    }

    override suspend fun clearAttendanceData() {
        safeEdit {
            it.remove(PreferenceKeys.CURRENT_SHIFT_JSON)
            it.remove(PreferenceKeys.USER_SHIFTS_JSON)
            it.remove(PreferenceKeys.SHIFTS_LAST_UPDATED)
            it.remove(PreferenceKeys.ZONE_LATITUDE)
            it.remove(PreferenceKeys.ZONE_LONGITUDE)
            it.remove(PreferenceKeys.ZONE_RADIUS)
            it.remove(PreferenceKeys.ZONE_NAME)
            it.remove(PreferenceKeys.IS_CHECKED_IN)
            it.remove(PreferenceKeys.LAST_CHECKIN_TIME)
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
}