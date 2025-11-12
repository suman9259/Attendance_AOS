package com.scharfesicht.attendencesystem.app

import android.content.Context
import android.content.Intent
import android.util.Log
import com.scharfesicht.attendencesystem.data.absher.source.AbsherDataSourceImpl
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import sa.gov.moi.absherinterior.core_logic.IMiniApp
import javax.inject.Inject

/**
 * Entry point for the Mini App.
 * This class is instantiated and launched by the Super App.
 */
class MiniAppEntryPoint : IMiniApp {

    /**
     * Called by the Super App to launch the Mini App
     * @param context Application or Activity context from Super App
     * @param data IAbsherHelper implementation from Super App
     */
    override fun launch(context: Context, data: IAbsherHelper) {
        try {
            Log.d(TAG, "MiniApp launch initiated")

            // Get application context
            val appContext = context.applicationContext

            // Access Hilt entry point to inject AbsherHelper
            val entryPoint = EntryPointAccessors.fromApplication(
                appContext,
                MiniAppEntryPoint::class.java
            )

            // Set the AbsherHelper in the data source
            entryPoint.absherDataSource().setAbsherHelper(data)

            // Launch MainActivity
            val intent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_LAUNCHED_FROM_SUPER_APP, true)
            }

            appContext.startActivity(intent)
            Log.d(TAG, "MiniApp launched successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch MiniApp", e)
            throw IllegalStateException("MiniApp launch failed", e)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MiniAppEntryPoint {
        fun absherDataSource(): AbsherDataSourceImpl
    }

    companion object {
        private const val TAG = "MiniAppEntryPoint"
        const val EXTRA_LAUNCHED_FROM_SUPER_APP = "launched_from_super_app"
    }
}