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


    suspend fun clearTokens() {
        mutex.withLock {
            try {
                preferenceStorage.saveJwtToken("")
                Log.d(TAG, "Tokens cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing tokens", e)
            }
        }
    }
}
