package com.scharfesicht.attendencesystem.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper

@HiltAndroidApp
class AttendanceSystemApp : Application() {

    companion object {
        private const val TAG = "AttendanceSystemApp"

        /**
         * Absher helper instance set by Super App
         * or manually for standalone testing.
         */
        @Volatile
        var absherHelper: IAbsherHelper? = null
            set(value) {
                field = value
                Log.d(TAG, "Absher helper ${if (value != null) "SET ✅" else "CLEARED ⚠️"}")
            }
            get() {
                Log.d(TAG, "Absher helper accessed: ${field != null}")
                return field
            }
    }
}
