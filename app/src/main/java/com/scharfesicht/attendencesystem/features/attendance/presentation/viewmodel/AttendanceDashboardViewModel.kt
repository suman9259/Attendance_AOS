package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.core.network.TokenManager
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.*
import com.scharfesicht.attendencesystem.features.facecompare.domain.FaceCompareUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.utils.ScreenState
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class AttendanceDashboardViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val faceCompareUseCase: FaceCompareUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val checkOutUseCase: CheckOutUseCase,
    private val getLatestRecordUseCase: GetLatestRecordUseCase,
    private val getUserShiftsUseCase: GetUserShiftsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager,
    private val preferenceStorage: IPreferenceStorage
) : ViewModel() {

    companion object {
        private const val TAG = "AttendanceDashboard"
        private const val MAX_DISTANCE_METERS = 10.0
        private const val FACE_MATCH_THRESHOLD = 80.0
        private const val SHIFTS_REFRESH_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours
        private const val CLICK_DEBOUNCE_MS = 2000L // 2 seconds
        private const val MAX_LATE_MINUTES_WARNING = 120 // Show warning if more than 2 hours late
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Camera and location triggers
    private val _shouldRequestLocation = MutableStateFlow(false)
    val shouldRequestLocation: StateFlow<Boolean> = _shouldRequestLocation.asStateFlow()

    private val _shouldOpenCamera = MutableStateFlow<CameraRequest?>(null)
    val shouldOpenCamera: StateFlow<CameraRequest?> = _shouldOpenCamera.asStateFlow()

    // Temporary storage for punch in/out flow
    private var currentLocation: Pair<Double, Double>? = null
    private var capturedPhoto: Bitmap? = null
    private var isPunchInFlow = true

    // Debounce tracking
    private var lastPunchInClick = 0L
    private var lastPunchOutClick = 0L

    init {
        safeExecute("initializeApp") { initializeApp() }
    }

    /**
     * Initialize app - Load from DataStore first, then refresh if needed
     * CRASH-SAFE: Handles all exceptions gracefully
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val isLoggedIn = safeGetPreference(
                    preferenceStorage.isLoggedIn,
                    defaultValue = false,
                    description = "isLoggedIn"
                )

                val token = safeGetPreference(
                    preferenceStorage.jwtToken,
                    defaultValue = null,
                    description = "jwtToken"
                )

                if (isLoggedIn && !token.isNullOrEmpty()) {
                    Log.d(TAG, "User already logged in, loading cached data")
                    loadCachedData()
                    safeExecute("checkAndRefreshShifts") {
                        checkAndRefreshShifts()
                    }
                } else {
                    Log.d(TAG, "No cached login, performing fresh login")
                    performLogin()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Critical error in initializeApp", e)
                handleInitializationError(e)
            }
        }
    }

    /**
     * Load cached data from DataStore
     * CRASH-SAFE: Returns defaults if any data is corrupted
     */
    private suspend fun loadCachedData() {
        try {
            val userName = safeGetPreference(
                preferenceStorage.userName,
                defaultValue = "User",
                description = "userName"
            ) ?: "User"

            val profileImageUrl = safeGetPreference(
                preferenceStorage.profileImageUrl,
                defaultValue = null,
                description = "profileImageUrl"
            )

            val zoneLat = safeParseDouble(
                safeGetPreference(preferenceStorage.zoneLatitude, null, "zoneLatitude"),
                defaultValue = 0.0,
                description = "zone latitude"
            )

            val zoneLon = safeParseDouble(
                safeGetPreference(preferenceStorage.zoneLongitude, null, "zoneLongitude"),
                defaultValue = 0.0,
                description = "zone longitude"
            )

            val zoneRadius = safeParseDouble(
                safeGetPreference(preferenceStorage.zoneRadius, null, "zoneRadius"),
                defaultValue = MAX_DISTANCE_METERS,
                description = "zone radius"
            )

            val shiftsJson = safeGetPreference(
                preferenceStorage.userShiftsJson,
                defaultValue = null,
                description = "userShiftsJson"
            )

            val shifts = safeParseShifts(shiftsJson)
            val currentShift = shifts.firstOrNull()

            val isCheckedIn = safeGetPreference(
                preferenceStorage.isCheckedIn,
                defaultValue = false,
                description = "isCheckedIn"
            )

            _uiState.value = _uiState.value.copy(
                currentShift = currentShift,
                allShifts = shifts,
                profileImageUrl = profileImageUrl,
                userName = userName,
                expectedLatitude = "30.335083".toDouble(),
                expectedLongitude = "76.382726".toDouble(),
                //TODO: Delete When You are releasing.
//                expectedLatitude = zoneLat,
//                expectedLongitude = zoneLon,
                maxDistanceMeters = zoneRadius,
                isLoading = false,
                isLoginComplete = true,
                isCheckedIn = isCheckedIn
            )

            Log.d(TAG, "Successfully loaded cached data: user=$userName, shifts=${shifts.size}, checkedIn=$isCheckedIn")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached data, falling back to fresh login", e)
            performLogin()
        }
    }

    /**
     * Check if shifts need refresh and refresh if needed
     * CRASH-SAFE: Won't crash if refresh fails
     */
    private suspend fun checkAndRefreshShifts() {
        try {
            val lastUpdated = safeGetPreference(
                preferenceStorage.shiftsLastUpdated,
                defaultValue = 0L,
                description = "shiftsLastUpdated"
            ) ?: 0L

            val now = System.currentTimeMillis()

            if (now - lastUpdated > SHIFTS_REFRESH_INTERVAL) {
                Log.d(TAG, "Shifts data is stale, refreshing...")
                refreshShifts()
            } else {
                val minutesOld = (now - lastUpdated) / 1000 / 60
                Log.d(TAG, "Shifts data is fresh ($minutesOld minutes old)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking shifts refresh, skipping", e)
        }
    }

    /**
     * Refresh shifts from API
     * CRASH-SAFE: Handles API failures gracefully
     */
    private suspend fun refreshShifts() {
        try {
            getUserShiftsUseCase().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        try {
                            val shifts = result.data

                            try {
                                val shiftsJson = json.encodeToString(shifts)
                                preferenceStorage.saveUserShifts(shiftsJson)
                            } catch (e: SerializationException) {
                                Log.e(TAG, "Failed to serialize shifts", e)
                            }

                            _uiState.value = _uiState.value.copy(
                                allShifts = shifts,
                                currentShift = shifts.firstOrNull()
                            )

                            Log.d(TAG, "Refreshed ${shifts.size} shifts from API")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing shifts data", e)
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Failed to refresh shifts: ${result.error.message}")
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error refreshing shifts", e)
        }
    }

    /**
     * Perform initial login - Only called once or when cache is invalid
     * CRASH-SAFE: Shows error to user if login fails
     */
    private fun performLogin() {
        viewModelScope.launch {
            try {
                loginUseCase(
                    username = "rahul_kumar_1999",
                    password = "Rahul@1321",
                    deviceToken = System.currentTimeMillis().toString()
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
                            try {
                                val loginData = result.data

                                safeExecute("saveLoginData") {
                                    saveLoginData(loginData)
                                }

                                val currentShift = loginData.shifts.firstOrNull()
                                val zone = loginData.zones.firstOrNull()

                                _uiState.value = _uiState.value.copy(
                                    currentShift = currentShift,
                                    allShifts = loginData.shifts,
                                    profileImageUrl = loginData.profile_image,
                                    userName = loginData.full_name,
                                    // TODO: Delete When You are releasing. it only for testing
                                    expectedLatitude = safeParseDouble("30.335083", 0.0, "zone latitude"),
                                    expectedLongitude = safeParseDouble("76.382726", 0.0, "zone longitude"),
                                    maxDistanceMeters = safeParseDouble(zone?.zone_radius, MAX_DISTANCE_METERS, "zone radius"),
                                    isLoading = false,
                                    isLoginComplete = true,
                                    isCheckedIn = false
                                )

                                Log.d(TAG, "Login successful: ${loginData.full_name}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing login data", e)
                                showError("Error processing login data: ${e.message}")
                            }
                        }

                        is NetworkResult.Error -> {
                            val errorMsg = result.error.message ?: "Unknown login error"
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoginComplete = false,
                                errorMessage = AppMessage.Error(
                                    message = errorMsg,
                                    messageKey = "error_message"
                                ),
                                showErrorToast = true
                            )
                            Log.e(TAG, "Login failed: $errorMsg")
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in performLogin", e)
                handleInitializationError(e)
            }
        }
    }

    /**
     * Save login data to DataStore
     * CRASH-SAFE: Logs errors but doesn't crash if saving fails
     */
    private suspend fun saveLoginData(loginData: LoginData) {
        try {
            tokenManager.saveJwtToken(loginData.token)
            preferenceStorage.setLoggedIn(true)
            preferenceStorage.saveUserName(loginData.full_name)
            preferenceStorage.saveProfileImageUrl(loginData.profile_image ?: "")

            try {
                val shiftsJson = json.encodeToString(loginData.shifts)
                preferenceStorage.saveUserShifts(shiftsJson)
            } catch (e: SerializationException) {
                Log.e(TAG, "Failed to serialize shifts", e)
            }

            val zone = loginData.zones.firstOrNull()
            if (zone != null) {
                try {
                    preferenceStorage.saveZoneData(
                        latitude = zone.zone_latitude,
                        longitude = zone.zone_longitude,
                        radius = zone.zone_radius,
                        name = zone.zone_name_lang
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save zone data", e)
                }
            }

            Log.d(TAG, "Successfully saved login data to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving login data", e)
        }
    }

    /**
     * Check current attendance status from API
     * CRASH-SAFE: Won't crash if API fails
     */
    private fun checkCurrentAttendanceStatus() {
        viewModelScope.launch {
            try {
                getLatestRecordUseCase().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            try {
                                val records = result.data
                                val latestRecord = records.firstOrNull()

                                val isCheckedIn = latestRecord?.checkout_time == null &&
                                        latestRecord?.checkin_time != null

                                preferenceStorage.setCheckedIn(isCheckedIn)
                                _uiState.value = _uiState.value.copy(isCheckedIn = isCheckedIn)

                                Log.d(TAG, "Attendance status: checkedIn=$isCheckedIn")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing attendance records", e)
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Failed to get attendance status: ${result.error.message}")
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error checking attendance status", e)
            }
        }
    }

    fun onTabChanged(tab: Int) {
        _selectedTab.value = tab
    }

    /**
     * Parse shift time (HH:mm format) to Calendar
     */
    private fun parseShiftTime(timeString: String?): Calendar? {
        return try {
            if (timeString.isNullOrEmpty()) return null

            val parts = timeString.split(":")
            if (parts.size < 2) return null

            val hours = parts[0].toIntOrNull() ?: return null
            val minutes = parts[1].toIntOrNull() ?: return null

            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing shift time: $timeString", e)
            null
        }
    }

    /**
     * Calculate time difference in minutes between current time and shift time
     * Positive value = shift time is in the past (late)
     * Negative value = shift time is in the future (early)
     */
    private fun calculateTimeDifferenceMinutes(currentTime: Calendar, shiftTime: Calendar): Int {
        val diffMillis = currentTime.timeInMillis - shiftTime.timeInMillis
        return (diffMillis / (60 * 1000)).toInt()
    }

    /**
     * Start punch in flow with comprehensive validation
     * CRASH-SAFE: Validates all conditions before proceeding
     *
     * Validation steps:
     * 1. Debounce check (prevent multiple rapid clicks)
     * 2. Shift assignment check
     * 3. Time validation with grace period
     * 4. Location check (triggered after validation)
     * 5. Camera/Face recognition (triggered after location)
     * 6. API call
     */
    fun startPunchIn() {
        try {
            // Step 1: Debounce check
            val currentClickTime = System.currentTimeMillis()
            if (currentClickTime - lastPunchInClick < CLICK_DEBOUNCE_MS) {
                Log.d(TAG, "Punch in clicked too quickly, ignoring")
                return
            }
            lastPunchInClick = currentClickTime

            // Check if already punching in
            if (_uiState.value.isPunchingIn) {
                Log.d(TAG, "Already punching in, ignoring")
                return
            }

            // Check if already checked in
            if (_uiState.value.isCheckedIn) {
                showError("You are already punched in. Please punch out first.")
                return
            }

            // Reset state
            isPunchInFlow = true
            currentLocation = null
            capturedPhoto = null

            // Step 2: Check shift assignment
            val currentShift = _uiState.value.currentShift
            if (currentShift == null) {
                showError("No shift assigned. Please contact your administrator.")
                return
            }

            val shiftRule = currentShift?.shift_rule!!.firstOrNull()
            if (shiftRule == null) {
                showError("Invalid shift configuration. Please contact your administrator.")
                return
            }

            // Step 3: Time validation with grace period
            val currentTime = Calendar.getInstance()
            val shiftStartTime = parseShiftTime(shiftRule?.start_time)
            val gracePeriodMinutes = shiftRule?.grace_period_in?.toIntOrNull() ?: 10

            if (shiftStartTime == null) {
                showError("Invalid shift start time. Please contact your administrator.")
                return
            }

            // Calculate time difference in minutes
            val timeDifferenceMinutes = calculateTimeDifferenceMinutes(currentTime, shiftStartTime!!)

            Log.d(TAG, "Time validation: current vs shift = $timeDifferenceMinutes minutes, grace period = $gracePeriodMinutes minutes")

            // Check if too early (before grace period)
            // If shift starts at 9:00 AM and grace period is 10 minutes,
            // user can punch in from 8:50 AM onwards
            if (timeDifferenceMinutes < -gracePeriodMinutes) {
                val minutesUntilAllowed = abs(timeDifferenceMinutes) - gracePeriodMinutes
                showError("Too early to punch in.\nYou can punch in after $minutesUntilAllowed minutes.\nShift starts at: ${shiftRule?.start_time}")
                return
            }

            // Show warning if significantly late (but still allow punch in)
            if (timeDifferenceMinutes > MAX_LATE_MINUTES_WARNING) {
                showWarning("You are ${timeDifferenceMinutes} minutes late.\nPlease contact your supervisor if needed.")
                // Continue with punch in flow
            }

            Log.d(TAG, "✓ Time validation passed. Starting location request...")
            _uiState.value = _uiState.value.copy(isPunchingIn = true)
            _shouldRequestLocation.value = true

        } catch (e: Exception) {
            Log.e(TAG, "Error in startPunchIn", e)
            showError("Failed to start punch in: ${e.message}")
        }
    }

    /**
     * Start punch out flow with comprehensive validation
     * CRASH-SAFE: Validates all conditions before proceeding
     *
     * Validation steps:
     * 1. Debounce check
     * 2. Check if user is checked in
     * 3. Shift time validation (show warning if early)
     * 4. Location check
     * 5. Camera/Face recognition
     * 6. Biometric authentication
     * 7. API call
     */
    fun startPunchOut() {
        try {
            // Step 1: Debounce check
            val currentClickTime = System.currentTimeMillis()
            if (currentClickTime - lastPunchOutClick < CLICK_DEBOUNCE_MS) {
                Log.d(TAG, "Punch out clicked too quickly, ignoring")
                return
            }
            lastPunchOutClick = currentClickTime

            // Check if already punching out
            if (_uiState.value.isPunchingOut) {
                Log.d(TAG, "Already punching out, ignoring")
                return
            }

            // Step 2: Check if user is checked in
            if (!_uiState.value.isCheckedIn) {
                showError("You are not punched in. Please punch in first.")
                return
            }

            // Reset state
            isPunchInFlow = false
            currentLocation = null
            capturedPhoto = null

            // Step 3: Shift time validation for punch out
            val currentShift = _uiState.value.currentShift
            if (currentShift == null) {
                showError("No shift assigned. Please contact your administrator.")
                return
            }

            val shiftRule = currentShift.shift_rule.firstOrNull()
            if (shiftRule == null) {
                showError("Invalid shift configuration. Please contact your administrator.")
                return
            }

            // Check if punching out before shift end time
            val currentTime = Calendar.getInstance()
            val shiftEndTime = parseShiftTime(shiftRule.end_time)

            if (shiftEndTime != null) {
                val timeDifferenceMinutes = calculateTimeDifferenceMinutes(currentTime, shiftEndTime)

                // If negative, means shift end time is in the future (punching out early)
                if (timeDifferenceMinutes < 0) {
                    val minutesEarly = abs(timeDifferenceMinutes)
                    showWarning("You are punching out $minutesEarly minutes early.\nShift ends at: ${shiftRule.end_time}\nYour supervisor will be notified.")
                    // Continue with punch out flow
                }
            }

            Log.d(TAG, "✓ Validation passed. Starting location request for punch out...")
            _uiState.value = _uiState.value.copy(isPunchingOut = true)
            _shouldRequestLocation.value = true

        } catch (e: Exception) {
            Log.e(TAG, "Error in startPunchOut", e)
            showError("Failed to start punch out: ${e.message}")
        }
    }

    /**
     * Handle location received - Step 4 in validation flow
     */
    fun onLocationReceived(latitude: Double, longitude: Double) {
        try {
            _shouldRequestLocation.value = false

            // Validate coordinates
            if (!isValidCoordinate(latitude, longitude)) {
                showError("Invalid location coordinates received. Please try again.")
                resetPunchState()
                return
            }

            // Calculate distance from expected location
            val distance = calculateDistance(
                latitude,
                longitude,
                _uiState.value.expectedLatitude,
                _uiState.value.expectedLongitude
            )

            val maxDistance = _uiState.value.maxDistanceMeters

            Log.d(TAG, "✓ Location received: distance = ${distance.toInt()}m, max allowed = ${maxDistance.toInt()}m")

            // Validate distance
            if (distance > maxDistance) {
                showError(
                    "Location validation failed!\n\n" +
                            "You are ${distance.toInt()} meters away from the designated location.\n\n" +
                            "Please move closer (within ${maxDistance.toInt()} meters) and try again."
                )
                resetPunchState()
                return
            }

            Log.d(TAG, "✓ Location validation passed. Opening camera...")
            currentLocation = Pair(latitude, longitude)
            _shouldOpenCamera.value = CameraRequest(
                requestCode = if (isPunchInFlow) CAMERA_REQUEST_PUNCH_IN else CAMERA_REQUEST_PUNCH_OUT
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing location", e)
            showError("Location error: ${e.message}")
            resetPunchState()
        }
    }

    fun onLocationError(error: String) {
        _shouldRequestLocation.value = false
        showError("Location error: $error\n\nPlease enable location services and grant permission.")
        resetPunchState()
    }

    /**
     * Handle photo captured - Step 5 in validation flow
     */
    fun onPhotoCaptured(bitmap: Bitmap) {
        try {
            _shouldOpenCamera.value = null
            capturedPhoto = bitmap

            Log.d(TAG, "✓ Photo captured: ${bitmap.width}x${bitmap.height}")

            if (isPunchInFlow) {
                completePunchIn()
            } else {
                completePunchOut()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing captured photo", e)
            showError("Photo error: ${e.message}")
            resetPunchState()
        }
    }

    fun onCameraError(error: String) {
        _shouldOpenCamera.value = null
        showError("Camera error: $error\n\nPlease grant camera permission and try again.")
        resetPunchState()
    }

    /**
     * Complete punch in - Steps 6 & 7: Face recognition, Biometric, API call
     */
    private fun completePunchIn() {
        viewModelScope.launch {
            try {
                val photo = capturedPhoto
                if (photo == null) {
                    showError("Photo unavailable. Please try again.")
                    resetPunchState()
                    return@launch
                }

                val location = currentLocation
                if (location == null) {
                    showError("Location unavailable. Please try again.")
                    resetPunchState()
                    return@launch
                }

                val profileImageUrl = _uiState.value.profileImageUrl

                // Step 6: Face comparison (if profile image exists)
                if (!profileImageUrl.isNullOrEmpty()) {
                    try {
                        Log.d(TAG, "Starting face comparison...")
                        val faceResult = faceCompareUseCase(profileImageUrl, photo)
                        Log.d(TAG, "✓ Face comparison result: match=${faceResult.isSame}, accuracy=${faceResult.accuracy}%")

                        if (!faceResult.isSame || faceResult.accuracy < FACE_MATCH_THRESHOLD) {
                            showError(
                                "Face verification failed!\n\n" +
                                        "Match accuracy: ${faceResult.accuracy.toInt()}%\n" +
                                        "Required: ${FACE_MATCH_THRESHOLD.toInt()}%\n\n" +
                                        "Please try again with better lighting."
                            )
                            resetPunchState()
                            return@launch
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Face comparison error", e)
                        showError("Face verification error: ${e.message}")
                        resetPunchState()
                        return@launch
                    }
                } else {
                    Log.d(TAG, "⚠ Skipping face comparison (no profile image)")
                }

                // Step 7: Biometric authentication
                try {
                    Log.d(TAG, "Starting biometric authentication...")
                    val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                    if (bioSuccess != true) {
                        showError("Biometric authentication failed.\n\nPlease authenticate and try again.")
                        resetPunchState()
                        return@launch
                    }
                    Log.d(TAG, "✓ Biometric authentication successful")
                } catch (e: Exception) {
                    Log.e(TAG, "Biometric error", e)
                    showError("Biometric error: ${e.message}")
                    resetPunchState()
                    return@launch
                }

                // Step 8: API call
                Log.d(TAG, "Calling check-in API...")
                checkInUseCase(location.first, location.second, 0).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            try {
                                preferenceStorage.setCheckedIn(true)

                                _uiState.value = _uiState.value.copy(
                                    isPunchingIn = false,
                                    successMessage = "✓ Punch in successful!",
                                    showSuccessToast = true,
                                    isCheckedIn = true
                                )

                                Log.d(TAG, "✓✓✓ PUNCH IN COMPLETED SUCCESSFULLY ✓✓✓")

                                currentLocation = null
                                capturedPhoto = null
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating UI after punch in", e)
                            }
                        }
                        is NetworkResult.Error -> {
                            val errorMsg = result.error.message ?: "Unknown error"
                            showError("Check-in failed: $errorMsg")
                            resetPunchState()
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in completePunchIn", e)
                showError("Punch in error: ${e.message}")
                resetPunchState()
            }
        }
    }

    /**
     * Complete punch out - Steps 6 & 7: Face recognition, Biometric, API call
     */
    private fun completePunchOut() {
        viewModelScope.launch {
            try {
                val photo = capturedPhoto
                if (photo == null) {
                    showError("Photo unavailable. Please try again.")
                    resetPunchState()
                    return@launch
                }

                val location = currentLocation
                if (location == null) {
                    showError("Location unavailable. Please try again.")
                    resetPunchState()
                    return@launch
                }

                val profileImageUrl = _uiState.value.profileImageUrl

                // Step 6: Face comparison
                if (!profileImageUrl.isNullOrEmpty()) {
                    try {
                        Log.d(TAG, "Starting face comparison...")
                        val faceResult = faceCompareUseCase(profileImageUrl, photo)
                        Log.d(TAG, "✓ Face comparison result: match=${faceResult.isSame}, accuracy=${faceResult.accuracy}%")

                        if (!faceResult.isSame || faceResult.accuracy < FACE_MATCH_THRESHOLD) {
                            showError(
                                "Face verification failed!\n\n" +
                                        "Match accuracy: ${faceResult.accuracy.toInt()}%\n" +
                                        "Required: ${FACE_MATCH_THRESHOLD.toInt()}%\n\n" +
                                        "Please try again with better lighting."
                            )
                            resetPunchState()
                            return@launch
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Face comparison error", e)
                        showError("Face verification error: ${e.message}")
                        resetPunchState()
                        return@launch
                    }
                } else {
                    Log.d(TAG, "⚠ Skipping face comparison (no profile image)")
                }

                // Step 7: Biometric authentication
                try {
                    Log.d(TAG, "Starting biometric authentication...")
                    val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                    if (bioSuccess != true) {
                        showError("Biometric authentication failed.\n\nPlease authenticate and try again.")
                        resetPunchState()
                        return@launch
                    }
                    Log.d(TAG, "✓ Biometric authentication successful")
                } catch (e: Exception) {
                    Log.e(TAG, "Biometric error", e)
                    showError("Biometric error: ${e.message}")
                    resetPunchState()
                    return@launch
                }

                // Step 8: API call
                Log.d(TAG, "Calling check-out API...")
                checkOutUseCase(location.first, location.second, 0).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            try {
                                preferenceStorage.setCheckedIn(false)

                                _uiState.value = _uiState.value.copy(
                                    isPunchingOut = false,
                                    successMessage = "✓ Punch out successful!",
                                    showSuccessToast = true,
                                    isCheckedIn = false
                                )

                                Log.d(TAG, "✓✓✓ PUNCH OUT COMPLETED SUCCESSFULLY ✓✓✓")

                                currentLocation = null
                                capturedPhoto = null
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating UI after punch out", e)
                            }
                        }
                        is NetworkResult.Error -> {
                            val errorMsg = result.error.message ?: "Unknown error"
                            showError("Check-out failed: $errorMsg")
                            resetPunchState()
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in completePunchOut", e)
                showError("Punch out error: ${e.message}")
                resetPunchState()
            }
        }
    }

    /**
     * Reset punch state on error
     */
    private fun resetPunchState() {
        _uiState.value = _uiState.value.copy(
            isPunchingIn = false,
            isPunchingOut = false
        )
        currentLocation = null
        capturedPhoto = null
    }

    /**
     * Show error message with toast
     */
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
        currentLocation = null
        capturedPhoto = null
    }

    /**
     * Show warning message (but continue with flow)
     */
    private fun showWarning(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = AppMessage.Error(
                message = "⚠️ Warning\n$message",
                messageKey = "warning_message"
            ),
            showErrorToast = true
        )
    }

    /**
     * CRASH-SAFE distance calculation using Haversine formula
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return try {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            earthRadius * c
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating distance", e)
            Double.MAX_VALUE // Return large distance on error
        }
    }

    /**
     * CRASH-SAFE coordinate validation
     */
    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }

    /**
     * CRASH-SAFE shift parsing
     */
    private fun safeParseShifts(shiftsJson: String?): List<Shift> {
        return try {
            if (shiftsJson.isNullOrEmpty()) return emptyList()
            json.decodeFromString<List<Shift>>(shiftsJson)
        } catch (e: SerializationException) {
            Log.e(TAG, "Error parsing shifts JSON", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing shifts", e)
            emptyList()
        }
    }

    /**
     * CRASH-SAFE double parsing
     */
    private fun safeParseDouble(value: String?, defaultValue: Double, description: String): Double {
        return try {
            value?.toDoubleOrNull() ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing $description", e)
            defaultValue
        }
    }

    /**
     * CRASH-SAFE preference getter
     */
    private suspend fun <T> safeGetPreference(
        flow: Flow<T>,
        defaultValue: T,
        description: String
    ): T {
        return try {
            flow.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting preference: $description", e)
            defaultValue
        }
    }

    /**
     * CRASH-SAFE execution wrapper
     */
    private fun safeExecute(operationName: String, operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in $operationName", e)
            }
        }
    }

    /**
     * Handle initialization errors gracefully
     */
    private fun handleInitializationError(error: Exception) {
        Log.e(TAG, "Initialization error", error)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isLoginComplete = false,
            errorMessage = AppMessage.Error(
                message = "Failed to initialize app. Please restart.",
                messageKey = "init_error"
            ),
            showErrorToast = true
        )
    }

    fun onToastShown() {
        _uiState.value = _uiState.value.copy(
            showSuccessToast = false,
            showErrorToast = false
        )
    }

    fun onLogout() {
        viewModelScope.launch {
            try {
                logoutUseCase().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            try {
                                tokenManager.clearTokens()
                                preferenceStorage.clearAll()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error clearing data on logout", e)
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
            }
        }
    }

    fun refresh() {
        safeExecute("refresh") {
            checkAndRefreshShifts()
            checkCurrentAttendanceStatus()
        }
    }
}

data class CameraRequest(val requestCode: Int)

const val CAMERA_REQUEST_PUNCH_IN = 1001
const val CAMERA_REQUEST_PUNCH_OUT = 1002

data class DashboardUiState(
    val screenState: ScreenState? = null,
    val currentShift: Shift? = null,
    val allShifts: List<Shift> = emptyList(),
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