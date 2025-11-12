package com.scharfesicht.attendencesystem.core.network.interceptor

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CacheInterceptor @Inject constructor(
    @param:ApplicationContext private val context: Context
) : Interceptor {

    companion object {
        private const val CACHE_MAX_AGE_ONLINE = 5
        private const val CACHE_MAX_STALE_OFFLINE = 7
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.method != "GET") {
            return chain.proceed(request)
        }

        val cacheControl = if (isNetworkAvailable()) {
            CacheControl.Builder()
                .maxAge(CACHE_MAX_AGE_ONLINE, TimeUnit.MINUTES)
                .build()
        } else {
            CacheControl.Builder()
                .onlyIfCached()
                .maxStale(CACHE_MAX_STALE_OFFLINE, TimeUnit.DAYS)
                .build()
        }

        val newRequest = request.newBuilder()
            .cacheControl(cacheControl)
            .build()

        val response = chain.proceed(newRequest)

        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header(
                "Cache-Control",
                "public, max-age=${TimeUnit.MINUTES.toSeconds(CACHE_MAX_AGE_ONLINE.toLong())}"
            )
            .build()
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @Suppress("DEPRECATION")
    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as? ConnectivityManager ?: return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                    ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo?.isConnectedOrConnecting == true
            }
        } catch (e: Exception) {
            false
        }
    }
}