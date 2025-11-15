package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.core.network.TokenManager
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.*
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceData
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.utils.ScreenState
import javax.inject.Inject

@HiltViewModel
class AttendanceDashboardViewModel @Inject constructor(
    private val checkInUseCase: CheckInUseCase,
    private val checkOutUseCase: CheckOutUseCase,
    private val getLatestRecordUseCase: GetLatestRecordUseCase,
    private val getUserShiftsUseCase: GetUserShiftsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private const val TAG = "AttendanceDashboard"
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedMonth = MutableStateFlow("April")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        loadUserTheme()
        loadUserShifts()
        loadLatestRecord()
    }

    private fun loadUserTheme() {
        viewModelScope.launch {
            MiniAppEntryPoint.superData?.getCurrentTheme()?.data?.let { theme ->
                _isDarkMode.value = theme == "dark"
            }
        }
    }

    private fun loadUserShifts() {
        viewModelScope.launch {
            getUserShiftsUseCase().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            screenState = ScreenState.Loading,
                            isLoading = true
                        )
                    }
                    is NetworkResult.Success -> {
                        val shifts = result.data
                        val currentShift = shifts.firstOrNull()

                        // Create AttendanceData from shift
                        val attendanceData = currentShift?.let { shift ->
                            val shiftRule = shift.shift_rule.firstOrNull()
                            AttendanceData(
                                upcomingHoliday = "OCT 12", // Mock data
                                shiftName = shift.shift_name_lang,
                                shiftTime = shiftRule?.let {
                                    "${it.start_time} - ${it.end_time}"
                                } ?: "--",
                                summary = AttendanceSummary(
                                    attendance = 0,
                                    lateLessThan1h = 0,
                                    lateMoreThan1h = 0,
                                    earlyPunchOut = 0,
                                    absence = 0
                                )
                            )
                        }

                        _uiState.value = _uiState.value.copy(
                            screenState = ScreenState.Success(shifts),
                            shifts = shifts,
                            currentShift = currentShift,
                            attendanceData = attendanceData,
                            isLoading = false
                        )
                        Log.d(TAG, "Loaded ${shifts.size} shifts")
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            screenState = ScreenState.Error(result.error.message),
                            errorMessage = result.error.message,
                            isLoading = false
                        )
                        Log.e(TAG, "Error loading shifts: ${result.error.message}")
                    }
                }
            }
        }
    }

    private fun loadLatestRecord() {
        viewModelScope.launch {
            getLatestRecordUseCase().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        // Keep current state
                    }
                    is NetworkResult.Success -> {
                        val latestRecord = result.data.firstOrNull()
                        _uiState.value = _uiState.value.copy(
                            latestRecord = latestRecord,
                            isCheckedIn = latestRecord?.checkout_time == null
                        )
                        Log.d(TAG, "Latest record loaded, checked in: ${_uiState.value.isCheckedIn}")
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error loading latest record: ${result.error.message}")
                    }
                }
            }
        }
    }

    fun onTabChanged(tab: Int) {
        _selectedTab.value = tab
    }

    fun onMonthChanged(month: String) {
        _selectedMonth.value = month
    }

    fun onPunchIn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get location from Absher
                val location = MiniAppEntryPoint.superData?.getLocation()?.data
                if (location == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to get location",
                        showErrorToast = true
                    )
                    return@launch
                }

                // Trigger biometric
                val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                if (bioSuccess != true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Biometric authentication failed",
                        showErrorToast = true
                    )
                    return@launch
                }

                // Call check-in API
                checkInUseCase(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    shiftIndex = 0
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            // Already showing loading
                        }
                        is NetworkResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Punch in successful",
                                showSuccessToast = true,
                                latestRecord = result.data,
                                isCheckedIn = true
                            )
                            Log.d(TAG, "Check-in successful: ${result.data.uuid}")
                        }
                        is NetworkResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.error.message,
                                showErrorToast = true
                            )
                            Log.e(TAG, "Check-in failed: ${result.error.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to punch in",
                    showErrorToast = true
                )
                Log.e(TAG, "Punch in error", e)
            }
        }
    }

    fun onPunchOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get location
                val location = MiniAppEntryPoint.superData?.getLocation()?.data
                if (location == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to get location",
                        showErrorToast = true
                    )
                    return@launch
                }

                // Trigger biometric
                val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                if (bioSuccess != true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Biometric authentication failed",
                        showErrorToast = true
                    )
                    return@launch
                }

                // Call check-out API
                checkOutUseCase(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    shiftIndex = 0
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            // Already showing loading
                        }
                        is NetworkResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Punch out successful",
                                showSuccessToast = true,
                                latestRecord = result.data,
                                isCheckedIn = false
                            )
                            Log.d(TAG, "Check-out successful: ${result.data.uuid}")
                        }
                        is NetworkResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.error.message,
                                showErrorToast = true
                            )
                            Log.e(TAG, "Check-out failed: ${result.error.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to punch out",
                    showErrorToast = true
                )
                Log.e(TAG, "Punch out error", e)
            }
        }
    }

    fun onToastShown() {
        _uiState.value = _uiState.value.copy(
            showSuccessToast = false,
            showErrorToast = false
        )
    }

    fun onLogout() {
        viewModelScope.launch {
            logoutUseCase().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        tokenManager.clearTokens()
                        // Navigate to login
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Logout error: ${result.error.message}")
                    }
                    is NetworkResult.Loading -> {
                        // Show loading
                    }
                }
            }
        }
    }

    fun refresh() {
        loadUserShifts()
        loadLatestRecord()
    }
}

data class DashboardUiState(
    val screenState: ScreenState? = null,
    val shifts: List<Shift>? = null,
    val currentShift: Shift? = null,
    val latestRecord: AttendanceRecord? = null,
    val attendanceData: AttendanceData? = null,
    val selectedMonth: String = "April",
    val isCheckedIn: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showSuccessToast: Boolean = false,
    val showErrorToast: Boolean = false
)