package com.scharfesicht.attendencesystem.core.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val HEADER_AUTHORIZATION = "Authorization"
    }

    private val isRefreshing = AtomicBoolean(false)

    override fun authenticate(route: Route?, response: Response): Request? {
        val requestRetryCount = response.request.header("X-Retry-Count")?.toIntOrNull() ?: 0

        if (requestRetryCount >= MAX_RETRY_ATTEMPTS) {
            Log.e(TAG, "Max retry attempts reached. Clearing tokens.")
            runBlocking { tokenManager.clearTokens() }
            return null
        }

        if (isRefreshing.get()) {
            Log.d(TAG, "Token refresh already in progress, waiting...")
            Thread.sleep(1000)

            val newToken = tokenManager.getJwtToken()
            return if (newToken != null) {
                response.request.newBuilder()
                    .header(HEADER_AUTHORIZATION, "Bearer $newToken")
                    .header("X-Retry-Count", (requestRetryCount + 1).toString())
                    .build()
            } else {
                null
            }
        }

        synchronized(this) {
            if (isRefreshing.get()) {
                return null
            }

            isRefreshing.set(true)

            return try {
                Log.d(TAG, "Attempting to refresh token...")

                val newToken = runBlocking {
                    tokenManager.refreshToken()
                }

                if (newToken != null) {
                    Log.d(TAG, "Token refreshed successfully")
                    response.request.newBuilder()
                        .header(HEADER_AUTHORIZATION, "Bearer $newToken")
                        .header("X-Retry-Count", (requestRetryCount + 1).toString())
                        .build()
                } else {
                    Log.e(TAG, "Token refresh failed, clearing tokens")
                    runBlocking { tokenManager.clearTokens() }
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh error: ${e.message}", e)
                runBlocking { tokenManager.clearTokens() }
                null
            } finally {
                isRefreshing.set(false)
            }
        }
    }
}