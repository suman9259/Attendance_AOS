package com.scharfesicht.attendencesystem.app

import android.content.Context
import android.content.Intent
import android.util.Log
import com.scharfesicht.attendencesystem.BuildConfig
import com.scharfesicht.attendencesystem.data.absher.source.AbsherDataSourceImpl
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import sa.gov.moi.absherinterior.core_logic.IMiniApp

class MiniAppEntryPoint : IMiniApp {

    override fun launch(context: Context, data: IAbsherHelper) {
        try {
            Log.d(TAG, "MiniApp launch() called from Super App  Is Debug Mode (${BuildConfig.DEBUG})")

            val appContext = context.applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(
                appContext,
                MiniAppHiltEntryPoint::class.java
            )

            // ✅ Set globally before DI modules initialize
            AttendanceSystemApp.absherHelper = data
            entryPoint.absherDataSource().setAbsherHelper(data)

            Log.d(TAG, "✅ Absher helper injected into data source and app")

            // Launch main activity
            val intent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_LAUNCHED_FROM_SUPER_APP, true)
            }

            appContext.startActivity(intent)
            Log.d(TAG, "🚀 Mini App launched successfully via Absher SDK")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to launch Mini App", e)
            throw IllegalStateException("MiniApp launch failed", e)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MiniAppHiltEntryPoint {
        fun absherDataSource(): AbsherDataSourceImpl
    }

    companion object {
        private const val TAG = "AbsherAppLog"
        const val EXTRA_LAUNCHED_FROM_SUPER_APP = "launched_from_super_app"

        /**
         * 🔧 For DEV builds: simulate Absher launch with a MockAbsherHelper
         */
        fun simulateLaunch(context: Context) {
            if (BuildConfig.DEBUG) {
                try {
                    Log.d(TAG, "⚙️ Simulating Absher SDK in dev mode")
                    val mockHelper =
                        com.scharfesicht.attendencesystem.app.mock.MockAbsherHelper()
                    MiniAppEntryPoint().launch(context, mockHelper)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to simulate Absher launch", e)
                }
            } else {
                Log.d(TAG, "🟢 Production build - skipping simulation")
            }
        }
    }
}
