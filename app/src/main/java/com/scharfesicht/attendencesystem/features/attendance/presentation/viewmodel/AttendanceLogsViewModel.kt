package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.utils.ScreenState
import javax.inject.Inject

@HiltViewModel
class AttendanceLogsViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    private val _selectedAttendanceType = MutableStateFlow("Attendance")
    val selectedAttendanceType: StateFlow<String> = _selectedAttendanceType

    private val _selectedMonth = MutableStateFlow("April")
    val selectedMonth: StateFlow<String> = _selectedMonth

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    init {
        loadUserTheme()
        loadAttendanceLogs()
    }

    private fun loadUserTheme() {
        viewModelScope.launch {
            MiniAppEntryPoint.superData?.getCurrentTheme()?.data?.let { theme ->
                _isDarkMode.value = theme == "dark"
            }
        }
    }

    private fun loadAttendanceLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(screenState = ScreenState.Loading)

            try {
                val logs = repository.getAttendanceLogs(
                    month = _selectedMonth.value,
                    type = _selectedAttendanceType.value
                )
                _uiState.value = _uiState.value.copy(
                    screenState = ScreenState.Success(logs),
                    attendanceLogs = logs
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = ScreenState.Error(e.message ?: "Unknown error"),
                    errorMessage = e.message ?: "Failed to load logs"
                )
            }
        }
    }

    fun onTabChanged(tab: Int) {
        _selectedTab.value = tab
    }

    fun onAttendanceTypeChanged(type: String) {
        _selectedAttendanceType.value = type
        loadAttendanceLogs()
    }

    fun onMonthChanged(month: String) {
        _selectedMonth.value = month
        loadAttendanceLogs()
    }
}

data class LogsUiState(
    val screenState: ScreenState? = null,
    val attendanceLogs: List<AttendanceLog>? = null,
    val errorMessage: String? = null
)