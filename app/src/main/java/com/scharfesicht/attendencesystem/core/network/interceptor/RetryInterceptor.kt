package com.scharfesicht.attendencesystem.core.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class RetryInterceptor @Inject constructor(
    private val maxRetries: Int = 3
) : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
        private const val INITIAL_BACKOFF_MS = 1000L
        private const val BACKOFF_MULTIPLIER = 2
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var tryCount = 0
        var lastException: IOException? = null

        while (tryCount <= maxRetries) {
            try {
                // PROCEED EXACTLY ONCE PER LOOP
                val response = chain.proceed(originalRequest)

                // Success -> return immediately
                if (response.isSuccessful) return response

                // Server error -> retry
                if (response.code in 500..599) {
                    Log.w(TAG, "Server error ${response.code}, retry $tryCount/$maxRetries")
                    response.close()
                    applyBackoff(tryCount++)
                    continue
                }

                // Client errors = do NOT retry
                return response

            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "Network error: ${e.message}, retry $tryCount/$maxRetries")

                if (!isRetryableException(e)) {
                    throw e
                }

                // Retry
                applyBackoff(tryCount++)
            }
        }

        throw lastException ?: IOException("Failed after $maxRetries retries")
    }

    private fun isRetryableException(exception: IOException): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true
            is java.net.SocketException -> true
            is java.net.UnknownHostException -> false
            is javax.net.ssl.SSLException -> false
            else -> exception.message?.contains("broken pipe", ignoreCase = true) ?: false
        }
    }

    private fun applyBackoff(attempt: Int) {
        if (attempt == 0) return
        val delay = INITIAL_BACKOFF_MS * BACKOFF_MULTIPLIER.toDouble()
            .pow((attempt - 1).toDouble()).toLong()
        Thread.sleep(delay)
    }
}
