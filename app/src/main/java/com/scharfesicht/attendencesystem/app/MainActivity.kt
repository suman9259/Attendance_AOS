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
import com.scharfesicht.attendencesystem.domain.absher.model.AppLanguage
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AbsherUiState
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AbsherViewModel
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
    @Inject lateinit var absherRepository: AbsherRepository

    private var isLaunchedFromSuperApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "═══════════════════════════════════")
        Log.d(TAG, "MainActivity onCreate")
        // ✅ DEV MODE: simulate Absher SDK
        if (BuildConfig.DEBUG  && AttendanceSystemApp.absherHelper == null) {
            Log.d(TAG, "⚙️ Dev environment detected — simulating Absher launch")
            MiniAppEntryPoint.simulateLaunch(this)
            finish()
            return
        }
        isLaunchedFromSuperApp = intent?.getBooleanExtra(
            MiniAppEntryPoint.EXTRA_LAUNCHED_FROM_SUPER_APP, false
        ) ?: false

        Log.d(TAG, "Launched from Super App: $isLaunchedFromSuperApp")

        // Set Absher helper if passed by intent
        tryGetAbsherHelperFromIntent()

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AttendanceSystemAppContent(
                networkMonitor = networkMonitor,
                preferenceStorage = preferenceStorage,
                absherRepository = absherRepository,
                isLaunchedFromSuperApp = isLaunchedFromSuperApp
            )
        }
    }

    private fun tryGetAbsherHelperFromIntent() {
        if (AttendanceSystemApp.absherHelper != null) {
            Log.d(TAG, "Absher helper already set, skipping Intent check")
            return
        }

        try {
            val helper = intent?.extras?.get("absher_helper") as? IAbsherHelper
                ?: intent?.getSerializableExtra("absher_helper") as? IAbsherHelper

            if (helper != null) {
                AttendanceSystemApp.absherHelper = helper
                Log.d(TAG, "✅ Absher helper received and set from Intent")
            } else {
                Log.d(TAG, "ℹ️ No Absher helper in Intent - running standalone")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Absher helper", e)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        tryGetAbsherHelperFromIntent()
    }
}



@Composable
fun AttendanceSystemAppContent(
    networkMonitor: NetworkMonitor,
    preferenceStorage: IPreferenceStorage,
    absherRepository: AbsherRepository,
    isLaunchedFromSuperApp: Boolean,
    absherViewModel: AbsherViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    // Load Absher data if available
    LaunchedEffect(isLaunchedFromSuperApp) {
        val isAbsherEnabled = try {
            absherRepository.isAbsherInitialized()
        } catch (e: Exception) {
            Log.e("AbsherAppLog", "Error checking Absher init", e)
            false
        }

        Log.d("AbsherAppLog", "Absher enabled: $isAbsherEnabled")
        Log.d("AbsherAppLog", "Launched from Super App: $isLaunchedFromSuperApp")

        if (isAbsherEnabled) {
            Log.d("AbsherAppLog", "Loading Absher user info...")
            absherViewModel.loadUserInfo()
        } else {
            Log.d("AbsherAppLog", "Skipping Absher - not initialized or standalone mode")
        }
    }

    // Get local theme preference
    val localThemeMode by preferenceStorage.themeMode.collectAsState(initial = "system")
    val dynamicColor by preferenceStorage.dynamicColor.collectAsState(initial = false)

    // Get Absher user info for language and theme
    val absherUiState by absherViewModel.uiState.collectAsState()

    // Determine language, theme, and RTL from Absher or defaults
    val (language, themeMode, isRTL) = when (val state = absherUiState) {
        is AbsherUiState.Success -> {
            Log.d("AbsherAppLog", "✅ Using Absher theme and language")
            Triple(
                state.userInfo.language,
                state.userInfo.theme.name.lowercase(),
                state.userInfo.isRTL
            )
        }
        is AbsherUiState.Loading -> {
            Log.d("AbsherAppLog", "⏳ Loading Absher data...")
            Triple(AppLanguage.ENGLISH, localThemeMode, false)
        }
        is AbsherUiState.Error -> {
            Log.e("AbsherAppLog", "❌ Absher error: ${state.message}")
            Triple(AppLanguage.ENGLISH, localThemeMode, false)
        }
        is AbsherUiState.NotInitialized -> {
            Log.d("AbsherAppLog", "ℹ️ Absher not initialized - using defaults")
            Triple(AppLanguage.ENGLISH, localThemeMode, false)
        }
        else -> {
            // Check if Absher is available but not loaded yet
            if (absherRepository.isAbsherInitialized()) {
                Log.d("AbsherAppLog", "Absher initialized, attempting direct access")
                // Try to get language and theme directly from repository
                val lang = absherRepository.getCurrentLanguage().getOrNull()?.let {
                    AppLanguage.from(it)
                } ?: AppLanguage.ENGLISH

                val theme = absherRepository.getCurrentTheme().getOrNull()?.lowercase()
                    ?: localThemeMode

                Triple(lang, theme, lang == AppLanguage.ARABIC)
            } else {
                Log.d("AbsherAppLog", "Using default theme and language")
                Triple(AppLanguage.ENGLISH, localThemeMode, false)
            }
        }
    }

    Log.d("AbsherAppLog", "Final - Language: $language, Theme: $themeMode, RTL: $isRTL")

    // Apply theme and language
    AttendanceSystemTheme(
        themeMode = themeMode,
        dynamicColor = dynamicColor
    ) {
        LocalizationProvider(
            language = language,
            isRTL = isRTL
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    isAbsherEnabled = absherRepository.isAbsherInitialized(),
                    isLaunchedFromSuperApp = isLaunchedFromSuperApp
                )
            }
        }
    }
}