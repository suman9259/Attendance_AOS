package com.scharfesicht.attendencesystem.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.scharfesicht.attendencesystem.app.navigation.AppNavGraph
import com.scharfesicht.attendencesystem.app.ui.theme.AttendanceSystemTheme
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import com.scharfesicht.attendencesystem.core.network.interceptor.NetworkMonitor
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var preferenceStorage: IPreferenceStorage

    @Inject
    lateinit var absherRepository: AbsherRepository

    private var isLaunchedFromSuperApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if launched from Super App
        isLaunchedFromSuperApp = intent?.getBooleanExtra(
            MiniAppEntryPoint.EXTRA_LAUNCHED_FROM_SUPER_APP,
            false
        ) ?: false

        // Modern immersive UI with edge-to-edge support
        enableEdgeToEdge()

        // Handle different navigation modes
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
}

@Composable
fun AttendanceSystemAppContent(
    networkMonitor: NetworkMonitor,
    preferenceStorage: IPreferenceStorage,
    absherRepository: AbsherRepository,
    isLaunchedFromSuperApp: Boolean
) {
    val context = LocalContext.current

    // Get local theme preferences
    val localThemeMode by preferenceStorage.themeMode.collectAsState(initial = "system")
    val dynamicColor by preferenceStorage.dynamicColor.collectAsState(initial = false)

    // Check if Absher is available
    val isAbsherEnabled = absherRepository.isAbsherInitialized()

    // Apply AttendanceSystem Theme
    // If launched from Super App, Absher theme will be applied in the AbsherUserScreen
    // For now, use local preferences
    AttendanceSystemTheme(
        themeMode = localThemeMode,
        dynamicColor = dynamicColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Navigation Graph
            AppNavGraph(
                isAbsherEnabled = isAbsherEnabled,
                isLaunchedFromSuperApp = isLaunchedFromSuperApp
            )
        }
    }
}

/*
* 1. /api/v1/attendance/checkin
* 2. /api/v1/attendance/middle-punch
* 3. /api/v1/attendance/checkout
* 4. /api/v1/logout
* 5. /api/v1/get-user-shifts
* 6. /api/v1/attendance/latest-record
* 7. /api/v1/login
* 8. /api/v1/refresh-user
*
* */