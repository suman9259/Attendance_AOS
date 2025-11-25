package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceRecord
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.GetLatestRecordUseCase
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceLog
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceLogStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.utils.ScreenState
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class AttendanceLogsViewModel @Inject constructor(
    private val getLatestRecordUseCase: GetLatestRecordUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "AttendanceLogsViewModel"

        private val DATE_TIME_FORMATTER =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        private val DAY_NAME_FORMATTER =
            SimpleDateFormat("EEE", Locale.getDefault())
        private val DAY_NUMBER_FORMATTER =
            SimpleDateFormat("dd", Locale.getDefault())
        private val TIME_FORMATTER =
            SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadUserTheme()
        loadAttendanceLogs()
    }

    // ----------------------------------------------------
    // LOAD THEME
    // ----------------------------------------------------
    private fun loadUserTheme() {
        viewModelScope.launch {
            runCatching {
                MiniAppEntryPoint.superData?.getCurrentTheme()?.data?.let { theme ->
                    _isDarkMode.value = theme == "dark"
                }
            }.onFailure {
                Log.e(TAG, "Theme load failed", it)
            }
        }
    }

    // ----------------------------------------------------
    // MAIN: LOAD ATTENDANCE LOGS WITH SHIMMER
    // ----------------------------------------------------
    fun loadAttendanceLogs() {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {

            // This allows shimmer to show
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                screenState = ScreenState.Success(emptyList<AttendanceLog>()),
                errorMessage = null
            )

            getLatestRecordUseCase()
                .catch { e ->
                    Log.e(TAG, "Flow exception", e)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        screenState = ScreenState.Error("Something went wrong"),
                        errorMessage = AppMessage.Error(
                            message = e.message ?: "Unknown error",
                            messageKey = "error_message"
                        )
                    )
                }
                .collect { result ->
                    when (result) {

                        is NetworkResult.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                screenState = ScreenState.Success(emptyList<AttendanceLog>())
                            )
                        }

                        is NetworkResult.Success -> {
                            val logs = result.data.map { convertRecordToLogSafe(it) }

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                attendanceLogs = logs,
                                screenState = ScreenState.Success(logs),
                                errorMessage = null
                            )
                        }

                        is NetworkResult.Error -> {
                            val msg = result.error.msg

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                screenState = ScreenState.Error(msg),
                                errorMessage = AppMessage.Error(
                                    message = msg,
                                    messageKey = "error_message"
                                )
                            )
                        }
                    }
                }
        }
    }

    // ----------------------------------------------------
    // SAFE CONVERSION
    // ----------------------------------------------------
    private fun convertRecordToLogSafe(record: AttendanceRecord): AttendanceLog =
        try {
            convertRecordToLog(record)
        } catch (e: Exception) {
            Log.e(TAG, "convertRecordToLog crashed", e)
            AttendanceLog(
                dayName = "--",
                dayNumber = "--",
                punchInTime = "--",
                punchOutTime = "--",
                workingHours = "--",
                status = AttendanceLogStatus.PRESENT,
                uuid = record.uuid,
                checkinMediaUrl = record.checkin_media_url ?: "",
                checkoutMediaUrl = record.checkout_media_url ?: "",
                shiftName = record.shift?.shift_name_lang ?: "N/A"
            )
        }

    private fun convertRecordToLog(record: AttendanceRecord): AttendanceLog {
        val checkinDate = parseDate(record.checkin_time)
        val checkoutDate = parseDate(record.checkout_time)

        val dayName = formatDayName(checkinDate)
        val dayNumber = formatDayNumber(checkinDate)

        val punchInTime = formatTime(checkinDate)
        val punchOutTime = checkoutDate?.let { formatTime(it) } ?: ""

        val workingHours = if (checkoutDate != null) {
            calculateWorkingHours(checkinDate, checkoutDate)
        } else ""

        return AttendanceLog(
            dayName = dayName,
            dayNumber = dayNumber,
            punchInTime = punchInTime,
            punchOutTime = punchOutTime,
            workingHours = workingHours,
            status = determineAttendanceStatus(record),
            uuid = record.uuid,
            checkinMediaUrl = record.checkin_media_url ?: "",
            checkoutMediaUrl = record.checkout_media_url ?: "",
            shiftName = record.shift?.shift_name_lang ?: "N/A"
        )
    }

    // ----------------------------------------------------
    // DATE UTILITIES
    // ----------------------------------------------------
    private fun parseDate(date: String?): Date {
        if (date.isNullOrBlank()) return Date()
        return runCatching { DATE_TIME_FORMATTER.parse(date) }.getOrNull() ?: Date()
    }

    private fun formatDayName(date: Date): String =
        runCatching { DAY_NAME_FORMATTER.format(date) }.getOrElse { "--" }

    private fun formatDayNumber(date: Date): String =
        runCatching { DAY_NUMBER_FORMATTER.format(date) }.getOrElse { "--" }

    private fun formatTime(date: Date): String =
        runCatching { TIME_FORMATTER.format(date) }.getOrElse { "--" }

    private fun calculateWorkingHours(checkin: Date, checkout: Date): String {
        val diff = checkout.time - checkin.time
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        return String.format("%02dh %02dm", hours, abs(minutes))
    }

    private fun determineAttendanceStatus(record: AttendanceRecord): AttendanceLogStatus {
        return AttendanceLogStatus.PRESENT
    }

    // ----------------------------------------------------
    // UI ACTIONS
    // ----------------------------------------------------
    fun onTabChanged(tab: Int) {
        _selectedTab.value = tab
    }

    fun refresh() = loadAttendanceLogs()
}

data class LogsUiState(
    val screenState: ScreenState? = null,
    val attendanceLogs: List<AttendanceLog>? = null,
    val isLoading: Boolean = false,
    val errorMessage: AppMessage? = null
)
