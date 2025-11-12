package com.scharfesicht.attendencesystem.data.absher.repository

import android.util.Log
import com.scharfesicht.attendencesystem.app.AttendanceSystemApp
import com.scharfesicht.attendencesystem.app.ui.theme.ThemeMode
import com.scharfesicht.attendencesystem.domain.absher.model.AppLanguage
import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherPosition
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbsherRepositoryImpl @Inject constructor(
    private var absherHelper: IAbsherHelper?
) : AbsherRepository {

    companion object {
        private const val TAG = "AbsherRepositoryImpl"
    }

    /**
     * Reattach global helper if Hilt instance was null (hot reload / app relaunch)
     */
    private fun getHelper(): IAbsherHelper? {
        if (absherHelper == null) {
            absherHelper = AttendanceSystemApp.absherHelper
            if (absherHelper != null) {
                Log.d(TAG, "✅ Helper reattached from Application context")
            }
        }
        return absherHelper
    }

    override fun isAbsherInitialized(): Boolean = getHelper() != null

    // ============================================================
    // USER INFO
    // ============================================================

    override fun getUserInfo(): Flow<Result<UserInfo>> = flow {
        val helper = getHelper()
        if (helper == null) {
            Log.e(TAG, "❌ Absher SDK not initialized")
            emit(Result.failure(Exception("Absher SDK not initialized")))
            return@flow
        }

        try {
            Log.d(TAG, "🔄 Fetching user info from Absher SDK...")

            // Basic info
            val nationalId = helper.getUserNationalID().getDataOrThrow()
            val fullNameAr = helper.getUserFullNameAr().getDataOrThrow()
            val fullNameEn = helper.getUserFullNameEn().getDataOrThrow()
            val firstNameAr = helper.getUserFirstNameAr().getDataOrThrow()
            val token = helper.getUserToken().getDataOrThrow()

            // Theme
            val themeString = helper.getCurrentTheme().getDataOrNull() ?: "light"
            val theme = when (themeString.lowercase()) {
                "dark" -> ThemeMode.DARK
                "light" -> ThemeMode.LIGHT
                else -> ThemeMode.SYSTEM
            }

            // Language
            val langString = helper.getCurrentLanguage().getDataOrNull() ?: "en"
            val language = AppLanguage.from(langString)
            val isRTL = language == AppLanguage.ARABIC

            val userInfo = UserInfo(
                nationalId = nationalId,
                fullNameAr = fullNameAr,
                fullNameEn = fullNameEn,
                firstNameAr = firstNameAr,
                token = token,
                theme = theme,
                language = language,
                isRTL = isRTL
            )

            Log.d(TAG, "✅ User info loaded successfully: $fullNameEn (${language.name})")
            emit(Result.success(userInfo))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch user info: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    // ============================================================
    // TOKEN
    // ============================================================

    override fun getUserToken(): Result<String> {
        return try {
            val token = getHelper()?.getUserToken()?.getDataOrThrow()
                ?: return Result.failure(Exception("No token available"))
            Log.d(TAG, "✅ Token retrieved successfully")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user token: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================
    // THEME / LANGUAGE
    // ============================================================

    override fun getCurrentTheme(): Result<String> {
        return try {
            val response = getHelper()?.getCurrentTheme()
            if (response != null && response.success && response.data != null) {
                Log.d(TAG, "🎨 Current theme: ${response.data}")
                Result.success(response.data!!)
            } else {
                Result.failure(Exception(response?.message ?: "Theme unavailable"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Theme fetch error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getCurrentLanguage(): Result<String> {
        return try {
            val response = getHelper()?.getCurrentLanguage()
            if (response != null && response.success && response.data != null) {
                Log.d(TAG, "🌐 Current language: ${response.data}")
                Result.success(response.data!!)
            } else {
                Result.failure(Exception(response?.message ?: "Language unavailable"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Language fetch error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================
    // PROFILE IMAGE
    // ============================================================

    override fun getUserProfileImage(): Result<String> {
        return try {
            val image = getHelper()?.getUserProfileImage()?.getDataOrThrow()
                ?: return Result.failure(Exception("Profile image not found"))
            Log.d(TAG, "🖼️ Profile image URL loaded")
            Result.success(image)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get profile image: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================
    // LOCAL STORAGE
    // ============================================================

    override fun saveStringToLocal(key: String, value: String): Result<Boolean> {
        return try {
            val success = getHelper()?.saveStringToLocal(key, value)?.getDataOrThrow() ?: false
            Log.d(TAG, "💾 Saved string key=$key success=$success")
            Result.success(success)
        } catch (e: Exception) {
            Log.e(TAG, "❌ saveStringToLocal error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun readStringFromLocal(key: String): Result<String> {
        return try {
            val value = getHelper()?.readStringFromLocal(key)?.getDataOrThrow() ?: ""
            Log.d(TAG, "📂 Read local key=$key value=$value")
            Result.success(value)
        } catch (e: Exception) {
            Log.e(TAG, "❌ readStringFromLocal error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================
    // LOCATION
    // ============================================================

    override fun getLocation(): Result<AbsherPosition> {
        return try {
            val absherPos = getHelper()?.getLocation()?.getDataOrThrow()
                ?: return Result.failure(Exception("No location available"))
            val position = AbsherPosition(
                latitude = absherPos.latitude,
                longitude = absherPos.longitude,
                accuracy = absherPos.accuracy
            )
            Log.d(TAG, "📍 Location retrieved: ${position.latitude}, ${position.longitude}")
            Result.success(position)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get location: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================
    // CLOSE APP
    // ============================================================

    override fun closeApp(data: Map<String, Any>?) {
        try {
            getHelper()?.closeApp(data)
            Log.d(TAG, "🚪 Close app invoked with data: $data")
        } catch (e: Exception) {
            Log.e(TAG, "❌ closeApp error: ${e.message}", e)
        }
    }
}

// ============================================================
// EXTENSION FUNCTIONS FOR AbsherResponse
// ============================================================

private fun <T> sa.gov.moi.absherinterior.core_logic.AbsherResponse<T>.getDataOrThrow(): T {
    return when {
        this.success && this.data != null -> this.data!!
        else -> throw Exception(this.message ?: "Unknown error from Absher SDK")
    }
}

private fun <T> sa.gov.moi.absherinterior.core_logic.AbsherResponse<T>.getDataOrNull(): T? {
    return if (this.success) this.data else null
}
