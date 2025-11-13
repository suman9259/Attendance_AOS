package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scharfesicht.attendencesystem.app.ui.componants.MainAppTopAppBar
import com.scharfesicht.attendencesystem.app.ui.theme.AttendanceSystemTheme
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.components.*
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.*
import com.scharfesicht.attendencesystem.features.facecompare.presentation.viewmodel.FaceCompareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDashboardScreen(
    viewModel: AttendanceDashboardViewModel = hiltViewModel(),
    absherViewModel: AbsherViewModel = hiltViewModel(),
    isAbsherEnabled: Boolean,
    faceCompareViewModel: FaceCompareViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val punchInOutLoading by viewModel.punchInOutLoading.collectAsState()
    val showSuccessDialog by viewModel.showSuccessDialog.collectAsState()
    val appPreferences by viewModel.appPreferences.collectAsState()

    val isArabic = absherViewModel.getCurrentLanguage() != "en"
    val isDark = absherViewModel.getCurrentTheme() != "light"

    // Handle LTR / RTL layout direction
    CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        // Success Dialog
        showSuccessDialog?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissSuccessDialog() },
                title = {
                    Text(if (isArabic) "نجاح" else "Success")
                },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissSuccessDialog() }) {
                        Text(if (isArabic) "حسنًا" else "OK")
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                MainAppTopAppBar(
                    titleAr = "نظام الحضور والانصراف",
                    titleEn = "Time Attendance",
                    isArabic = isArabic,
                    isDark = isDark,
                )
            }
        ) { padding ->
            when (val state = uiState) {
                is AttendanceDashboardUiState.Loading -> {
                    // SHIMMER LOADING STATE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        // Tab Row (static during loading)
                        AttendanceTabRow(
                            selectedTab = AttendanceTab.MARK_ATTENDANCE,
                            onTabSelected = {},
                            isArabic = isArabic
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Shimmer Content
                        AttendanceDashboardShimmer(
                            isRtl = isArabic,
                            isDark = isDark
                        )
                    }
                }

                is AttendanceDashboardUiState.Success -> {
                    AttendanceDashboardContent(
                        state = state,
                        modifier = Modifier.padding(padding),
                        onTabSelected = { viewModel.selectTab(it) },
                        onPunchIn = { viewModel.punchIn() },
                        onPunchOut = { viewModel.punchOut() },
                        punchInOutLoading = punchInOutLoading,
                        isArabic = isArabic,
                    )
                }

                is AttendanceDashboardUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(
                                onClick = {  }
                            ) {
                                Text(if (isArabic) "إعادة المحاولة" else "Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceDashboardContent(
    state: AttendanceDashboardUiState.Success,
    modifier: Modifier = Modifier,
    onTabSelected: (AttendanceTab) -> Unit,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    punchInOutLoading: Boolean,
    isArabic: Boolean,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Tab Row
        AttendanceTabRow(
            selectedTab = state.selectedTab,
            onTabSelected = onTabSelected,
            isArabic = isArabic
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (state.selectedTab) {
            AttendanceTab.MARK_ATTENDANCE -> {
                MarkAttendanceContent(
                    shift = state.shift,
                    onPunchIn = onPunchIn,
                    onPunchOut = onPunchOut,
                    loading = punchInOutLoading,
                    isArabic = isArabic,
                )
            }

            AttendanceTab.PERMISSION_APPLICATION -> {
                PermissionApplicationContent(isArabic = isArabic)
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AttendanceTabRow(
    selectedTab: AttendanceTab,
    onTabSelected: (AttendanceTab) -> Unit,
    isArabic: Boolean
) {
    val textColorActive = MaterialTheme.colorScheme.primary
    val textColorInactive = Color.Gray

    val tabs = listOf(
        AttendanceTab.MARK_ATTENDANCE to if (isArabic) "تسجيل الحضور" else "Mark Attendance",
        AttendanceTab.PERMISSION_APPLICATION to if (isArabic) "طلب استئذان" else "Permission Application"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // Tab buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { (tab, label) ->
                TextButton(onClick = { onTabSelected(tab) }) {
                    Text(
                        text = label,
                        color = if (selectedTab == tab) textColorActive else textColorInactive,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Animated bottom indicator
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            val indicatorWidth = maxWidth / tabs.size
            val indicatorOffset by animateDpAsState(
                targetValue = when (selectedTab) {
                    AttendanceTab.MARK_ATTENDANCE -> if (isArabic) indicatorWidth else 0.dp
                    AttendanceTab.PERMISSION_APPLICATION -> if (isArabic) 0.dp else indicatorWidth
                },
                label = "tabIndicatorOffset"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .background(textColorActive)
            )
        }
    }
}

@Composable
fun MarkAttendanceContent(
    shift: ShiftData?,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    loading: Boolean,
    isArabic: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        shift?.let {
            ShiftCard(
                shift = it,
                onPunchIn = onPunchIn,
                onPunchOut = onPunchOut,
                loading = loading,
                isArabic = isArabic,
            )
        }
    }
}

@Composable
fun PermissionApplicationContent(isArabic: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isArabic) "طلب استئذان" else "Permission Application",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - English")
@Composable
private fun PreviewLightEnglish() {
    AttendanceSystemTheme(false) {
        AttendanceTabRow(
            selectedTab = AttendanceTab.MARK_ATTENDANCE,
            onTabSelected = {},
            isArabic = false,
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode - English")
@Composable
private fun PreviewDarkEnglish() {
    AttendanceSystemTheme(true) {
        AttendanceTabRow(
            selectedTab = AttendanceTab.MARK_ATTENDANCE,
            onTabSelected = {},
            isArabic = false,
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Arabic")
@Composable
private fun PreviewLightArabic() {
    AttendanceSystemTheme(false) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AttendanceTabRow(
                selectedTab = AttendanceTab.MARK_ATTENDANCE,
                onTabSelected = {},
                isArabic = true,
            )
        }
    }
}

@Preview(showBackground = true, name = "Shimmer - Light")
@Composable
private fun PreviewShimmerLight() {
    AttendanceSystemTheme(false) {
        AttendanceDashboardShimmer(isRtl = false, isDark = false)
    }
}

@Preview(showBackground = true, name = "Shimmer - Dark")
@Composable
private fun PreviewShimmerDark() {
    AttendanceSystemTheme(true) {
        AttendanceDashboardShimmer(isRtl = false, isDark = true)
    }
}

@Preview(showBackground = true, name = "Shimmer - Arabic RTL")
@Composable
private fun PreviewShimmerArabic() {
    AttendanceSystemTheme(false) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AttendanceDashboardShimmer(isRtl = true, isDark = false)
        }
    }
}