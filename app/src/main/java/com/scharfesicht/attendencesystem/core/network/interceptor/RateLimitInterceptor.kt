package com.scharfesicht.attendencesystem.core.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLimitInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val TAG = "RateLimit"
        private const val MAX_REQUESTS_PER_SECOND = 10
        private const val TIME_WINDOW_MS = 1000L
    }

    private val requestTimestamps = ConcurrentHashMap<String, MutableList<Long>>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val endpoint = request.url.encodedPath

        synchronized(this) {
            val now = System.currentTimeMillis()
            val timestamps = requestTimestamps.getOrPut(endpoint) { mutableListOf() }

            // Remove timestamps older than the time window
            timestamps.removeAll { it < now - TIME_WINDOW_MS }

            // Check if rate limit exceeded
            if (timestamps.size >= MAX_REQUESTS_PER_SECOND) {
                val oldestTimestamp = timestamps.first()
                val waitTime = TIME_WINDOW_MS - (now - oldestTimestamp)

                if (waitTime > 0) {
                    Log.w(TAG, "Rate limit reached for $endpoint. Waiting ${waitTime}ms")
                    Thread.sleep(waitTime)
                }
            }

            // Add current timestamp
            timestamps.add(System.currentTimeMillis())
        }

        val response = chain.proceed(request)

        // Handle 429 Too Many Requests
        if (response.code == 429) {
            val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: 5L
            Log.w(TAG, "Server rate limit hit. Retry after ${retryAfter}s")
            throw RateLimitException("Rate limit exceeded. Retry after $retryAfter seconds")
        }

        return response
    }
}

class RateLimitException(message: String) : IOException(message)