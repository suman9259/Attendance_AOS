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
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount < maxRetries) {
            try {
                response?.close()
                response = chain.proceed(request)

                if (response.isSuccessful) {
                    return response
                }

                if (response.code in 500..599) {
                    Log.w(TAG, "Server error ${response.code}, retry $tryCount/$maxRetries")
                    tryCount++
                    if (tryCount < maxRetries) {
                        response.close()
                        applyBackoff(tryCount)
                        continue
                    }
                }

                return response

            } catch (e: IOException) {
                exception = e
                Log.w(TAG, "Network error: ${e.message}, retry $tryCount/$maxRetries")

                if (isRetryableException(e)) {
                    tryCount++
                    if (tryCount < maxRetries) {
                        applyBackoff(tryCount)
                        continue
                    }
                }
                throw e
            }
        }

        return response ?: throw (exception ?: IOException("Failed after $maxRetries retries"))
    }

    private fun isRetryableException(exception: IOException): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true
            is java.net.SocketException -> true
            is java.net.UnknownHostException -> false
            is javax.net.ssl.SSLException -> false
            else -> exception.message?.let {
                it.contains("timeout", ignoreCase = true) ||
                        it.contains("connection reset", ignoreCase = true) ||
                        it.contains("broken pipe", ignoreCase = true)
            } ?: false
        }
    }

    private fun applyBackoff(attempt: Int) {
        try {
            val backoffDelay = INITIAL_BACKOFF_MS * BACKOFF_MULTIPLIER.toDouble()
                .pow((attempt - 1).toDouble()).toLong()
            Thread.sleep(backoffDelay)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
