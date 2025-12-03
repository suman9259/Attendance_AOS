package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getDateText
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getMarkAttendanceTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getNoAttendanceRecordsText
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getPermissionTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getPunchInCardTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getPunchOutCardTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getWorkingHoursTitle
import com.scharfesicht.attendencesystem.app.navigation.NavManager
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceLogsViewModel
import sa.gov.moi.absherinterior.R
import sa.gov.moi.absherinterior.components.*
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.theme.base
import sa.gov.moi.absherinterior.theme.small
import sa.gov.moi.absherinterior.theme.xSmall
import sa.gov.moi.absherinterior.utils.*

@Composable
fun AttendanceLogsScreen(
    navManager: NavManager,
    viewModel: AttendanceLogsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(Unit) {viewModel.loadAttendanceLogs()}

    MainScreenView(
        uiState = uiState.screenState,  // ALWAYS Success/Success(logs)/Error
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
                title = MiniAppEntryPoint.getServiceTitle(),
                generalIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_as_admin),
                        contentDescription = "time attendance"
                    )
                },
                onBackClicked = { navManager.navigateBack() }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = uiState.errorMessage,
        successComposable = {

            // 👉 Show SHIMMER here when ViewModel says loading
            if (uiState.isLoading) {
                AttendanceLogsShimmer()
            } else {
                AttendanceLogsContent(
                    selectedTab = selectedTab,
                    onTabChanged = viewModel::onTabChanged,
                    attendanceLogs = uiState.attendanceLogs,
                )
            }
        }
    )
}

@Composable
private fun AttendanceLogsShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // ─────────────────── Tabs Shimmer ───────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shimmerEffect()
            )
        }

        20.0.MOIVerticalSpacer()

        // ─────────────────── Header Shimmer ───────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(18.dp)
                    .shimmerEffect()
            )
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .shimmerEffect()
                )
            }
        }

        12.0.MOIVerticalSpacer()

        // ─────────────────── List Shimmer Rows ───────────────────
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(6) {
                AttendanceLogItemShimmer()
            }
        }
    }
}

@Composable
private fun AttendanceLogItemShimmer() {
    MOICard(
        cornerSize = CardSize.MEDIUM,
        padding = PaddingValues(10.dp),
        cardContent = {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Date card shimmer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .shimmerEffect()
                )

                // Punch In shimmer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .shimmerEffect()
                )

                // Punch Out shimmer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .shimmerEffect()
                )

                // Working Hours shimmer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .shimmerEffect()
                )
            }
        }
    )
}

@Composable
private fun AttendanceLogsContent(
    selectedTab: Int,
    onTabChanged: (Int) -> Unit,
    attendanceLogs: List<AttendanceLog>?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        // ─────────────────── Tabs ───────────────────
        CustomTabRow(
            selectedTabIndex = selectedTab,
            tabType = TabType.OUTLINED,
            backgroundColor = Color.Transparent,
            indicatorColor = colorResource(R.color.primary_main),
            tabs = {
                CustomIndicatorTab(
                    isSelected = selectedTab == 0,
                    tabTitle = getMarkAttendanceTitle() ?: "",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
                    tabTitle = getPermissionTitle() ?: "",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = { onTabChanged(1) }
                )
            }
        )

        16.0.MOIVerticalSpacer()

        AttendanceTableHeader()

        8.0.MOIVerticalSpacer()

        // ─────────────────── Logs List ───────────────────
        attendanceLogs?.let { logs ->
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getNoAttendanceRecordsText() ?: "",
                        style = Typography().base,
                        color = colorResource(R.color.dark_gray_400)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(logs) { log ->
                        AttendanceLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = getDateText() ?: "",
            modifier = Modifier.weight(0.8f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color),
            textAlign = TextAlign.Center
        )
        Text(
            text = getPunchInCardTitle() ?: "",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color),
            textAlign = TextAlign.Center
        )
        Text(
            text = getPunchOutCardTitle() ?: "",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color),
            textAlign = TextAlign.Center
        )
        Text(
            text = getWorkingHoursTitle() ?: "",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AttendanceLogItem(log: AttendanceLog) {
    val statusColor = when (log.status) {
        AttendanceLogStatus.PRESENT -> colorResource(R.color.green_main_400)
        AttendanceLogStatus.LATE_LESS_THAN_1H -> colorResource(R.color.card_bg_color)
        AttendanceLogStatus.LATE_MORE_THAN_1H -> colorResource(R.color.read_bg_color)
        AttendanceLogStatus.EARLY_PUNCH_OUT -> colorResource(R.color.app_bg_color)
        AttendanceLogStatus.ABSENCE -> colorResource(R.color.read_bg_color)
    }

    MOICard(
        cornerSize = CardSize.MEDIUM,
        padding = PaddingValues(vertical = 3.dp),
        cardContent = {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Date Card
                MOICard(
                    modifier = Modifier.weight(1f),
                    cornerSize = CardSize.LARGE,
                    padding = PaddingValues(10.dp),
                    cardColor = statusColor,
                    cardContent = {
                        Column(
                            Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = log.dayName,
                                style = Typography().small.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            2.0.MOIVerticalSpacer()
                            Text(
                                text = log.dayNumber,
                                style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                )

                Spacer(Modifier.width(8.dp))

                // Punch In
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.punchInTime.isNotEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = "Punch In",
                            tint = colorResource(R.color.primary_main),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = log.punchInTime,
                            style = Typography().small.copy(fontWeight = FontWeight.SemiBold),
                            color = colorResource(R.color.content_fg_color)
                        )
                    } else {
                        Text("----", style = Typography().small, color = colorResource(R.color.dark_gray_100))
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Punch Out
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.punchOutTime.isNotEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_up),
                            contentDescription = "Punch Out",
                            tint = colorResource(R.color.primary_main),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = log.punchOutTime,
                            style = Typography().small.copy(fontWeight = FontWeight.Bold),
                            color = colorResource(R.color.content_fg_color)
                        )
                    } else {
                        Text("----", style = Typography().small, color = colorResource(R.color.dark_gray_400))
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Working Hours
                Text(
                    text = log.workingHours.ifEmpty { "----" },
                    modifier = Modifier.weight(1f),
                    style = Typography().small.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.content_fg_color)
                )
            }
        }
    )
}

/* Updated Data class for logs with API fields */
data class AttendanceLog(
    val dayName: String,
    val dayNumber: String,
    val punchInTime: String,
    val punchOutTime: String,
    val workingHours: String,
    val lateDuration: String = "",
    val status: AttendanceLogStatus = AttendanceLogStatus.PRESENT,
    // Additional fields from API
    val uuid: String = "",
    val checkinMediaUrl: String = "",
    val checkoutMediaUrl: String = "",
    val shiftName: String = ""
)

enum class AttendanceLogStatus {
    PRESENT,
    LATE_LESS_THAN_1H,
    LATE_MORE_THAN_1H,
    EARLY_PUNCH_OUT,
    ABSENCE
}