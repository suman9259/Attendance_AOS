package com.scharfesicht.attendencesystem.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper

@HiltAndroidApp
class AttendanceSystemApp : Application() {

    companion object {
        private const val TAG = "AttendanceSystemApp"

        @Volatile
        private var _absherHelper: IAbsherHelper? = null

        /**
         * Safe global accessor for AbsherHelper.
         * Automatically logs state for debugging & analytics.
         */
        var absherHelper: IAbsherHelper?
            get() {
                if (BuildConfig.DEBUG) Log.d(TAG, "AbsherHelper accessed: ${_absherHelper != null}")
                return _absherHelper
            }
            set(value) {
                _absherHelper = value
                Log.i(TAG, "AbsherHelper ${if (value != null) "initialized ✅" else "cleared ⚠️"}")
            }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "AttendanceSystemApp initialized")
    }
}
