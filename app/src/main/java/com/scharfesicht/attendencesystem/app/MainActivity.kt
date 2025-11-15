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
import androidx.lifecycle.lifecycleScope
import com.scharfesicht.attendencesystem.app.navigation.AppNavGraph
import com.scharfesicht.attendencesystem.app.ui.theme.AttendanceSystemTheme
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import com.scharfesicht.attendencesystem.core.localization.LocalizationProvider
import com.scharfesicht.attendencesystem.core.network.TokenManager
import com.scharfesicht.attendencesystem.core.network.interceptor.NetworkMonitor
import com.scharfesicht.attendencesystem.core.utils.AppLanguage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var preferenceStorage: IPreferenceStorage
    @Inject lateinit var tokenManager: TokenManager

    private var isLaunchedFromSuperApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Check if launched from Absher
        isLaunchedFromSuperApp = intent.getBooleanExtra("FROM_ABSHER", false) ||
                MiniAppEntryPoint.isLaunchedFromAbsher()

        Log.d(TAG, "Activity created. Launched from Absher: $isLaunchedFromSuperApp")

        setContent {
            AttendanceSystemApplicationContent(
                networkMonitor = networkMonitor,
                preferenceStorage = preferenceStorage,
                isLaunchedFromSuperApp = isLaunchedFromSuperApp
            )
        }
    }

}

@Composable
fun AttendanceSystemApplicationContent(
    networkMonitor: NetworkMonitor,
    preferenceStorage: IPreferenceStorage,
    isLaunchedFromSuperApp: Boolean,
) {
    // Get theme and language from Absher or use defaults
    val themeMode = MiniAppEntryPoint.superData?.getCurrentTheme()?.data ?: "light"

    val language = remember {
        if (isLaunchedFromSuperApp) {
            MiniAppEntryPoint.superData?.getCurrentLanguage()?.data ?: "en"
        } else {
            "en"
        }
    }

    val isRTL = language == "ar"

    Log.d("AttendanceApp", "Theme: $themeMode, Language: $language, RTL: $isRTL")

    AttendanceSystemTheme(themeMode = themeMode) {
        LocalizationProvider(
            language = AppLanguage.from(language),
            isRTL = isRTL
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    isAbsherEnabled = isLaunchedFromSuperApp,
                    isLaunchedFromSuperApp = isLaunchedFromSuperApp
                )
            }
        }
    }
}