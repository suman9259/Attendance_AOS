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
        private const val HEADER_LANGUAGE = "Accept-Language"
        private const val HEADER_PLATFORM = "X-Platform"
        private const val HEADER_BUILD_NUMBER = "X-Build-Number"
        private const val CONTENT_TYPE_JSON = "application/json"
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
                addHeader(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                addHeader(HEADER_APP_VERSION, BuildConfig.VERSION_NAME)
                addHeader(HEADER_BUILD_NUMBER, BuildConfig.VERSION_CODE.toString())
                addHeader(HEADER_DEVICE_TYPE, DEVICE_TYPE)
                addHeader(HEADER_PLATFORM, PLATFORM)
                addHeader(COMPANY_CODE, BuildConfig.COMPANY_CODE)
                addHeader(HEADER_LANGUAGE, Locale.getDefault().toLanguageTag())
                method(originalRequest.method, originalRequest.body)
            }

        return chain.proceed(requestBuilder.build())
    }
}