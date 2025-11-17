package com.scharfesicht.attendencesystem.core.network.interceptor

import com.scharfesicht.attendencesystem.BuildConfig
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceStorage: IPreferenceStorage
) : Interceptor {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_ACCEPT = "Accept"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        private const val HEADER_APP_VERSION = "X-App-Version"
        private const val HEADER_DEVICE_TYPE = "X-Device-Type"
        private const val HEADER_LANGUAGE = "lang"
        private const val HEADER_PLATFORM = "X-Platform"
        private const val HEADER_BUILD_NUMBER = "X-Build-Number"
        private const val CONTENT_TYPE_JSON = "multipart/form-data"
        private const val DEVICE_TYPE = "Android"
        private const val PLATFORM = "android"
        private const val COMPANY_CODE = "code"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { preferenceStorage.jwtToken.firstOrNull() }

        val requestBuilder = originalRequest.newBuilder()
            .apply {
                token?.takeIf { it.isNotBlank() }?.let {
                    addHeader(HEADER_AUTHORIZATION, "Bearer $it")
                }
                addHeader(HEADER_ACCEPT, "application/json")
                // Only add content-type for POST/PUT/PATCH
                if (originalRequest.method != "GET") {
                    addHeader("Content-Type", "multipart/form-data")
                }
//                addHeader(HEADER_APP_VERSION, BuildConfig.VERSION_NAME)
//                addHeader(HEADER_BUILD_NUMBER, BuildConfig.VERSION_CODE.toString())
//                addHeader(HEADER_DEVICE_TYPE, DEVICE_TYPE)
//                addHeader(HEADER_PLATFORM, PLATFORM)
                addHeader(COMPANY_CODE, BuildConfig.COMPANY_CODE)
                addHeader(HEADER_LANGUAGE, "en")
                method(originalRequest.method, originalRequest.body)
            }

        return chain.proceed(requestBuilder.build())
    }
}

/*
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_ACCEPT = "Accept"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Skip auth for login endpoint
        if (original.url.encodedPath.contains("/login")) {
            Log.d(TAG, "Skipping auth for login endpoint")
            return chain.proceed(original)
        }

        val token = tokenManager.getJwtToken()

        val request = if (token != null && tokenManager.isTokenValid(token)) {
            Log.d(TAG, "Adding auth token to request: ${original.url}")
            original.newBuilder()
                .header(HEADER_AUTHORIZATION, "Bearer $token")
                .header(HEADER_ACCEPT, "application/json")
                .header(HEADER_CONTENT_TYPE, "application/json")
                .build()
        } else {
            Log.w(TAG, "No valid token available for request: ${original.url}")
            original.newBuilder()
                .header(HEADER_ACCEPT, "application/json")
                .header(HEADER_CONTENT_TYPE, "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}*/
