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
        val TAG = "AbsherMiniApp"
        try {
            Log.i(TAG, "Launching Mini App via Absher SDK")

            val appContext = context.applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(appContext, MiniAppHiltEntryPoint::class.java)

            AttendanceSystemApp.absherHelper = data
            entryPoint.absherDataSource().setAbsherHelper(data)

            val intent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_LAUNCHED_FROM_SUPER_APP, true)
            }

            appContext.startActivity(intent)
            Log.i(TAG, "🚀 Mini App launched successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Mini App launch failed", e)
            throw IllegalStateException("MiniApp launch failure", e)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MiniAppHiltEntryPoint {
        fun absherDataSource(): AbsherDataSourceImpl
    }

    companion object {
        const val EXTRA_LAUNCHED_FROM_SUPER_APP = "launched_from_super_app"

        fun simulateLaunch(context: Context) {
            if (BuildConfig.DEBUG) {
                runCatching {
                    Log.d("AbsherMiniApp", "Simulating Absher SDK in DEV mode")
                    val mockHelper = com.scharfesicht.attendencesystem.app.mock.MockAbsherHelper()
                    MiniAppEntryPoint().launch(context, mockHelper)
                }.onFailure {
                    Log.e("AbsherMiniApp", "Simulation failed", it)
                }
            }
        }
    }
}
