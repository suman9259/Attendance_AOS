package com.scharfesicht.attendencesystem.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.scharfesicht.attendencesystem.core.network.interceptor.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AttendanceSystemApp : Application() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    // Optional: app-wide coroutine scope
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.d(TAG, "AttendanceSystemApp initialized")

        // 🔹 Initialize services asynchronously (no UI delay)
        applicationScope.launch {
            // 1️⃣ Initialize Analytics
            initAnalytics()

            // 2️⃣ Initialize network monitoring
            initNetworkMonitor()

            // 3️⃣ Log Absher SDK availability
            logAbsherStatus()
        }
    }

    private fun initAnalytics() {
        // Enable Firebase Crashlytics collection
        // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        Log.d(TAG, "Analytics initialized")
    }

    private fun initNetworkMonitor() {
        // Trigger network monitor initialization
        networkMonitor.isConnected // lazy StateFlow initialization
        Log.d(TAG, "Network monitor initialized")
    }

    private fun logAbsherStatus() {
        try {
            // Check if Absher SDK classes are available
            Class.forName("sa.gov.moi.IMiniApp")
            Log.d(TAG, "Absher SDK is available")
        } catch (e: ClassNotFoundException) {
            Log.w(TAG, "Absher SDK not found - running in standalone mode")
        }
    }

    companion object {
        private const val TAG = "AttendanceSystemApp"

        lateinit var instance: AttendanceSystemApp
            private set
    }
}

// 🔸 Extension for easy access
val Context.app: AttendanceSystemApp
    get() = applicationContext as AttendanceSystemApp