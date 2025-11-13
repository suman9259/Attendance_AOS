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
import androidx.compose.ui.platform.LocalContext
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
import com.scharfesicht.attendencesystem.features.facecompare.presentation.ui.FaceVerifyScreen
import com.scharfesicht.attendencesystem.features.facecompare.presentation.viewmodel.FaceCompareViewModel

@Composable
fun AttendanceDashboardScreen(
    viewModel: AttendanceDashboardViewModel = hiltViewModel(),
    absherViewModel: AbsherViewModel = hiltViewModel(),
    faceCompareViewModel: FaceCompareViewModel = hiltViewModel(),
    isAbsherEnabled: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val punchLoading by viewModel.punchInOutLoading.collectAsState()

    val context = LocalContext.current
    val showFaceScreen = remember { mutableStateOf(false) }
    val punchType = remember { mutableStateOf("") } // "IN" / "OUT"

    // OLD IMAGE URL from login response
    val oldImageUrl = "https://hrmpro.time-365.com/storage/images/profile/time-365_188264/537304871511132025095607691581079ddb1.jpg"
    LaunchedEffect(oldImageUrl) {
        faceCompareViewModel.setOldImageUrl(oldImageUrl)
    }

    // -------------------------
    // FACE SCREEN RESULT HANDLER
    // -------------------------
    if (showFaceScreen.value) {
        FaceVerifyScreen(
            onResult = { isMatch, accuracy ->

                showFaceScreen.value = false

                if (isMatch) {
                    if (punchType.value == "IN") {
                        viewModel.punchIn()
                    } else {
                        viewModel.punchOut()
                    }
                } else {
                    viewModel.showAlert(
                        title = "Face Not Recognized",
                        message = "Match score: $accuracy%\nTry again."
                    )
                }
            }
        )
        return
    }

    // -------------------------
    // MAIN DASHBOARD UI
    // -------------------------
    AttendanceDashboardContent(
        state = uiState as? AttendanceDashboardUiState.Success ?: return,
        onPunchIn = {
            punchType.value = "IN"
            showFaceScreen.value = true
        },
        onPunchOut = {
            punchType.value = "OUT"
            showFaceScreen.value = true
        },
        punchInOutLoading = punchLoading,
        isArabic = absherViewModel.getCurrentLanguage() != "en",
        onTabSelected = { AttendanceTab.MARK_ATTENDANCE },
    )
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