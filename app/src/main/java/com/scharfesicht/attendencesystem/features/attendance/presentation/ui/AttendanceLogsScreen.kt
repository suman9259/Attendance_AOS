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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
    val selectedAttendanceType by viewModel.selectedAttendanceType.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    MainScreenView(
        uiState = uiState.screenState,
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
//                title = stringResource(R.string.time_attendance),
                title = "Time Attendance",
                generalIcon = {
                    Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = null)
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = uiState.errorMessage as? AppMessage,
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
        CustomTabRow(
            selectedTabIndex = selectedTab,
            tabType = TabType.OUTLINED,
            backgroundColor = Color.Transparent,
            indicatorColor = colorResource(R.color.primary_main),
            tabs = {
                CustomIndicatorTab(
                    isSelected = selectedTab == 0,
//                    tabTitle = stringResource(R.string.mark_attendance),
                    tabTitle = "mark attendance",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
//                    tabTitle = stringResource(R.string.permission_application),
                    tabTitle = "permission request",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(1) }
                )
            }
        )

        16.0.MOIVerticalSpacer()

        AttendanceTableHeader()

        8.0.MOIVerticalSpacer()

        attendanceLogs?.let { logs ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs) { log ->
                    AttendanceLogItem(log = log, isDarkMode = isDarkMode)
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingWidget()
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
//            text = stringResource(R.string.attendance_logs), // "Date" or "Attendance Logs"
            text = "Attendance", // "Date" or "Attendance Logs"
            modifier = Modifier.weight(1.2f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
//            text = stringResource(R.string.punch_in),
            text = "Punch In",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
//            text = stringResource(R.string.punch_out),
            text = "Punch Out",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
//            text = stringResource(R.string.working_hours),
            text = "Work Hours",
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
        padding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        cardContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MOICard(
                    cornerSize = CardSize.LARGE,
                    modifier = Modifier.weight(1f),
                    padding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    cardColor = colorResource(R.color.green_main_400),
                    cardContent = {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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

                // Punch In
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.punchInTime.isNotEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
//                            contentDescription = stringResource(R.string.punch_in),
                            contentDescription = "Punch In",
                            tint = colorResource(R.color.primary_main),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = log.punchInTime,
                            style = Typography().base.copy(fontWeight = FontWeight.SemiBold),
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

                // Punch Out
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.punchOutTime.isNotEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_up),
//                            contentDescription = stringResource(R.string.punch_out),
                            contentDescription = "Punch Out",
                            tint = colorResource(R.color.primary_main),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = log.punchOutTime,
                            style = Typography().base.copy(fontWeight = FontWeight.Bold),
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
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.content_fg_color)
                )
            }
        }
    )
}
/* Data class for logs */
data class AttendanceLog(
    val dayName: String,
    val dayNumber: String,
    val punchInTime: String,
    val punchOutTime: String,
    val workingHours: String,
    val lateDuration: String = "",
    val status: AttendanceLogStatus = AttendanceLogStatus.PRESENT
)

enum class AttendanceLogStatus {
    PRESENT,
    LATE_LESS_THAN_1H,
    LATE_MORE_THAN_1H,
    EARLY_PUNCH_OUT,
    ABSENCE
}
/*

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
            AbsherAppBar(
                showEventTheme = false,
//                title = "stringResource(R.string.attendance_dashboard)",
                title = "Time Attendance",
                generalIcon = { Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = null) }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = null,
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
//                    tabTitle = "stringResource(R.string.attendance_logs)",
                    tabTitle = "mark attendance",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
//                    tabTitle = "stringResource(R.string.permission_request)",
                    tabTitle = "permission request",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(1) }
                )
            }
        )

        16.0.MOIVerticalSpacer()

        // Table Header
        AttendanceTableHeader()

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
private fun AttendanceTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Data",
//            text = "stringResource(R.string.date)",
            modifier = Modifier.weight(1.2f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
            text = "Punch In",
//            text = "stringResource(R.string.punch_in)",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
            text = "Punch Out",
//            text = "stringResource(R.string.punch_out)",
            modifier = Modifier.weight(1f),
            style = Typography().small.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )
        Text(
//            text = "stringResource(R.string.working_hours)",
            text = "Working Hours",
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
        padding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        cardContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MOICard(
                    cornerSize = CardSize.LARGE,
                    modifier = Modifier.weight(1f),
                    padding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    cardColor = colorResource(R.color.green_main_400),
                    cardContent = {
                        Column(
                            Modifier.padding(10.dp)
                        ) {
                            Text(
                                text = log.dayName,
                                style = Typography().xSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                modifier = Modifier.padding(top = 4.dp).align(Alignment.CenterHorizontally),
                                text = log.dayNumber,
                                style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                )

                // Punch In Time
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.punchInTime.isNotEmpty()) {
                        SvgImageRender(
                            // TODO: Change Icons Because it's not available in sdk.
                            url = R.drawable.ic_arrow_down,   // ✅ FIXED
                            imageSize = 16.dp,
                            desc = "Punch In",
                            imageType = ImageType.LOCAL
                        )
                        Text(
                            text = log.punchInTime,
                            style = Typography().base.copy(fontWeight = FontWeight.SemiBold),
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
                            // TODO: Change Icons Because it's not available in sdk.
                            url = R.drawable.ic_arrow_up,   // ✅ FIXED
                            imageSize = 16.dp,
                            desc = "Punch Out",
                            imageType = ImageType.LOCAL
                        )
                        Text(
                            text = log.punchOutTime,
                            style = Typography().base.copy(
                                fontWeight = FontWeight.Bold
                            ),
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
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
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

    AttendanceLogItem(
        log = mockLogs[0],
        isDarkMode = isDarkMode
    )
}
*/
