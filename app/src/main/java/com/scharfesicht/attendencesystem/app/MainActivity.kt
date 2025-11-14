package com.scharfesicht.attendencesystem.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.scharfesicht.attendencesystem.BuildConfig
import com.scharfesicht.attendencesystem.app.navigation.AppNavGraph
import com.scharfesicht.attendencesystem.app.ui.theme.AttendanceSystemTheme
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import com.scharfesicht.attendencesystem.core.localization.LocalizationProvider
import com.scharfesicht.attendencesystem.core.network.interceptor.NetworkMonitor
import com.scharfesicht.attendencesystem.core.utils.AppLanguage
import dagger.hilt.android.AndroidEntryPoint
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "AbsherAppLog"
    }

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var preferenceStorage: IPreferenceStorage

    private var isLaunchedFromSuperApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        Log.d(TAG, "Activity created")

        setContent {
            AttendanceSystemApplicationContent(
                networkMonitor = networkMonitor,
                preferenceStorage = preferenceStorage,
                isLaunchedFromSuperApp = isLaunchedFromSuperApp

            )
            val id = MiniAppEntryPoint.superData?.getUserNationalID()?.data
            val token = MiniAppEntryPoint.superData?.getUserToken()?.data

            Log.e("Kuch Bhi Ho Sakta Hai", "Kuch Bhi Ho Sakta Hai $id and $token")
        }
    }

}




@Composable
fun AttendanceSystemApplicationContent(
    networkMonitor: NetworkMonitor,
    preferenceStorage: IPreferenceStorage,
    isLaunchedFromSuperApp: Boolean,
) {

    val themeMode : String = (MiniAppEntryPoint.superData?.getCurrentTheme()?.data?.firstOrNull() ?: "dark") as String
    val language = MiniAppEntryPoint.superData?.getCurrentLanguage()?.data?.firstOrNull() ?: "en"
    val isRTL = language != "en"


    AttendanceSystemTheme(themeMode = themeMode) {
        LocalizationProvider(language = AppLanguage.from(language.toString()), isRTL = isRTL) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    isAbsherEnabled = false,
                    isLaunchedFromSuperApp = isLaunchedFromSuperApp
                )
            }
        }
    }
}