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
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        Log.d(TAG, "Activity created")

        handleAbsherLaunch()
    }

    private fun handleAbsherLaunch() {
        try {
            // 🧠 Detect if launched from Super App
            isLaunchedFromSuperApp = intent?.getBooleanExtra(
                MiniAppEntryPoint.EXTRA_LAUNCHED_FROM_SUPER_APP, false
            ) ?: false

            Log.i(TAG, "Launch Source: ${if (isLaunchedFromSuperApp) "Super App" else "Standalone"}")

            // ✅ Auto-mock in debug
            if (BuildConfig.DEBUG && AttendanceSystemApp.absherHelper == null) {
                Log.w(TAG, "Debug mode: simulating Absher SDK")
                MiniAppEntryPoint.simulateLaunch(this)
                finish()
                return
            }

            setAbsherHelperFromIntent()
            renderContent()

        } catch (e: Exception) {
            Log.e(TAG, "Fatal Absher initialization failure", e)
            // Optionally show an error screen or fallback UI
        }
    }

    private fun setAbsherHelperFromIntent() {
        if (AttendanceSystemApp.absherHelper != null) {
            Log.d(TAG, "Absher helper already available")
            return
        }

        val helper = intent?.extras?.get("absher_helper") as? IAbsherHelper
        if (helper != null) {
            AttendanceSystemApp.absherHelper = helper
            Log.i(TAG, "✅ Absher helper received from Intent")
        } else {
            Log.w(TAG, "⚠️ No Absher helper in Intent - fallback to standalone mode")
        }
    }

    private fun renderContent() {
        setContent {
            AttendanceSystemAppContent(
                networkMonitor = networkMonitor,
                preferenceStorage = preferenceStorage,
                absherRepository = absherRepository,
                isLaunchedFromSuperApp = isLaunchedFromSuperApp
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        setAbsherHelperFromIntent()

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
    val TAG = "AbsherAppUI"

    val localThemeMode by preferenceStorage.themeMode.collectAsState(initial = "system")
    val dynamicColor by preferenceStorage.dynamicColor.collectAsState(initial = false)
    val absherUiState by absherViewModel.uiState.collectAsState()

    LaunchedEffect(isLaunchedFromSuperApp) {
        runCatching {
            if (absherRepository.isAbsherInitialized()) {
                absherViewModel.loadUserInfo()
                Log.d(TAG, "Absher initialized — loading user info")
            } else {
                Log.d(TAG, "Standalone mode — skipping Absher load")
            }
        }.onFailure {
            Log.e(TAG, "Error during Absher initialization", it)
        }
    }

    val (language, themeMode, isRTL) = when (val state = absherUiState) {
        is AbsherUiState.Success -> Triple(state.userInfo.language, state.userInfo.theme.name.lowercase(), state.userInfo.isRTL)
        is AbsherUiState.Error -> Triple(AppLanguage.ENGLISH, localThemeMode, false)
        else -> Triple(AppLanguage.ENGLISH, localThemeMode, false)
    }

    AttendanceSystemTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
        LocalizationProvider(language = language, isRTL = isRTL) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    isAbsherEnabled = absherRepository.isAbsherInitialized(),
                    isLaunchedFromSuperApp = isLaunchedFromSuperApp
                )
            }
        }
    }
}