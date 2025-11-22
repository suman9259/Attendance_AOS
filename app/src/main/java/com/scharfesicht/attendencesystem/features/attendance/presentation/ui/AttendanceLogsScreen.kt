package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import androidx.compose.foundation.layout.*
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
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    MainScreenView(
        uiState = uiState.screenState,
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
                title = MiniAppEntryPoint.getServiceTitle(),
                generalIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_as_admin),
                        contentDescription = "time attendance"
                    )
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = uiState.errorMessage,
        successComposable = {
            // Show shimmer while loading
            if (uiState.isLoading) {
                AttendanceLogsShimmer()
            } else {
                AttendanceLogsContent(
                    selectedTab = selectedTab,
                    onTabChanged = viewModel::onTabChanged,
                    attendanceLogs = uiState.attendanceLogs,
                    isDarkMode = isDarkMode
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
        // Tab shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

        16.0.MOIVerticalSpacer()

        // Header shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(16.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .shimmerEffect()
            )
        }

        8.0.MOIVerticalSpacer()

        // List items shimmer
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(5) {
                AttendanceLogItemShimmer()
            }
        }
    }
}

@Composable
private fun AttendanceLogItemShimmer() {
    MOICard(
        cornerSize = CardSize.MEDIUM,
        padding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        cardContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date card shimmer
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(60.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Punch in shimmer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Punch out shimmer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Working hours shimmer
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
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        CustomTabRow(
            selectedTabIndex = selectedTab,
            tabType = TabType.OUTLINED,
            backgroundColor = Color.Transparent,
            indicatorColor = colorResource(R.color.primary_main),
            tabs = {
                CustomIndicatorTab(
                    isSelected = selectedTab == 0,
                    tabTitle = getMarkAttendanceTitle() ?:"",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
                    tabTitle = getPermissionTitle()?:"",
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
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = getDateText() ?:"",
            modifier = Modifier.weight(1.2f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
            text = getPunchInCardTitle() ?:"",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
            text = getPunchOutCardTitle() ?:"",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
            text = getWorkingHoursTitle() ?: "",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
    }
}

@Composable
private fun AttendanceLogItem(
    log: AttendanceLog
) {
    val statusColor = when (log.status) {
        AttendanceLogStatus.PRESENT -> colorResource(R.color.green_main_400)
        AttendanceLogStatus.LATE_LESS_THAN_1H -> colorResource(R.color.card_bg_color)
        AttendanceLogStatus.LATE_MORE_THAN_1H -> colorResource(R.color.read_bg_color)
        AttendanceLogStatus.EARLY_PUNCH_OUT -> colorResource(R.color.app_bg_color)
        AttendanceLogStatus.ABSENCE -> colorResource(R.color.read_bg_color)
    }

    MOICard(
        cornerSize = CardSize.MEDIUM,
        padding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        cardContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Card
                MOICard(
                    cornerSize = CardSize.LARGE,
                    modifier = Modifier.weight(1.2f),
                    padding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    cardColor = statusColor,
                    cardContent = {
                        Column(
                            Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = log.dayName,
                                style = Typography().xSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                modifier = Modifier.padding(top = 4.dp),
                                text = log.dayNumber,
                                style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Punch In
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        Text(
                            text = "----",
                            style = Typography().small,
                            color = colorResource(R.color.dark_gray_100)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Punch Out
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        Text(
                            text = "----",
                            style = Typography().small,
                            color = colorResource(R.color.dark_gray_400)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

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