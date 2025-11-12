package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scharfesicht.attendencesystem.app.ui.componants.MainAppTopAppBar
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.components.*
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDashboardScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AttendanceDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val punchInOutLoading by viewModel.punchInOutLoading.collectAsState()
    val showSuccessDialog by viewModel.showSuccessDialog.collectAsState()

    // Show success dialog
    showSuccessDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = { Text("Success") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSuccessDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MainAppTopAppBar(
                title = "Time Attendance",
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is AttendanceDashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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
                    onPeriodChange = { viewModel.changePeriod(it) },
                    onViewChange = { viewModel.changeView(it) }
                )
            }

            is AttendanceDashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Attendance",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    "Time Attendance",
                    color = MaterialTheme.colorScheme.onPrimary
                )
                IconButton(onClick = { /* Navigate to time attendance details */ }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
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
    onPeriodChange: (String) -> Unit,
    onViewChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        AttendanceTabRow(
            selectedTab = state.selectedTab,
            onTabSelected = onTabSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected tab
        when (state.selectedTab) {
            AttendanceTab.MARK_ATTENDANCE -> {
                MarkAttendanceContent(
                    holiday = state.holiday,
                    shift = state.shift,
                    todayAttendance = state.todayAttendance,
                    summaries = state.summaries,
                    onPunchIn = onPunchIn,
                    onPunchOut = onPunchOut,
                    loading = punchInOutLoading,
                    selectedPeriod = state.selectedPeriod,
                    selectedView = state.selectedView,
                    onPeriodChange = onPeriodChange,
                    onViewChange = onViewChange
                )
            }

            AttendanceTab.PERMISSION_APPLICATION -> {
                PermissionApplicationContent()
            }
        }
    }
}

@Composable
fun MarkAttendanceContent(
    holiday: com.scharfesicht.attendencesystem.features.attendance.domain.model.Holiday?,
    shift: com.scharfesicht.attendencesystem.features.attendance.domain.model.Shift?,
    todayAttendance: com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceRecord?,
    summaries: List<com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceSummary>,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    loading: Boolean,
    selectedPeriod: String,
    selectedView: String,
    onPeriodChange: (String) -> Unit,
    onViewChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Holiday Card
        holiday?.let {
            HolidayCard(holiday = it)
        }

        // Shift Card
        shift?.let {
            ShiftCard(
                shift = it,
                todayAttendance = todayAttendance,
                onPunchIn = onPunchIn,
                onPunchOut = onPunchOut,
                loading = loading
            )
        }

        // Attendance Summary Chart
        AttendanceSummaryChart(
            summaries = summaries,
            selectedPeriod = selectedPeriod,
            selectedView = selectedView,
            onPeriodChange = onPeriodChange,
            onViewChange = onViewChange
        )
    }
}

@Composable
fun PermissionApplicationContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Permission Application Content",
            style = MaterialTheme.typography.titleLarge
        )
    }
}