package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import com.scharfesicht.attendencesystem.presentation.dashboard.AttendanceData


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.utils.ScreenState
import javax.inject.Inject

@HiltViewModel
class AttendanceDashboardViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    init {
        loadUserTheme()
        loadAttendanceData()
    }

    private fun loadUserTheme() {
        viewModelScope.launch {
            MiniAppEntryPoint.superData?.getCurrentTheme()?.data?.let { theme ->
                _isDarkMode.value = theme == "dark"
            }
        }
    }

    private fun loadAttendanceData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(screenState = ScreenState.Loading)

            try {
                val data = repository.getAttendanceData()
                _uiState.value = _uiState.value.copy(
                    screenState = ScreenState.Success(data),
                    attendanceData = data
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = ScreenState.Error(e.message ?: "Unknown error"),
                    errorMessage = e.message ?: "Failed to load attendance data"
                )
            }
        }
    }

    fun onTabChanged(tab: Int) {
        _selectedTab.value = tab
    }

    fun onMonthChanged(month: String) {
        _uiState.value = _uiState.value.copy(selectedMonth = month)
        loadAttendanceData()
    }

    fun onPunchIn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get location
                MiniAppEntryPoint.superData?.getLocation()?.data?.let { location ->
                    // Check if location is within allowed area
                    val isLocationValid = repository.validateLocation(location.latitude, location.longitude)

                    if (isLocationValid) {
                        // Trigger biometric authentication
                        MiniAppEntryPoint.superData?.authenticateBiometric()?.data?.let { success ->
                            if (success) {
                                // Mark punch in
                                val result = repository.punchIn(location.latitude, location.longitude)
                                if (result) {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        successMessage = "Punch in successful",
                                        showSuccessToast = true
                                    )
                                    // Reload data
                                    loadAttendanceData()
                                } else {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = "Failed to mark punch in",
                                        showErrorToast = true
                                    )
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Biometric authentication failed",
                                    showErrorToast = true
                                )
                            }
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "You are not in the allowed location",
                            showErrorToast = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to punch in",
                    showErrorToast = true
                )
            }
        }
    }

    fun onPunchOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get location
                MiniAppEntryPoint.superData?.getLocation()?.data?.let { location ->
                    val isLocationValid = repository.validateLocation(location.latitude, location.longitude)

                    if (isLocationValid) {
                        MiniAppEntryPoint.superData?.authenticateBiometric()?.data?.let { success ->
                            if (success) {
                                val result = repository.punchOut(location.latitude, location.longitude)
                                if (result) {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        successMessage = "Punch out successful",
                                        showSuccessToast = true
                                    )
                                    loadAttendanceData()
                                } else {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = "Failed to mark punch out",
                                        showErrorToast = true
                                    )
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Biometric authentication failed",
                                    showErrorToast = true
                                )
                            }
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "You are not in the allowed location",
                            showErrorToast = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to punch out",
                    showErrorToast = true
                )
            }
        }
    }

    fun onToastShown() {
        _uiState.value = _uiState.value.copy(
            showSuccessToast = false,
            showErrorToast = false
        )
    }
}

data class DashboardUiState(
    val screenState: ScreenState? = null,
    val attendanceData: AttendanceData? = null,
    val selectedMonth: String = "April",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showSuccessToast: Boolean = false,
    val showErrorToast: Boolean = false
)