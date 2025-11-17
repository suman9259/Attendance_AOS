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
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true // Coerce invalid values to defaults
        encodeDefaults = true
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

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
        safeExecute("loadUserTheme") { loadUserTheme() }
        safeExecute("initializeApp") { initializeApp() }
    }


    fun onLocationPermissionGranted() {
        _shouldRequestLocation.value = true
    }

    private fun loadUserTheme() {
        viewModelScope.launch {
            try {
                MiniAppEntryPoint.superData?.getCurrentTheme()?.data?.let { theme ->
                    _isDarkMode.value = theme == "dark"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading theme", e)
                // Default to light mode on error
                _isDarkMode.value = false
            }
        }
    }

    /**
     * Initialize app - Load from DataStore first, then refresh if needed
     * CRASH-SAFE: Handles all exceptions gracefully
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Safely check login status
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

                    // Refresh shifts if needed (in background, catch exceptions)
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
            // Load user info with safe defaults
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

            // Load zone data with safe parsing
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

            // Load shifts with safe JSON parsing
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
                expectedLatitude = zoneLat,
                expectedLongitude = zoneLon,
                maxDistanceMeters = zoneRadius,
                isLoading = false,
                isLoginComplete = true,
                isCheckedIn = isCheckedIn
            )

            Log.d(TAG, "Successfully loaded cached data: user=$userName, shifts=${shifts.size}, checkedIn=$isCheckedIn")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached data, falling back to fresh login", e)
            // If cache loading fails, perform fresh login
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
            // Don't crash, just skip the refresh
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

                            // Save to DataStore with error handling
                            try {
                                val shiftsJson = json.encodeToString(shifts)
                                preferenceStorage.saveUserShifts(shiftsJson)
                            } catch (e: SerializationException) {
                                Log.e(TAG, "Failed to serialize shifts", e)
                            }

                            // Update UI
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
                        // Don't show error to user, just log it
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        } catch (e: CancellationException) {
            throw e // Always rethrow CancellationException
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

//                                // Save to DataStore with error handling
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
                                    expectedLatitude = safeParseDouble(zone?.zone_latitude, 0.0, "zone latitude"),
                                    expectedLongitude = safeParseDouble(zone?.zone_longitude, 0.0, "zone longitude"),
                                    maxDistanceMeters = safeParseDouble(zone?.zone_radius, MAX_DISTANCE_METERS, "zone radius"),
                                    isLoading = false,
                                    isLoginComplete = true,
                                    isCheckedIn = false
                                )

                                Log.d(TAG, "Login successful: ${loginData.full_name}")

//                                // Fetch latest attendance record (non-blocking)
//                                safeExecute("checkCurrentAttendanceStatus") {
//                                    checkCurrentAttendanceStatus()
//                                }
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
            // Save auth data
            tokenManager.saveJwtToken(loginData.token)
            preferenceStorage.setLoggedIn(true)
            preferenceStorage.saveUserName(loginData.full_name)
            preferenceStorage.saveProfileImageUrl(loginData.profile_image ?: "")

            // Save shifts with error handling
            try {
                val shiftsJson = json.encodeToString(loginData.shifts)
                preferenceStorage.saveUserShifts(shiftsJson)
            } catch (e: SerializationException) {
                Log.e(TAG, "Failed to serialize shifts", e)
            }

            // Save zone data
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
            // Don't rethrow - allow app to continue even if save fails
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

                                // Check if user is currently checked in
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
                            // Don't show error to user
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
     * Start punch in flow
     * CRASH-SAFE: Validates all data before proceeding
     */
    fun startPunchIn() {
        try {
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

            // Time Validation with safe parsing
            val currentTime = System.currentTimeMillis()
            val shiftStartTime = safeParseTimeToMillis(shiftRule.start_time)
            val gracePeriodMs = safeParseGracePeriod(shiftRule.grace_period_in)

            if (shiftStartTime == 0L) {
                showError("Invalid shift start time")
                return
            }

            if (currentTime < shiftStartTime - gracePeriodMs) {
                val minutesEarly = ((shiftStartTime - gracePeriodMs - currentTime) / (60 * 1000)).toInt()
                showError("Too early to punch in. You can punch in $minutesEarly minutes later")
                return
            }

            _uiState.value = _uiState.value.copy(isPunchingIn = true)
            _shouldRequestLocation.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error in startPunchIn", e)
            showError("Failed to start punch in: ${e.message}")
        }
    }

    fun onLocationReceived(latitude: Double, longitude: Double) {
        try {
            _shouldRequestLocation.value = false

            // Validate coordinates
            if (!isValidCoordinate(latitude, longitude)) {
                showError("Invalid location coordinates")
                return
            }

            val distance = calculateDistance(
                latitude,
                longitude,
                _uiState.value.expectedLatitude,
                _uiState.value.expectedLongitude
            )

            Log.d(TAG, "Distance: $distance meters")

            if (distance > _uiState.value.maxDistanceMeters) {
                showError("You are ${distance.toInt()}m away. Move closer (within ${_uiState.value.maxDistanceMeters.toInt()}m)")
                return
            }

            currentLocation = Pair(latitude, longitude)
            _shouldOpenCamera.value = CameraRequest(
                requestCode = if (isPunchInFlow) CAMERA_REQUEST_PUNCH_IN else CAMERA_REQUEST_PUNCH_OUT
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing location", e)
            showError("Location error: ${e.message}")
        }
    }

    fun onLocationError(error: String) {
        _shouldRequestLocation.value = false
        showError("Location error: $error")
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        try {
            _shouldOpenCamera.value = null
            capturedPhoto = bitmap

            if (isPunchInFlow) {
                completePunchIn()
            } else {
                completePunchOut()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing captured photo", e)
            showError("Photo error: ${e.message}")
        }
    }

    fun onCameraError(error: String) {
        _shouldOpenCamera.value = null
        showError("Camera error: $error")
    }

    private fun completePunchIn() {
        viewModelScope.launch {
            try {
                val photo = capturedPhoto
                if (photo == null) {
                    showError("Photo unavailable")
                    return@launch
                }

                val location = currentLocation
                if (location == null) {
                    showError("Location unavailable")
                    return@launch
                }

                val profileImageUrl = _uiState.value.profileImageUrl

                // Face comparison (optional if no profile image)
                if (!profileImageUrl.isNullOrEmpty()) {
                    try {
                        val faceResult = faceCompareUseCase(profileImageUrl, photo)
                        Log.d(TAG, "Face match: ${faceResult.accuracy}%")

                        if (!faceResult.isSame || faceResult.accuracy < FACE_MATCH_THRESHOLD) {
                            showError("Face verification failed (${faceResult.accuracy.toInt()}%)")
                            return@launch
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Face comparison error", e)
                        showError("Face verification error: ${e.message}")
                        return@launch
                    }
                }

                // Biometric (optional)
                try {
                    val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                    if (bioSuccess != true) {
                        showError("Biometric authentication failed")
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Biometric error", e)
                    showError("Biometric error: ${e.message}")
                    return@launch
                }

                // Check-in API
                checkInUseCase(location.first, location.second, 0).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            try {
                                preferenceStorage.setCheckedIn(true)

                                _uiState.value = _uiState.value.copy(
                                    isPunchingIn = false,
                                    successMessage = "Punch in successful!",
                                    showSuccessToast = true,
                                    isCheckedIn = true
                                )

                                currentLocation = null
                                capturedPhoto = null
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating UI after punch in", e)
                            }
                        }
                        is NetworkResult.Error -> {
                            showError("Check-in failed: ${result.error.message}")
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in completePunchIn", e)
                showError("Punch in error: ${e.message}")
            }
        }
    }

    fun startPunchOut() {
        try {
            isPunchInFlow = false
            currentLocation = null
            capturedPhoto = null
            _uiState.value = _uiState.value.copy(isPunchingOut = true)
            _shouldRequestLocation.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error in startPunchOut", e)
            showError("Failed to start punch out: ${e.message}")
        }
    }

    private fun completePunchOut() {
        viewModelScope.launch {
            try {
                val photo = capturedPhoto
                if (photo == null) {
                    showError("Photo unavailable")
                    return@launch
                }

                val location = currentLocation
                if (location == null) {
                    showError("Location unavailable")
                    return@launch
                }

                // Face & biometric (similar to punch in)
                val profileImageUrl = _uiState.value.profileImageUrl
                if (!profileImageUrl.isNullOrEmpty()) {
                    try {
                        val faceResult = faceCompareUseCase(profileImageUrl, photo)
                        if (!faceResult.isSame || faceResult.accuracy < FACE_MATCH_THRESHOLD) {
                            showError("Face verification failed")
                            return@launch
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Face comparison error", e)
                        showError("Face verification error: ${e.message}")
                        return@launch
                    }
                }

                try {
                    val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                    if (bioSuccess != true) {
                        showError("Biometric authentication failed")
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Biometric error", e)
                    showError("Biometric error: ${e.message}")
                    return@launch
                }

                checkOutUseCase(location.first, location.second, 0).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            try {
                                preferenceStorage.setCheckedIn(false)

                                _uiState.value = _uiState.value.copy(
                                    isPunchingOut = false,
                                    successMessage = "Punch out successful!",
                                    showSuccessToast = true,
                                    isCheckedIn = false
                                )

                                currentLocation = null
                                capturedPhoto = null
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating UI after punch out", e)
                            }
                        }
                        is NetworkResult.Error -> {
                            showError("Check-out failed: ${result.error.message}")
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in completePunchOut", e)
                showError("Punch out error: ${e.message}")
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
        currentLocation = null
        capturedPhoto = null
    }

    /**
     * CRASH-SAFE distance calculation
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return try {
            val earthRadius = 6371000.0
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
     * CRASH-SAFE time parsing
     */
    private fun safeParseTimeToMillis(timeString: String?): Long {
        return try {
            if (timeString.isNullOrEmpty()) return 0L

            val parts = timeString.split(":")
            if (parts.size < 2) return 0L

            val hours = parts[0].toLongOrNull() ?: 0L
            val minutes = parts[1].toLongOrNull() ?: 0L

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            calendar.timeInMillis + (hours * 60 * 60 * 1000) + (minutes * 60 * 1000)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time: $timeString", e)
            0L
        }
    }

    /**
     * CRASH-SAFE grace period parsing
     */
    private fun safeParseGracePeriod(gracePeriod: String?): Long {
        return try {
            val minutes = gracePeriod?.toIntOrNull() ?: 10
            minutes * 60 * 1000L
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing grace period", e)
            10 * 60 * 1000L // Default 10 minutes
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