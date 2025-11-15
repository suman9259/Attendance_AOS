package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import androidx.compose.foundation.layout.*
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
import sa.gov.moi.absherinterior.R
import sa.gov.moi.absherinterior.components.*
import sa.gov.moi.absherinterior.theme.base
import sa.gov.moi.absherinterior.theme.small
import sa.gov.moi.absherinterior.utils.*
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceDashboardViewModel
import sa.gov.moi.absherinterior.models.AppMessage


@Composable
fun AttendanceDashboardScreen(
    navManager: NavManager,
    viewModel: AttendanceDashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    MainScreenView(
        uiState = uiState.screenState,
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
//                title = stringResource(id = R.string.time_attendance), // "Time Attendance"
                title = "Time Attendance", // "Time Attendance"
                generalIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_as_admin),
//                        contentDescription = stringResource(id = R.string.time_attendance)
                        contentDescription = "time attendance"
                    )
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = uiState.errorMessage as? AppMessage,
        successComposable = {
            AttendanceDashboardContent(
                selectedTab = selectedTab,
                onTabChanged = viewModel::onTabChanged,
                attendanceData = uiState.attendanceData,
                selectedMonth = uiState.selectedMonth,
                onMonthChanged = viewModel::onMonthChanged,
                onPunchIn = viewModel::onPunchIn,
                onPunchOut = viewModel::onPunchOut,
                isDarkMode = isDarkMode,
                navManager = navManager
            )
        }
    )
}

@Composable
private fun AttendanceDashboardContent(
    selectedTab: Int,
    onTabChanged: (Int) -> Unit,
    attendanceData: AttendanceData?,
    selectedMonth: String,
    onMonthChanged: (String) -> Unit,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    isDarkMode: Boolean,
    navManager: NavManager?
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
                    tabTitle = "permission application",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(1) }
                )
            }
        )

        16.0.MOIVerticalSpacer()

        PunchInOutCard(
//            shiftTitle = stringResource(R.string.your_assigned_shift),
            shiftTitle = "Assign Shift",
            shiftName = attendanceData?.shiftName ?: "--",
            shiftTime = attendanceData?.shiftTime ?: "--",
            onPunchIn = onPunchIn,
            onPunchOut = onPunchOut
        )

        12.0.MOIVerticalSpacer()

        // Optional: summary / other dashboard items can go here (kept minimal per request)
    }
}

/* Data classes for Dashboard */
data class AttendanceData(
    val upcomingHoliday: String,
    val shiftName: String,
    val shiftTime: String,
    val summary: AttendanceSummary
)

data class AttendanceSummary(
    val attendance: Int,
    val lateLessThan1h: Int,
    val lateMoreThan1h: Int,
    val earlyPunchOut: Int,
    val absence: Int
)

/* Punch In / Out Card */
@Composable
fun PunchInOutCard(
    modifier: Modifier = Modifier,
    shiftTitle: String = "",
    shiftName: String = "",
    shiftTime: String = "",
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    isLoading: Boolean = false
) {
    MOICard(
        cornerSize = CardSize.LARGE,
        cardContent = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = shiftTitle,
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.content_fg_color)
                )

                4.0.MOIVerticalSpacer()

                Text(
                    text = shiftName,
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.dark_gray_100)
                )

                Text(
                    text = shiftTime,
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.primary_main)
                )

                8.0.MOIVerticalSpacer()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Punch In
                    MOICard(
                        modifier = Modifier.weight(1f),
                        cornerSize = CardSize.MEDIUM,
                        cardColor = colorResource(R.color.green_main),
                        onCardClicked = onPunchIn,
                        cardContent = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                CustomIconButton(
                                    icon = R.drawable.ic_exit_app,
                                    iconSize = 18,
                                    tint = Color.White,
                                    onClick = { /* kept intentionally empty - handled by card click */ },
                                    showContainer = false
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
//                                    text = stringResource(R.string.punch_in),
                                    text = "Punch In",
                                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    )

                    // Punch Out
                    MOICard(
                        modifier = Modifier.weight(1f),
                        cornerSize = CardSize.LARGE,
                        cardColor = colorResource(R.color.primary_main),
                        onCardClicked = onPunchOut,
                        cardContent = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                CustomIconButton(
                                    icon = R.drawable.ic_exit_app,
                                    iconSize = 18,
                                    tint = Color.White,
                                    onClick = { /* kept intentionally empty - handled by card click */ },
                                    showContainer = false
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
//                                    text = stringResource(R.string.punch_out),
                                    text = "Punch Out",
                                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    )
                }
            }
        }
    )
}

/*
@Composable
fun AttendanceDashboardScreen(
    navController: NavController,
    viewModel: AttendanceDashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    MainScreenView(
        uiState = uiState.screenState,
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
//                title = "stringResource(R.string.attendance_dashboard)",
                title = "Time Attendance",
                generalIcon = { Icon(painter = painterResource(R.drawable.ic_as_admin), contentDescription = null) }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = uiState.errorMessage as AppMessage?,
        successComposable = {
//            AttendanceDashboardContent(
//                selectedTab = selectedTab,
//                onTabChanged = viewModel::onTabChanged,
//                attendanceData = uiState.attendanceData,
//                selectedMonth = uiState.selectedMonth,
//                onMonthChanged = viewModel::onMonthChanged,
//                onPunchIn = viewModel::onPunchIn,
//                onPunchOut = viewModel::onPunchOut,
//                isDarkMode = isDarkMode,
//                navController = navController
//            )
        }
    )
}

@Composable
private fun AttendanceDashboardContent(
//    selectedTab: Int,
//    onTabChanged: (Int) -> Unit,
//    attendanceData: AttendanceData?,
//    selectedMonth: String,
//    onMonthChanged: (String) -> Unit,
//    onPunchIn: () -> Unit,
//    onPunchOut: () -> Unit,
//    isDarkMode: Boolean,
//    navController: NavController?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        CustomTabRow(
            selectedTabIndex = 0,
            tabType = TabType.OUTLINED,
            backgroundColor = Color.Transparent,
            indicatorColor = colorResource(R.color.primary_main),
            tabs = {
                CustomIndicatorTab(
                    isSelected = true,
//                    tabTitle = "stringResource(R.string.mark_attendance)",
                    tabTitle = "Mark Attendance",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = {}
                )
                CustomIndicatorTab(
                    isSelected = false,
//                    tabTitle = "stringResource(R.string.permission_application)",
                    tabTitle = "Permission Application",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = {  }
                )
            }
        )

        16.0.MOIVerticalSpacer()
        PunchInOutCard(
            onPunchIn = {},
            onPunchOut = {}
        )

    }
}

// Data classes
data class AttendanceData(
    val upcomingHoliday: String,
    val shiftName: String,
    val shiftTime: String,
    val summary: AttendanceSummary
)

data class AttendanceSummary(
    val attendance: Int,
    val lateLessThan1h: Int,
    val lateMoreThan1h: Int,
    val earlyPunchOut: Int,
    val absence: Int
)


// ========================================
// 1. ATTENDANCE DASHBOARD PREVIEWS
// ========================================

@Preview(
    name = "Dashboard - Light Mode",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewAttendanceDashboardLight() {
    AbsherInteriorTheme(){
        AttendanceDashboardPreview(isDarkMode = false)
    }
}


@Composable
private fun AttendanceDashboardPreview(isDarkMode: Boolean) {
    var selectedTab by remember { mutableStateOf(0) }

    val mockAttendanceData = AttendanceData(
        upcomingHoliday = "OCT 12",
        shiftName = "standard Shift",
        shiftTime = "07:00 AM - 12:00 PM",
        summary = AttendanceSummary(
            attendance = 16,
            lateLessThan1h = 8,
            lateMoreThan1h = 4,
            earlyPunchOut = 6,
            absence = 2
        )
    )

    MainScreenView(
        uiState = ScreenState.Success(data = "Success"),
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
//                title = "stringResource(R.string.attendance_dashboard)",
                title = "Time Attendance",
                generalIcon = {
                    Icon(painter = painterResource(R.drawable.ic_as_admin),
                        contentDescription = null
                    )
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = null,
        successComposable = {
            AttendanceDashboardContent(
//                selectedTab = selectedTab,
//                onTabChanged = viewModel::onTabChanged,
//                attendanceData = uiState.attendanceData,
//                selectedMonth = uiState.selectedMonth,
//                onMonthChanged = viewModel::onMonthChanged,
//                onPunchIn = viewModel::onPunchIn,
//                onPunchOut = viewModel::onPunchOut,
//                isDarkMode = isDarkMode,
//                navController = navController
            )
        }
    )

}


@Composable
fun PunchInOutCard(
    modifier: Modifier = Modifier,
    shiftTitle: String = "your Assigned Shift",
    shiftName: String = "standard Shift",
    shiftTime: String = "07:00 AM - 12:00 PM",
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    isLoading: Boolean = false
) {

    MOICard(
            cornerSize = CardSize.LARGE,
            cardContent = {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    // Title - "your Assigned Shift"
                    Text(
                        text = shiftTitle,
                        style = Typography().base.copy(fontWeight = FontWeight.Bold),
                        color = colorResource(R.color.content_fg_color)
                    )

                    1.0.MOIVerticalSpacer()

                    // Shift Name - "standard Shift"
                    Text(
                        text = shiftName,
                        style = Typography().base.copy(fontWeight = FontWeight.Bold),
                        color = colorResource(R.color.dark_gray_100)
                    )

                    // Shift Time - "07:00 AM - 12:00 PM" (Gold color)
                    Text(
                        text = shiftTime,
                        style = Typography().base.copy(fontWeight = FontWeight.Bold),
                        color = colorResource(R.color.primary_main)
                    )

//                    12.0.MOIVerticalSpacer()

                    // Buttons Row

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Punch In Card
                        MOICard(
                            modifier = Modifier.weight(1f),
                            cornerSize = CardSize.MEDIUM,
                            cardColor = colorResource(R.color.green_main),
                            onCardClicked = onPunchIn,
                            cardContent = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp, horizontal = 2.dp)
                                ) {
                                    CustomIconButton(
                                        icon = R.drawable.ic_exit_app,
                                        iconSize = 18,
                                        tint = Color.White,
                                        onClick = { },
                                        showContainer = false
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "punch in",
                                        style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        )

                        // Punch Out Card
                        MOICard(
                            modifier = Modifier.weight(1f),
                            cornerSize = CardSize.LARGE,
                            cardColor = colorResource(R.color.primary_main),
                            cardContent = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp, horizontal = 2.dp)
                                ) {
                                    CustomIconButton(
                                        icon = R.drawable.ic_exit_app,
                                        // TODO: Change Image Icon because same is not available in sdk.
                                        iconSize = 18,
                                        tint = Color.White,
                                        onClick = { },
                                        showContainer = false
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "punch out",
                                        style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        )
                    }
//
                }
            }
    )
}



*/
