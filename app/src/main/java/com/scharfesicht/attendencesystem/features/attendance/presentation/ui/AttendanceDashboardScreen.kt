package com.scharfesicht.attendencesystem.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import sa.gov.moi.absherinterior.R
import sa.gov.moi.absherinterior.components.*
import sa.gov.moi.absherinterior.theme.base
import sa.gov.moi.absherinterior.theme.small
import sa.gov.moi.absherinterior.theme.xSmall
import sa.gov.moi.absherinterior.utils.*
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceDashboardViewModel
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.theme.AbsherInteriorTheme

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
                            onClick = { /* Open menu */ },
                            tint = colorResource(R.color.header_fg_color)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "stringResource(R.string.time_attendance)",
                                style = Typography().base.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.header_fg_color)
                                )
                            )
                            CustomIconButton(
                                icon = R.drawable.ic_arrow_right,
                                onClick = { navController.navigateUp() },
                                tint = colorResource(R.color.header_fg_color),
                                iconSize = 18
                            )
                        }
                    }
                },
                centerLogo = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        SvgImageRender(
                            url = "R.drawable.ic_moi_logo",
                            imageSize = 64.dp,
                            desc = "MOI Logo",
                            imageType = ImageType.LOCAL
                        )
                    }
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = uiState.errorMessage as AppMessage?,
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
                navController = navController
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
    navController: NavController?
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
                    tabTitle = "stringResource(R.string.mark_attendance)",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
                    tabTitle = "stringResource(R.string.permission_application)",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.dark_gray_400),
                    onTabClick = { onTabChanged(1) }
                )
            }
        )

        16.0.MOIVerticalSpacer()

        when (selectedTab) {
            0 -> navController?.let {
                MarkAttendanceTab(
                    attendanceData = attendanceData,
                    selectedMonth = selectedMonth,
                    onMonthChanged = onMonthChanged,
                    onPunchIn = onPunchIn,
                    onPunchOut = onPunchOut,
                    isDarkMode = isDarkMode,
                    navController = it
                )
            }
            1 -> PermissionApplicationTab()
        }
    }
}

@Composable
private fun MarkAttendanceTab(
    attendanceData: AttendanceData?,
    selectedMonth: String,
    onMonthChanged: (String) -> Unit,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    isDarkMode: Boolean,
    navController: NavController
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Holiday Info Section
        item {
            ExpandableHolidayCard(
                holidayDate = attendanceData?.upcomingHoliday ?: "OCT 12"
            )
        }

        // Shift Info Section
        item {
            ShiftInfoSection(
                shiftName = attendanceData?.shiftName ?: "standard Shift",
                shiftTime = attendanceData?.shiftTime ?: "07:00 AM - 12:00 PM",
                onPunchIn = onPunchIn,
                onPunchOut = onPunchOut
            )
        }

        // Attendance Summary
        item {
            AttendanceSummaryCard(
                selectedMonth = selectedMonth,
                onMonthChanged = onMonthChanged,
                attendanceSummary = attendanceData?.summary,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun ExpandableHolidayCard(holidayDate: String) {
    var isExpanded by remember { mutableStateOf(false) }

    MOICard(
        cornerSize = CardSize.MEDIUM,
        cardColor = colorResource(R.color.card_bg_color),
        padding = PaddingValues(AppPadding.MEDIUM.padding()),
        onCardClicked = { isExpanded = !isExpanded },
        cardContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "stringResource(R.string.the_coming_holiday)",
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.content_fg_color)
                )
                CustomIconButton(
                    icon = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down,
                    onClick = { isExpanded = !isExpanded },
                    iconSize = 20,
                    tint = colorResource(R.color.content_fg_color)
                )
            }

            if (isExpanded) {
                16.0.MOIVerticalSpacer()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = holidayDate.split(" ")[0],
                        style = Typography().base.copy(fontWeight = FontWeight.Bold),
                        color = colorResource(R.color.content_fg_color)
                    )
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(30.dp)
                            .background(colorResource(R.color.primary_main))
                    )
                    Text(
                        text = holidayDate.split(" ")[1],
                        style = Typography().base.copy(fontWeight = FontWeight.Bold),
                        color = colorResource(R.color.content_fg_color)
                    )
                }
            }
        }
    )
}

@Composable
private fun ShiftInfoSection(
    shiftName: String,
    shiftTime: String,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "stringResource(R.string.your_assigned_shift)",
            style = Typography().base.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.content_fg_color)
        )

        Text(
            text = shiftName,
            style = Typography().small,
            color = colorResource(R.color.dark_gray_400)
        )

        Text(
            text = shiftTime,
            style = Typography().base.copy(fontWeight = FontWeight.Bold),
            color = colorResource(R.color.primary_main)
        )

        16.0.MOIVerticalSpacer()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Punch In Button
            CustomButton(
                modifier = Modifier.weight(1f),
                buttonTitle = "stringResource(R.string.punch_in)",
                titleStyle = Typography().base,
                titleColor = Color.White,
//                buttonIcon = R.drawable.logo_primary,
                buttonIcon = R.drawable.profile_off,
                iconTint = Color.White,
                buttonSize = ButtonSize.LARGE,
                buttonType = ButtonType.SOLID,
                buttonStyle = ButtonStyle.GREEN,
                onClick = onPunchIn
            )

            // Punch Out Button
            CustomButton(
                modifier = Modifier.weight(1f),
                buttonTitle = "stringResource(R.string.punch_out)",
                titleStyle = Typography().base,
                titleColor = Color.White,
                buttonIcon = R.drawable.ic_exit_app,
                iconTint = Color.White,
                buttonSize = ButtonSize.LARGE,
                buttonType = ButtonType.SOLID,
                buttonStyle = ButtonStyle.PRIMARY,
                onClick = onPunchOut
            )
        }
    }
}

@Composable
private fun AttendanceSummaryCard(
    selectedMonth: String,
    onMonthChanged: (String) -> Unit,
    attendanceSummary: AttendanceSummary?,
    isDarkMode: Boolean
) {
    MOICard(
        cornerSize = CardSize.MEDIUM,
        cardColor = colorResource(R.color.card_bg_color),
        padding = PaddingValues(AppPadding.MEDIUM.padding()),
        cardContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomIconButton(
                            icon = R.drawable.ic_menu,
                            onClick = { },
                            iconSize = 18,
                            tint = colorResource(R.color.content_fg_color)
                        )
                        Text(
                            text = "stringResource(R.string.attendance_summary)",
                            style = Typography().base.copy(fontWeight = FontWeight.Bold),
                            color = colorResource(R.color.content_fg_color)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Month Dropdown
                        LabeledDropdown(
                            modifier = Modifier.width(120.dp),
                            items = listOf("January", "February", "March", "April", "May"),
                            hint = "stringResource(R.string.this_month)",
                            selectedOption = selectedMonth,
                            onSelection = { item, _ -> onMonthChanged(item) }
                        )

                        // View Type Dropdown
                        LabeledDropdown(
                            modifier = Modifier.width(100.dp),
                            items = listOf("Days", "Hours"),
                            hint = "Days",
                            selectedOption = "Days",
                            onSelection = { _, _ -> }
                        )
                    }
                }

                // Chart
                AttendanceChart(
                    summary = attendanceSummary,
                    isDarkMode = isDarkMode
                )
            }
        }
    )
}

@Composable
private fun AttendanceChart(
    summary: AttendanceSummary?,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChartLegendItem(
                color = colorResource(R.color.background),
                label = "stringResource(R.string.attendance)"
            )
            ChartLegendItem(
                color = colorResource(R.color.card_bg_color_with_opacity),
                label = "stringResource(R.string.late_less_than_1h)"
            )
            ChartLegendItem(
                color = colorResource(R.color.danger_main),
                label = "stringResource(R.string.late_more_than_1h)"
            )
            ChartLegendItem(
                color = colorResource(R.color.main_info_color),
                label = "stringResource(R.string.early_punch_out)"
            )
            ChartLegendItem(
                color = colorResource(R.color.dark_gray_100),
                label = "stringResource(R.string.absence)"
            )
        }

        // Bar Chart (Simplified representation)
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("16h", style = Typography().xSmall, color = colorResource(R.color.primary_main))
                Text("12h", style = Typography().xSmall, color = colorResource(R.color.primary_main))
                Text("8h", style = Typography().xSmall, color = colorResource(R.color.primary_main))
                Text("4h", style = Typography().xSmall, color = colorResource(R.color.primary_main))
                Text("0h", style = Typography().xSmall, color = colorResource(R.color.primary_main))
            }

            // Bars (simplified)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // This would be replaced with actual chart library
                repeat(5) { index ->
                    Column(
                        modifier = Modifier
                            .width(30.dp)
                            .fillMaxHeight(0.6f + (index * 0.05f)),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .weight(1f)
                                .background(
                                    when (index % 5) {
                                        0 -> colorResource(R.color.primary_100)
                                        1 -> colorResource(R.color.read_bg_color)
                                        2 -> colorResource(R.color.danger_main)
                                        3 -> colorResource(R.color.main_info_color)
                                        else -> colorResource(R.color.dark_gray_100)
                                    }
                                )
                        )
                    }
                }
            }

            // X-axis label
            Text(
                text = "April",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 8.dp),
                style = Typography().small.copy(fontWeight = FontWeight.Bold),
                color = colorResource(R.color.content_fg_color)
            )
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Text(
            text = label,
            style = Typography().xSmall,
            color = colorResource(R.color.content_fg_color)
        )
    }
}

@Composable
private fun PermissionApplicationTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Permission Application",
            style = Typography().base,
            color = colorResource(R.color.content_fg_color)
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

@Preview(
    name = "Dashboard - Dark Mode",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewAttendanceDashboardDark() {
    AbsherInteriorTheme() {
        AttendanceDashboardPreview(isDarkMode = true)
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Mock AppBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(colorResource(R.color.header_bg_color))
        )

        AttendanceDashboardContent(
            selectedTab = selectedTab,
            onTabChanged = { selectedTab = it },
            attendanceData = mockAttendanceData,
            selectedMonth = "April",
            onMonthChanged = {},
            onPunchIn = {},
            onPunchOut = {},
            isDarkMode = isDarkMode,
            navController = null // Not needed for preview
        )
    }
}