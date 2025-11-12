package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class AttendanceDashboardUiState {
    object Loading : AttendanceDashboardUiState()
    data class Success(
        val holiday: Holiday?,
        val shift: Shift?,
        val todayAttendance: AttendanceRecord?,
        val summaries: List<AttendanceSummary>,
        val stats: AttendanceStats?,
        val selectedTab: AttendanceTab = AttendanceTab.MARK_ATTENDANCE,
        val selectedPeriod: String = "this month",
        val selectedView: String = "Days"
    ) : AttendanceDashboardUiState()
    data class Error(val message: String) : AttendanceDashboardUiState()
}

enum class AttendanceTab {
    MARK_ATTENDANCE,
    PERMISSION_APPLICATION
}

@HiltViewModel
class AttendanceDashboardViewModel @Inject constructor(
    private val getUpcomingHolidayUseCase: GetUpcomingHolidayUseCase,
    private val getAssignedShiftUseCase: GetAssignedShiftUseCase,
    private val markAttendanceUseCase: MarkAttendanceUseCase,
    private val getAttendanceSummaryUseCase: GetAttendanceSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttendanceDashboardUiState>(
        AttendanceDashboardUiState.Loading
    )
    val uiState: StateFlow<AttendanceDashboardUiState> = _uiState.asStateFlow()

    private val _punchInOutLoading = MutableStateFlow(false)
    val punchInOutLoading: StateFlow<Boolean> = _punchInOutLoading.asStateFlow()

    private val _showSuccessDialog = MutableStateFlow<String?>(null)
    val showSuccessDialog: StateFlow<String?> = _showSuccessDialog.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = AttendanceDashboardUiState.Loading

            combine(
                getUpcomingHolidayUseCase(),
                getAssignedShiftUseCase(),
                getTodayAttendanceUseCase(),
                getAttendanceSummaryUseCase("April")
            ) { holiday, shift, attendance, summaries ->
                Quintuple(holiday, shift, attendance, summaries, null)
            }.collect { (holidayResult, shiftResult, attendanceResult, summariesResult, _) ->
                _uiState.value = AttendanceDashboardUiState.Success(
                    holiday = holidayResult.getOrNull(),
                    shift = shiftResult.getOrNull(),
                    todayAttendance = attendanceResult.getOrNull(),
                    summaries = summariesResult.getOrNull() ?: emptyList(),
                    stats = null
                )
            }
        }
    }

    fun selectTab(tab: AttendanceTab) {
        val currentState = _uiState.value
        if (currentState is AttendanceDashboardUiState.Success) {
            _uiState.value = currentState.copy(selectedTab = tab)
        }
    }

    fun punchIn() {
        viewModelScope.launch {
            _punchInOutLoading.value = true
            markAttendanceUseCase.punchIn().fold(
                onSuccess = {
                    _showSuccessDialog.value = "Punched In Successfully"
                    loadDashboardData()
                },
                onFailure = {
                    // Handle error
                }
            )
            _punchInOutLoading.value = false
        }
    }

    fun punchOut() {
        viewModelScope.launch {
            _punchInOutLoading.value = true
            markAttendanceUseCase.punchOut().fold(
                onSuccess = {
                    _showSuccessDialog.value = "Punched Out Successfully"
                    loadDashboardData()
                },
                onFailure = {
                    // Handle error
                }
            )
            _punchInOutLoading.value = false
        }
    }

    fun dismissSuccessDialog() {
        _showSuccessDialog.value = null
    }

    fun changePeriod(period: String) {
        val currentState = _uiState.value
        if (currentState is AttendanceDashboardUiState.Success) {
            _uiState.value = currentState.copy(selectedPeriod = period)
        }
    }

    fun changeView(view: String) {
        val currentState = _uiState.value
        if (currentState is AttendanceDashboardUiState.Success) {
            _uiState.value = currentState.copy(selectedView = view)
        }
    }

    // Helper method to get today's attendance
    private fun getTodayAttendanceUseCase() = flow {
        emit(Result.success<AttendanceRecord?>(null))
    }
}

// Helper class for combining 5 flows
private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)