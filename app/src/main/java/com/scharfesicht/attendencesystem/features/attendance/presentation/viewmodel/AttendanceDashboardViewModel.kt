package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.core.network.TokenManager
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.*
import com.scharfesicht.attendencesystem.features.facecompare.domain.FaceCompareUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.utils.ScreenState
import javax.inject.Inject
import kotlin.math.*
import kotlin.random.Random

@HiltViewModel
class AttendanceDashboardViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val faceCompareUseCase: FaceCompareUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val checkOutUseCase: CheckOutUseCase,
    private val getLatestRecordUseCase: GetLatestRecordUseCase,
    private val getUserShiftsUseCase: GetUserShiftsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private const val TAG = "AttendanceDashboard"
        private const val MAX_DISTANCE_METERS = 10.0
        private const val FACE_MATCH_THRESHOLD = 80.0
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedMonth = MutableStateFlow("April")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Camera and location triggers
    private val _shouldRequestLocation = MutableStateFlow(false)
    val shouldRequestLocation: StateFlow<Boolean> = _shouldRequestLocation.asStateFlow()

    private val _shouldOpenCamera = MutableStateFlow<CameraRequest?>(null)
    val shouldOpenCamera: StateFlow<CameraRequest?> = _shouldOpenCamera.asStateFlow()

    // Temporary storage for punch in/out flow
    private var currentLocation: Pair<Double, Double>? = null
    private var capturedPhoto: Bitmap? = null
    private var isPunchInFlow = true

    init {
        loadUserTheme()
        performAutoLogin()
    }

    private fun loadUserTheme() {
        viewModelScope.launch {
            MiniAppEntryPoint.superData?.getCurrentTheme()?.data?.let { theme ->
                _isDarkMode.value = theme == "dark"
            }
        }
    }

    private fun performAutoLogin() {
        viewModelScope.launch {
            loginUseCase(
                username = "rahul_kumar_1999",
                password = "Rahul@1321",
                deviceToken = Random.nextInt(1, 999999).toString()
            ).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            isLoginComplete = false,
                            errorMessage = null
                        )
                    }
                    is NetworkResult.Success -> {
                        tokenManager.saveJwtToken(result.data.token)

                        val loginData = result.data
                        val currentShift = loginData.shifts.firstOrNull()
                        val zone = loginData.zones.firstOrNull()

                        _uiState.value = _uiState.value.copy(
                            currentShift = currentShift,
                            profileImageUrl = loginData.profile_image,
                            userName = loginData.full_name,
                            expectedLatitude = zone?.zone_latitude?.toDoubleOrNull() ?: 0.0,
                            expectedLongitude = zone?.zone_longitude?.toDoubleOrNull() ?: 0.0,
                            maxDistanceMeters = zone?.zone_radius?.toDoubleOrNull() ?: MAX_DISTANCE_METERS,
                            isLoading = false,
                            isLoginComplete = true,
                            isCheckedIn = false
                        )

                        Log.d(TAG, "Login successful for: ${loginData.full_name}")
                        Log.d(TAG, "Shift: ${currentShift?.shift_name_lang}")
                        Log.d(TAG, "Expected location: ${zone?.zone_name_lang}")
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoginComplete = false,
                            errorMessage = AppMessage.Error(
                                message = result.error.message,
                                messageKey = "error_message"
                            ),
                            showErrorToast = true
                        )
                        Log.e(TAG, "Login failed: ${result.error.message}")
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

    /**
     * Start punch in flow
     * Step 1: Time validation
     * Step 2: Request location from Android
     */
    fun startPunchIn() {
        isPunchInFlow = true
        currentLocation = null
        capturedPhoto = null

        val currentShift = _uiState.value.currentShift
        if (currentShift == null) {
            showError("No shift assigned")
            return
        }

        val shiftRule = currentShift.shift_rule.firstOrNull()
        if (shiftRule == null) {
            showError("Invalid shift configuration")
            return
        }

        // Step 1: Time Validation
        val currentTime = System.currentTimeMillis()
        val shiftStartTime = parseTimeToMillis(shiftRule.start_time)
        val gracePeriodMs = (shiftRule.grace_period_in.toIntOrNull() ?: 10) * 60 * 1000L

        if (currentTime < shiftStartTime - gracePeriodMs) {
            val minutesEarly = ((shiftStartTime - gracePeriodMs - currentTime) / (60 * 1000)).toInt()
            showError("Too early to punch in. You can punch in $minutesEarly minutes later")
            return
        }

        // Step 2: Request location from Android
        _uiState.value = _uiState.value.copy(isPunchingIn = true)
        _shouldRequestLocation.value = true
    }

    /**
     * Called when location is received from Android system
     */
    fun onLocationReceived(latitude: Double, longitude: Double) {
        _shouldRequestLocation.value = false

        Log.d(TAG, "Location received: $latitude, $longitude")

        // Validate location distance
        val distance = calculateDistance(
            latitude,
            longitude,
            _uiState.value.expectedLatitude,
            _uiState.value.expectedLongitude
        )

        Log.d(TAG, "Distance from workplace: $distance meters")

        if (distance > _uiState.value.maxDistanceMeters) {
            showError("You are ${distance.toInt()} meters away. Please move closer (within ${_uiState.value.maxDistanceMeters.toInt()} meters)")
//            return
        }

        // Store location and request camera
        currentLocation = Pair(latitude, longitude)

        // Step 3: Request camera
        _shouldOpenCamera.value = CameraRequest(
            requestCode = if (isPunchInFlow) CAMERA_REQUEST_PUNCH_IN else CAMERA_REQUEST_PUNCH_OUT
        )
    }

    /**
     * Called when location request fails
     */
    fun onLocationError(error: String) {
        _shouldRequestLocation.value = false
        showError("Failed to get location: $error")
    }

    /**
     * Called when photo is captured from camera
     */
    fun onPhotoCaptured(bitmap: Bitmap) {
        _shouldOpenCamera.value = null
        capturedPhoto = bitmap

        Log.d(TAG, "Photo captured successfully")

        // Continue with face recognition and API call
        if (isPunchInFlow) {
            completePunchIn()
        } else {
            completePunchOut()
        }
    }

    /**
     * Called when camera is cancelled or fails
     */
    fun onCameraError(error: String) {
        _shouldOpenCamera.value = null
        showError("Failed to capture photo: $error")
    }

    /**
     * Complete punch in after photo captured
     */
    private fun completePunchIn() {
        viewModelScope.launch {
            try {
                val photo = capturedPhoto
                if (photo == null) {
                    showError("Photo not available")
                    return@launch
                }

                val location = currentLocation
                if (location == null) {
                    showError("Location not available")
                    return@launch
                }

                // Step 4: Face Compare
                val profileImageUrl = _uiState.value.profileImageUrl
                if (profileImageUrl.isNullOrEmpty()) {
                    showError("Profile image not found")
                    return@launch
                }

                Log.d(TAG, "Comparing faces...")
                val faceResult = faceCompareUseCase(
                    oldImageUrl = profileImageUrl,
                    newImage = photo
                )

                Log.d(TAG, "Face match accuracy: ${faceResult.accuracy}%")

                if (!faceResult.isSame || faceResult.accuracy < FACE_MATCH_THRESHOLD) {
                    showError("Face verification failed (${faceResult.accuracy.toInt()}% match). Please try again")
                    return@launch
                }

                // Step 5: Biometric Authentication
                Log.d(TAG, "Requesting biometric authentication")
                val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                if (bioSuccess != true) {
                    showError("Biometric authentication failed")
                    return@launch
                }

                // Step 6: Check-In API Call
                Log.d(TAG, "Calling check-in API")
                checkInUseCase(
                    latitude = location.first,
                    longitude = location.second,
                    shiftIndex = 0
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            Log.d(TAG, "Check-in API call in progress...")
                        }
                        is NetworkResult.Success -> {
                            val distance = calculateDistance(
                                location.first,
                                location.second,
                                _uiState.value.expectedLatitude,
                                _uiState.value.expectedLongitude
                            )
                            _uiState.value = _uiState.value.copy(
                                isPunchingIn = false,
                                successMessage = "Punch in successful! (${distance.toInt()}m from workplace)",
                                showSuccessToast = true,
                                isCheckedIn = true
                            )
                            // Clear temporary data
                            currentLocation = null
                            capturedPhoto = null
                            Log.d(TAG, "Check-in successful: ${result.data.uuid}")
                        }
                        is NetworkResult.Error -> {
                            showError("Check-in failed: ${result.error.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                showError(e.message ?: "An error occurred during punch in")
                Log.e(TAG, "Punch in error", e)
            }
        }
    }

    /**
     * Start punch out flow
     */
    fun startPunchOut() {
        isPunchInFlow = false
        currentLocation = null
        capturedPhoto = null

        _uiState.value = _uiState.value.copy(isPunchingOut = true)

        // Request location first
        _shouldRequestLocation.value = true
    }

    /**
     * Complete punch out after photo captured
     */
    private fun completePunchOut() {
        viewModelScope.launch {
            try {
                val photo = capturedPhoto
                if (photo == null) {
                    showError("Photo not available")
                    return@launch
                }

                val location = currentLocation
                if (location == null) {
                    showError("Location not available")
                    return@launch
                }

                // Face Compare
                val profileImageUrl = _uiState.value.profileImageUrl
                if (!profileImageUrl.isNullOrEmpty()) {
                    Log.d(TAG, "Comparing faces for punch out")
                    val faceResult = faceCompareUseCase(
                        oldImageUrl = profileImageUrl,
                        newImage = photo
                    )

                    Log.d(TAG, "Punch out face match: ${faceResult.accuracy}%")

                    if (!faceResult.isSame || faceResult.accuracy < FACE_MATCH_THRESHOLD) {
                        showError("Face verification failed (${faceResult.accuracy.toInt()}% match)")
                        return@launch
                    }
                }

                // Biometric Authentication
                Log.d(TAG, "Requesting biometric for punch out")
                val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                if (bioSuccess != true) {
                    showError("Biometric authentication failed")
                    return@launch
                }

                // Check-Out API Call
                Log.d(TAG, "Calling check-out API")
                checkOutUseCase(
                    latitude = location.first,
                    longitude = location.second,
                    shiftIndex = 0
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            Log.d(TAG, "Check-out API call in progress...")
                        }
                        is NetworkResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isPunchingOut = false,
                                successMessage = "Punch out successful!",
                                showSuccessToast = true,
                                isCheckedIn = false
                            )
                            // Clear temporary data
                            currentLocation = null
                            capturedPhoto = null
                            Log.d(TAG, "Check-out successful: ${result.data.uuid}")
                        }
                        is NetworkResult.Error -> {
                            showError("Check-out failed: ${result.error.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                showError(e.message ?: "An error occurred during punch out")
                Log.e(TAG, "Punch out error", e)
            }
        }
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            isPunchingIn = false,
            isPunchingOut = false,
            errorMessage = AppMessage.Error(
                message = message,
                messageKey = "error_message"
            ),
            showErrorToast = true
        )
        // Clear temporary data on error
        currentLocation = null
        capturedPhoto = null
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c * 1000
    }

    private fun parseTimeToMillis(timeString: String): Long {
        try {
            val parts = timeString.split(":")
            if (parts.size < 2) return 0L

            val hours = parts[0].toLongOrNull() ?: 0L
            val minutes = parts[1].toLongOrNull() ?: 0L

            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)

            return calendar.timeInMillis +
                    (hours * 60 * 60 * 1000) +
                    (minutes * 60 * 1000)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time: $timeString", e)
            return 0L
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
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Logout error: ${result.error.message}")
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun refresh() {
        performAutoLogin()
    }
}

// Camera request data class
data class CameraRequest(
    val requestCode: Int
)

// Camera request codes
const val CAMERA_REQUEST_PUNCH_IN = 1001
const val CAMERA_REQUEST_PUNCH_OUT = 1002

data class DashboardUiState(
    val screenState: ScreenState? = null,
    val currentShift: Shift? = null,
    val profileImageUrl: String? = null,
    val userName: String? = null,
    val expectedLatitude: Double = 0.0,
    val expectedLongitude: Double = 0.0,
    val maxDistanceMeters: Double = 0.0,
    val isCheckedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginComplete: Boolean = false,
    val isPunchingIn: Boolean = false,
    val isPunchingOut: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: AppMessage? = null,
    val showSuccessToast: Boolean = false,
    val showErrorToast: Boolean = false
)