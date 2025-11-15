package com.scharfesicht.attendencesystem.core.network

import android.util.Log
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
//    @AuthApiForRefresh private val authApi: AuthApiService, // ⚠️ IMPORTANT: Use the special one!
    private val preferenceStorage: IPreferenceStorage
) {

    companion object {
        private const val TAG = "TokenManager"
    }

    private val mutex = Mutex()

    fun getJwtToken(): String? = try {
        kotlinx.coroutines.runBlocking {
            preferenceStorage.jwtToken.firstOrNull()?.takeIf { it.isNotBlank() }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting JWT token", e)
        null
    }

    fun getRefreshToken(): String? = try {
        kotlinx.coroutines.runBlocking {
            preferenceStorage.refreshToken.firstOrNull()?.takeIf { it.isNotBlank() }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting refresh token", e)
        null
    }

    suspend fun saveJwtToken(token: String) {
        try {
            if (token.isNotBlank()) {
                preferenceStorage.saveJwtToken(token)
                Log.d(TAG, "JWT token saved")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving JWT token", e)
            throw e
        }
    }

    suspend fun saveRefreshToken(token: String) {
        try {
            if (token.isNotBlank()) {
                preferenceStorage.saveRefreshToken(token)
                Log.d(TAG, "Refresh token saved")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving refresh token", e)
            throw e
        }
    }

    suspend fun saveTokens(jwtToken: String, refreshToken: String) {
        mutex.withLock {
            saveJwtToken(jwtToken)
            saveRefreshToken(refreshToken)
        }
    }

    suspend fun refreshToken(): String? = mutex.withLock {
        try {
            val currentRefreshToken = preferenceStorage.refreshToken.firstOrNull()

            if (currentRefreshToken.isNullOrBlank()) {
                Log.e(TAG, "No refresh token available")
                return null
            }

            Log.d(TAG, "Refreshing token...")
//            val response = authApi.refreshToken()

            if (/*response.success && response.data != null*/ true) {
                val tokenResponse = /*response.token!!*/ "dummy token for test"

                saveJwtToken(tokenResponse)
                tokenResponse.let { saveRefreshToken(it) }

                Log.d(TAG, "Token refresh successful")
                return tokenResponse
            } else {
//                Log.e(TAG, "Token refresh failed: ${response.status} ${response.message}")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh exception", e)
            return null
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return try {
            val token = preferenceStorage.jwtToken.firstOrNull()
            !token.isNullOrBlank()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking authentication", e)
            false
        }
    }

    suspend fun clearTokens() {
        mutex.withLock {
            try {
                preferenceStorage.saveJwtToken("")
                preferenceStorage.saveRefreshToken("")
                Log.d(TAG, "Tokens cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing tokens", e)
            }
        }
    }

    fun isTokenValid(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        val parts = token.split(".")
        return parts.size == 3
    }
}
/*

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "attendance_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}*/
