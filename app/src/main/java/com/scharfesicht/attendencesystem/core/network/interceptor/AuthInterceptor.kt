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
        private const val HEADER_LANGUAGE = "lang"
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
                if (originalRequest.method != "GET") {
                    addHeader("Content-Type", "multipart/form-data")
                }
                addHeader(COMPANY_CODE, BuildConfig.COMPANY_CODE)
                addHeader(HEADER_LANGUAGE, "en")
                method(originalRequest.method, originalRequest.body)
            }

        return chain.proceed(requestBuilder.build())
    }
}
