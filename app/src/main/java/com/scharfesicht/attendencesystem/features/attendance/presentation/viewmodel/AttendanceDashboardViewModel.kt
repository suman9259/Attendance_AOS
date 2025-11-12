package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.ui.theme.ThemeMode
import com.scharfesicht.attendencesystem.core.network.TokenManager
import com.scharfesicht.attendencesystem.domain.absher.model.AppLanguage
import com.scharfesicht.attendencesystem.domain.absher.model.AppPreferences
import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import com.scharfesicht.attendencesystem.domain.absher.usecase.GetUserInfoUseCase
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.LoginUseCase
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.MarkAttendanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalTime
import javax.inject.Inject

sealed class AttendanceDashboardUiState {
    object Loading : AttendanceDashboardUiState()
    data class Success(
        val shift: ShiftData?,
        val userInfo: UserInfo,
        val todayAttendance: AttendanceRecord?,
        val selectedTab: AttendanceTab = AttendanceTab.MARK_ATTENDANCE,
        val appPreferences: AppPreferences = AppPreferences()
    ) : AttendanceDashboardUiState()
    data class Error(val message: String) : AttendanceDashboardUiState()
}

enum class AttendanceTab { MARK_ATTENDANCE, PERMISSION_APPLICATION }

@HiltViewModel
class AttendanceDashboardViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
//    private val apiService: AttendanceApiService,
    private val loginUseCase: LoginUseCase,
    private val markAttendanceUseCase: MarkAttendanceUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttendanceDashboardUiState>(AttendanceDashboardUiState.Loading)
    val uiState: StateFlow<AttendanceDashboardUiState> = _uiState.asStateFlow()

    private val _punchInOutLoading = MutableStateFlow(false)
    val punchInOutLoading: StateFlow<Boolean> = _punchInOutLoading.asStateFlow()

    private val _showSuccessDialog = MutableStateFlow<String?>(null)
    val showSuccessDialog: StateFlow<String?> = _showSuccessDialog.asStateFlow()

    private val _appPreferences = MutableStateFlow(AppPreferences())
    val appPreferences: StateFlow<AppPreferences> = _appPreferences.asStateFlow()

    init {
        loadDashboardFlow()
    }

    private fun loadDashboardFlow() {
        viewModelScope.launch {
            _uiState.value = AttendanceDashboardUiState.Loading

            try {
                // Step 1️⃣ — Try to get user info from Absher
                val userInfo = try {
                    getUserInfoUseCase().first().getOrThrow()
                } catch (e: Exception) {
                    // Step 2️⃣ — fallback to mock user if Absher not initialized
                    Log.e("AbsharAppLog","Absher not initialized: ${e.message}")
                    UserInfo(
                        nationalId = "Dev01",
                        fullNameEn = "John Doe",
                        fullNameAr = "جون دو",
                        token = "null",
                        theme = ThemeMode.SYSTEM,
                        language = AppLanguage.ENGLISH,
                        isRTL = false,
                        firstNameAr = "TODO()"
                    )
                }

                _appPreferences.value = AppPreferences.fromAbsher(userInfo)

                // Step 2: Call login API using Absher data
                val loginResult = loginUseCase(
                    LoginRequest(
                    )
                )

                loginResult.fold(
                    onSuccess = { response ->
                        val loginData = response.data
                        // Step 4️⃣ — Save token locally
                        loginData?.token?.let { tokenManager.saveJwtToken(it) }

                        if (loginData?.token != null) {
                            tokenManager.saveJwtToken(loginData.token)
                            _uiState.value = AttendanceDashboardUiState.Success(
                                shift = loginData.shifts?.firstOrNull(),
                                userInfo = userInfo,
                                todayAttendance = null
                            )
                        } else {
                            _uiState.value = AttendanceDashboardUiState.Error("Invalid login response")
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = AttendanceDashboardUiState.Error("Login failed: ${e.message}")
                    }
                )



//                // Step 5️⃣ — Fetch user shifts
//                val shiftResponse = apiService.getUserShifts()
//                val shift = if (shiftResponse.isSuccessful && !shiftResponse.body().isNullOrEmpty()) {
//                    val s = shiftResponse.body()!!.first()
//                    Shift(
//                        id = s.id,
//                        name = s.name,
//                        nameAr = s.name_ar,
//                        startTime = LocalTime.parse(s.start_time),
//                        endTime = LocalTime.parse(s.end_time),
//                        type = ShiftType.valueOf(s.type.uppercase())
//                    )
//                } else null

//                // Step 6️⃣ — Show dashboard
//                _uiState.value = AttendanceDashboardUiState.Success(
//                    shift = shift,
//                    userInfo = userInfo,
//                    todayAttendance = null,
//                    appPreferences = _appPreferences.value
//                )

            } catch (e: IOException) {
                _uiState.value = AttendanceDashboardUiState.Error("Network error: ${e.message}")
            } catch (e: HttpException) {
                _uiState.value = AttendanceDashboardUiState.Error("Server error: ${e.code()}")
            } catch (e: Exception) {
                _uiState.value = AttendanceDashboardUiState.Error("Initialization failed: ${e.message}")
            }
        }
    }


    fun selectTab(tab: AttendanceTab) {
        val current = _uiState.value
        if (current is AttendanceDashboardUiState.Success) {
            _uiState.value = current.copy(selectedTab = tab)
        }
    }

    fun punchIn() {
        viewModelScope.launch {
            _punchInOutLoading.value = true
            markAttendanceUseCase.punchIn().fold(
                onSuccess = {
                    val prefs = _appPreferences.value
                    _showSuccessDialog.value = AttendanceStrings.punchInSuccess.get(prefs.isArabic)
                    loadDashboardFlow()
                },
                onFailure = {
                    _showSuccessDialog.value = "Error: ${it.message}"
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
                    val prefs = _appPreferences.value
                    _showSuccessDialog.value = AttendanceStrings.punchOutSuccess.get(prefs.isArabic)
                    loadDashboardFlow()
                },
                onFailure = {
                    _showSuccessDialog.value = "Error: ${it.message}"
                }
            )
            _punchInOutLoading.value = false
        }
    }

    fun dismissSuccessDialog() {
        _showSuccessDialog.value = null
    }
}
