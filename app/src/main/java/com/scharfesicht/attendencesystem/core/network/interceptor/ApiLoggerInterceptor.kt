package com.scharfesicht.attendencesystem.core.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class ApiLoggerInterceptor : Interceptor {

    companion object {
        private const val TAG = "API"
        private const val MAX_LOG_LENGTH = 4000
        private const val BODY_PREVIEW_LENGTH = 1000
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()

        logRequest(request)

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Request failed: ${request.url}", e)
            throw e
        }

        val endTime = System.nanoTime()
        val duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime)

        logResponse(response, duration)

        return response
    }

    private fun logRequest(request: okhttp3.Request) {
        try {
            val method = request.method
            val url = request.url

            Log.d(TAG, "┌──────────────────────────────────────────────────────────")
            Log.d(TAG, "│ ➡️ REQUEST: $method $url")
            Log.d(TAG, "├──────────────────────────────────────────────────────────")

            if (request.headers.size > 0) {
                Log.v(TAG, "│ Headers:")
                request.headers.forEach { (name, value) ->
                    val maskedValue = if (name.equals("Authorization", ignoreCase = true)) {
                        value.take(20) + "..."
                    } else {
                        value
                    }
                    Log.v(TAG, "│   $name: $maskedValue")
                }
            }

            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                val charset = body.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                val bodyString = buffer.readString(charset)

                if (bodyString.isNotEmpty()) {
                    Log.v(TAG, "│ Body:")
                    logLongString(bodyString.take(BODY_PREVIEW_LENGTH))
                }
            }

            Log.d(TAG, "└──────────────────────────────────────────────────────────")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging request", e)
        }
    }

    private fun logResponse(response: Response, durationMs: Long) {
        try {
            val request = response.request
            val code = response.code
            val message = response.message
            val isSuccessful = response.isSuccessful

            val emoji = when {
                isSuccessful -> "✅"
                code in 400..499 -> "⚠️"
                code >= 500 -> "❌"
                else -> "ℹ️"
            }

            Log.d(TAG, "┌──────────────────────────────────────────────────────────")
            Log.d(TAG, "│ $emoji RESPONSE: $code $message | ${request.url}")
            Log.d(TAG, "│ ⏱️ Duration: ${durationMs}ms")
            Log.d(TAG, "├──────────────────────────────────────────────────────────")

            if (response.headers.size > 0) {
                Log.v(TAG, "│ Headers:")
                response.headers.forEach { (name, value) ->
                    Log.v(TAG, "│   $name: $value")
                }
            }

            val responseBody = response.body
            val source = responseBody?.source()
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer

            val charset = responseBody?.contentType()?.charset(StandardCharsets.UTF_8)
                ?: StandardCharsets.UTF_8

            val bodyString = buffer?.clone()?.readString(charset) ?: ""

            if (bodyString.isNotEmpty()) {
                Log.v(TAG, "│ Body:")
                if (!isSuccessful) {
                    Log.e(TAG, "│ Error Body:")
                }
                logLongString(bodyString.take(BODY_PREVIEW_LENGTH))

                if (bodyString.length > BODY_PREVIEW_LENGTH) {
                    Log.v(TAG, "│   ... (truncated)")
                }
            }

            Log.d(TAG, "└──────────────────────────────────────────────────────────")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging response", e)
        }
    }

    private fun logLongString(message: String) {
        val lines = message.split("\n")
        lines.forEach { line ->
            if (line.length > MAX_LOG_LENGTH) {
                var i = 0
                while (i < line.length) {
                    val end = minOf(i + MAX_LOG_LENGTH, line.length)
                    Log.v(TAG, "│   ${line.substring(i, end)}")
                    i = end
                }
            } else {
                Log.v(TAG, "│   $line")
            }
        }
    }
}