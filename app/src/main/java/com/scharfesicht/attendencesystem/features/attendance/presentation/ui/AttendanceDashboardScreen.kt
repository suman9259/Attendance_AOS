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
import com.scharfesicht.attendencesystem.app.navigation.ScreenRoutes
import com.scharfesicht.attendencesystem.features.attendance.domain.model.Shift
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
        message = uiState.errorMessage?.let {
            AppMessage.Error(
                message = it.message,
                messageKey = "error_message"
            )
        },
        successComposable = {
            AttendanceDashboardContent(
                currentShift = uiState.currentShift,
                selectedTab = selectedTab,
                onTabChanged = viewModel::onTabChanged,
//                attendanceData = uiState.attendanceData,
//                selectedMonth = uiState.selectedMonth,
//                onMonthChanged = viewModel::onMonthChanged,
                onPunchIn = {
                /*viewModel::onPunchIn*/
                    navManager.navigate(ScreenRoutes.FaceRecognition.route)
                },
                onPunchOut = viewModel::onPunchOut,
                navManager = navManager
            )
        }
    )
}

@Composable
private fun AttendanceDashboardContent(
    currentShift: Shift? = null,
    selectedTab: Int,
    onTabChanged: (Int) -> Unit,
//    attendanceData: AttendanceData?,
//    selectedMonth: String,
//    onMonthChanged: (String) -> Unit,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
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
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
//                    tabTitle = stringResource(R.string.permission_application),
                    tabTitle = "permission application",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = { onTabChanged(1) }
                )
            }
        )

        2.0.MOIVerticalSpacer()

        PunchInOutCard(
//            shiftTitle = stringResource(R.string.your_assigned_shift),
            shiftTitle = "Assign Shift",
//            shiftName = currentShift?.shiftName ?: "--",
            currentShift = currentShift,
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
    currentShift: Shift?,
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
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = shiftTitle,
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.content_fg_color)
                )

                4.0.MOIVerticalSpacer()

                Text(
//                    text = shiftName,
                    text = "Standard Shift",
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.dark_gray_100)
                )

                Text(
                    text = "${currentShift?.shift_rule?.get(0)?.start_time ?: "--"} - ${currentShift?.shift_rule?.get(0)?.end_time ?: "--"}",
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
