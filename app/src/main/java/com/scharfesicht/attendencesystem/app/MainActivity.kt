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
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import com.scharfesicht.attendencesystem.core.localization.LocalizationProvider
import com.scharfesicht.attendencesystem.core.network.TokenManager
import com.scharfesicht.attendencesystem.core.network.interceptor.NetworkMonitor
import com.scharfesicht.attendencesystem.core.utils.AppLanguage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.theme.AbsherInteriorTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var preferenceStorage: IPreferenceStorage
    @Inject lateinit var tokenManager: TokenManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AttendanceSystemApplicationContent()
        }
    }

}
@Composable
fun AttendanceSystemApplicationContent() {
    val absherLanguage = MiniAppEntryPoint.getCurrentLanguage()

    val isRTL = absherLanguage == "ar"

    Log.d("AttendanceApp", "Final Language: $absherLanguage | RTL=$isRTL")

    AbsherInteriorTheme {
        LocalizationProvider(
            language = AppLanguage.from(absherLanguage),
            isRTL = isRTL
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph()
            }
        }
    }
}
