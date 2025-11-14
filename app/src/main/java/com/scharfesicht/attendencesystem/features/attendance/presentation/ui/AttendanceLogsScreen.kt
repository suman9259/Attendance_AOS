package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceLogsViewModel
import sa.gov.moi.absherinterior.R
import sa.gov.moi.absherinterior.components.*
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.theme.AbsherInteriorTheme
import sa.gov.moi.absherinterior.theme.base
import sa.gov.moi.absherinterior.theme.small
import sa.gov.moi.absherinterior.theme.xSmall
import sa.gov.moi.absherinterior.utils.*

@Composable
fun AttendanceLogsScreen(
    navController: NavController,
    viewModel: AttendanceLogsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedAttendanceType by viewModel.selectedAttendanceType.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    MainScreenView(
        uiState = uiState.screenState,
        topBar = {
            if (isDarkMode) {
                AbsherAppBar(
                    title = "stringResource(R.string.attendance_system)",
                    navController = navController,
                    showEventTheme = false,
                    generalIcon = {
                        CustomIconButton(
                            icon = R.drawable.profile_off,
                            onClick = { },
                            iconSize = 22,
                            tint = colorResource(R.color.header_fg_color)
                        )
                    }
                )
            } else {
                AbsherAppBarLarge(
                    showEventTheme = false,
                    appBarContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomIconButton(
                                icon = R.drawable.ic_menu,
                                onClick = { },
                                tint = colorResource(R.color.header_fg_color)
                            )

                            CustomIconButton(
                                icon = R.drawable.ic_arrow_right,
                                onClick = { navController.navigateUp() },
                                iconSize = 18,
                                tint = colorResource(R.color.header_fg_color)
                            )
                        }
                    },
                    centerLogo = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            SvgImageRender(
                                url = R.drawable.logo_primary,
                                imageSize = 64.dp,
                                desc = "MOI Logo",
                                imageType = ImageType.LOCAL
                            )
                        }
                    }
                )
            }
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = uiState.errorMessage as AppMessage?,
        successComposable = {
            AttendanceLogsContent(
                selectedTab = selectedTab,
                onTabChanged = viewModel::onTabChanged,
                selectedAttendanceType = selectedAttendanceType,
                onAttendanceTypeChanged = viewModel::onAttendanceTypeChanged,
                selectedMonth = selectedMonth,
                onMonthChanged = viewModel::onMonthChanged,
                attendanceLogs = uiState.attendanceLogs,
                isDarkMode = isDarkMode
            )
        }
    )
}

@Composable
private fun AttendanceLogsContent(
    selectedTab: Int,
    onTabChanged: (Int) -> Unit,
    selectedAttendanceType: String,
    onAttendanceTypeChanged: (String) -> Unit,
    selectedMonth: String,
    onMonthChanged: (String) -> Unit,
    attendanceLogs: List<AttendanceLog>?,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Tab Row
        CustomTabRow(
            selectedTabIndex = selectedTab,
            tabType = TabType.OUTLINED,
            backgroundColor = Color.Transparent,
            indicatorColor = colorResource(R.color.primary_main),
            tabs = {
                CustomIndicatorTab(
                    isSelected = selectedTab == 0,
                    tabTitle = "stringResource(R.string.attendance_logs)",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
                    tabTitle = "stringResource(R.string.permission_request)",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(1) }
                )
            }
        )

        16.0.MOIVerticalSpacer()

        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attendance Type Dropdown
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDarkMode) colorResource(R.color.dark_gray_400)
                            else colorResource(R.color.card_bg_color)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    GenericSearch(
                        modifier = Modifier.width(140.dp),
                        items = listOf(
                            "stringResource(R.string.attendance)",
                            "stringResource(R.string.permission)",
                            "stringResource(R.string.leave)"
                        ),
                        onSelectedItem = { item, _ -> onAttendanceTypeChanged(item) },
                        hint = "stringResource(R.string.attendance)",
                        selectedItem = selectedAttendanceType
                    )
                }

                // Month Dropdown
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDarkMode) colorResource(R.color.dark_gray_400)
                            else colorResource(R.color.card_bg_color)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    LabeledDropdown(
                        modifier = Modifier.width(100.dp),
                        items = listOf("January", "February", "March", "April", "May"),
                        hint = "April",
                        selectedOption = selectedMonth,
                        onSelection = { item, _ -> onMonthChanged(item) }
                    )
                }
            }

            CustomIconButton(
                icon = R.drawable.ic_interior_ksa,
                onClick = { },
                iconSize = 22,
                tint = colorResource(R.color.primary_main),
                showContainer = true,
                containerColor = colorResource(
                    if (isDarkMode) R.color.dark_gray_400 else R.color.secondary_50
                ),
                buttonSize = 36.dp
            )
        }

        16.0.MOIVerticalSpacer()

        // Table Header
        AttendanceTableHeader(isDarkMode = isDarkMode)

        8.0.MOIVerticalSpacer()

        // Attendance List
        attendanceLogs?.let { logs ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    AttendanceLogItem(
                        log = log,
                        isDarkMode = isDarkMode
                    )
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingWidget()
            }
        }
    }
}

@Composable
private fun AttendanceTableHeader(isDarkMode: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "stringResource(R.string.date)",
            modifier = Modifier.weight(1.2f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
            text = "stringResource(R.string.punch_in)",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
            text = "stringResource(R.string.punch_out)",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
            text = "stringResource(R.string.working_hours)",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
    }
}

@Composable
private fun AttendanceLogItem(
    log: AttendanceLog,
    isDarkMode: Boolean
) {
    MOICard(
        cornerSize = CardSize.MEDIUM,
        cardColor = if (isDarkMode) colorResource(R.color.dark_gray_100)
        else colorResource(R.color.card_bg_color),
        border = 1.dp,
        borderColor = if (isDarkMode) colorResource(R.color.dark_gray_400)
        else colorResource(R.color.light_gray_75),
        padding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        cardContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Box
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorResource(R.color.primary_100)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = log.dayName,
                            style = Typography().xSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = log.dayNumber,
                            style = Typography().base.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Punch In Time
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.punchInTime.isNotEmpty()) {
                        SvgImageRender(
                            url = "R.drawable.ic_punch_in",
                            imageSize = 16.dp,
                            desc = "Punch In",
                            imageType = ImageType.LOCAL
                        )
                        Text(
                            text = log.punchInTime,
                            style = Typography().small,
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

                // Punch Out Time
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.punchOutTime.isNotEmpty()) {
                        SvgImageRender(
                            url =" R.drawable.ic_punch_out",
                            imageSize = 16.dp,
                            desc = "Punch Out",
                            imageType = ImageType.LOCAL
                        )
                        Text(
                            text = log.punchOutTime,
                            style = Typography().small,
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

// Data class
data class AttendanceLog(
    val dayName: String,
    val dayNumber: String,
    val punchInTime: String,
    val punchOutTime: String,
    val workingHours: String
)


// ========================================
// 2. ATTENDANCE LOGS PREVIEWS
// ========================================

@Preview(
    name = "Logs - Light Mode",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewAttendanceLogsLight() {
    AbsherInteriorTheme{
        AttendanceLogsPreview(isDarkMode = false)
    }
}

@Preview(
    name = "Logs - Dark Mode",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewAttendanceLogsDark() {
    AbsherInteriorTheme{
        AttendanceLogsPreview(isDarkMode = true)
    }
}

@Composable
private fun AttendanceLogsPreview(isDarkMode: Boolean) {
    var selectedTab by remember { mutableStateOf(0) }

    val mockLogs = listOf(
        AttendanceLog("الخميس", "15", "09:15am", "05:45pm", "08h30m"),
        AttendanceLog("الخميس", "15", "09:15am", "05:45pm", "08h30m"),
        AttendanceLog("الخميس", "15", "09:15am", "05:45pm", "08h30m"),
        AttendanceLog("الخميس", "15", "09:15am", "", "")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Mock AppBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isDarkMode) 88.dp else 200.dp)
                .background(
                    if (isDarkMode) colorResource(R.color.dark_gray_100)
                    else colorResource(R.color.header_bg_color)
                )
        )

        AttendanceLogsContent(
            selectedTab = selectedTab,
            onTabChanged = { selectedTab = it },
            selectedAttendanceType = "Attendance",
            onAttendanceTypeChanged = {},
            selectedMonth = "April",
            onMonthChanged = {},
            attendanceLogs = mockLogs,
            isDarkMode = isDarkMode
        )
    }
}
