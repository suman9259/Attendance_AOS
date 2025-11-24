package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.core.network.TokenManager
import com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceRecord
import com.scharfesicht.attendencesystem.features.attendance.domain.model.LoginData
import com.scharfesicht.attendencesystem.features.attendance.domain.model.Shift
import com.scharfesicht.attendencesystem.features.attendance.domain.model.ShiftRule
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.CheckInUseCase
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.CheckOutUseCase
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.GetLatestRecordUseCase
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.GetUserShiftsUseCase
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.LoginUseCase
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.LogoutUseCase
import com.scharfesicht.attendencesystem.features.facecompare.domain.FaceCompareUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.utils.ScreenState
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/* ---------- PUNCH FLOW TYPES ---------- */

enum class PunchType { IN, OUT }

sealed class PunchFlowState {
    object Idle : PunchFlowState()
    object WaitingForLocation : PunchFlowState()
    object WaitingForCamera : PunchFlowState()
    object ValidatingFace : PunchFlowState()
    object ValidatingBiometric : PunchFlowState()
    object PunchingIn : PunchFlowState()
    object PunchingOut : PunchFlowState()
    data class Error(val message: String) : PunchFlowState()
}

data class PunchSession(
    val type: PunchType,
    val location: Pair<Double, Double>? = null,
    val bitmap: Bitmap? = null
)

/* ---------- VIEWMODEL ---------- */

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

    // UI state
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Tabs
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Punch flow state for Compose
    private val _flowState = MutableStateFlow<PunchFlowState>(PunchFlowState.Idle)
    val flowState: StateFlow<PunchFlowState> = _flowState.asStateFlow()

    // Current punch session
    private val _session = MutableStateFlow<PunchSession?>(null)

    private val _toast = MutableSharedFlow<String>()
    val toast = _toast

    // Debounce
    private var lastPunchInClick = 0L
    private var lastPunchOutClick = 0L


    init {
        safeExecute("initializeApp") { initializeApp() }
    }

    /* ---------- INITIALIZATION ---------- */

    private fun initializeApp() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

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
                        checkCurrentAttendanceStatus()
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
            val zoneId = safeGetPreference(preferenceStorage.zoneId, 0, "zoneRadius")

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

            _uiState.update {
                it.copy(
                    currentShift = currentShift,
                    allShifts = shifts,
                    profileImageUrl = profileImageUrl,
                    userName = userName,
                    zoneId = zoneId,
                    expectedLatitude = zoneLat,
                    expectedLongitude = zoneLon,
                    maxDistanceMeters = zoneRadius,
                    isLoading = false,
                    isLoginComplete = true,
                    isCheckedIn = isCheckedIn
                )
            }

            Log.d(
                TAG,
                "Successfully loaded cached data: user=$userName, shifts=${shifts.size}, checkedIn=$isCheckedIn"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached data, falling back to fresh login", e)
            performLogin()
        }
    }

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

                            _uiState.update {
                                it.copy(
                                    allShifts = shifts,
                                    currentShift = shifts.firstOrNull()
                                )
                            }

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
                            _uiState.update {
                                it.copy(
                                    isLoading = true,
                                    isLoginComplete = false,
                                    errorMessage = null
                                )
                            }
                        }

                        is NetworkResult.Success -> {
                            try {
                                val loginData = result.data

                                safeExecute("saveLoginData") {
                                    saveLoginData(loginData)
                                }

                                val currentShift = loginData.shifts.firstOrNull()
                                val zone = loginData.zones.firstOrNull()

                                _uiState.update {
                                    it.copy(
                                        currentShift = currentShift,
                                        allShifts = loginData.shifts,
                                        profileImageUrl = loginData.profile_image,
                                        userName = loginData.full_name,
                                        expectedLatitude = safeParseDouble(zone?.zone_latitude, 0.0, "zone latitude"),
                                        expectedLongitude = safeParseDouble(zone?.zone_longitude, 0.0, "zone longitude"),
                                        zoneId = zone?.id ?: 0,
                                        maxDistanceMeters = safeParseDouble(
                                            zone?.zone_radius,
                                            MAX_DISTANCE_METERS,
                                            "zone radius"
                                        ) * 1000,
                                        isLoading = false,
                                        isLoginComplete = true,
                                        isCheckedIn = false
                                    )
                                }

                                Log.d(TAG, "Login successful: ${loginData.full_name}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing login data", e)
                                showError("Error processing login data: ${e.message}")
                            }
                        }

                        is NetworkResult.Error -> {
                            val errorMsg = result.error.message ?: "Unknown login error"
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isLoginComplete = false,
                                    errorMessage = AppMessage.Error(
                                        message = errorMsg,
                                        messageKey = "error_message"
                                    ),
                                    showErrorToast = true
                                )
                            }
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
                    val lat = safeParseDouble(zone.zone_latitude, 0.0, "zone latitude")
                    val lon = safeParseDouble(zone.zone_longitude, 0.0, "zone longitude")
                    val radiusMeters = safeParseDouble(zone.zone_radius, 0.0, "zone radius") * 1000

                    preferenceStorage.saveZoneData(
                        latitude = lat.toString(),
                        longitude = lon.toString(),
                        radius = radiusMeters.toString(),
                        name = zone.zone_name_lang,
                        zoneId = zone.id
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
                                _uiState.update { it.copy(isCheckedIn = isCheckedIn) }

                                Log.d(TAG, "Attendance status: checkedIn=$isCheckedIn")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing attendance records", e)
                            }
                        }

                        is NetworkResult.Error -> {
                            Log.e(
                                TAG,
                                "Failed to get attendance status: ${result.error.message}"
                            )
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

    /* ---------- PUBLIC UI API ---------- */

    fun onTabChanged(tab: Int) {
        _selectedTab.value = tab
    }

    fun startPunchIn() = startPunch(PunchType.IN)

    fun startPunchOut() = startPunch(PunchType.OUT)

    fun onLocationReceived(latitude: Double, longitude: Double) {
        handleLocation(latitude, longitude)
    }

    fun onLocationError(error: String) {
        showError("Location error: $error\n\nPlease enable location services and grant permission.")
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        handlePhoto(bitmap)
    }

    fun onCameraError(error: String) {
        showError("Camera error: $error\n\nPlease grant camera permission and try again.")
    }

    fun onToastShown() {
        viewModelScope.launch {
            delay(2000)
//            resetFlow()
            _uiState.update {
                it.copy(
                    showSuccessToast = false,
                    showErrorToast = false,
                    successMessage = null,
                    errorMessage = null
                )
            }
        }
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
                                _uiState.value = DashboardUiState()
                                _flowState.value = PunchFlowState.Idle
                                _session.value = null
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

    /* ---------- PUNCH FLOW (FLOW-BASED) ---------- */

    private fun startPunch(type: PunchType) {
        try {

            val now = System.currentTimeMillis()
            if (type == PunchType.IN) {
                if (now - lastPunchInClick < CLICK_DEBOUNCE_MS) return
                lastPunchInClick = now
            } else {
                if (now - lastPunchOutClick < CLICK_DEBOUNCE_MS) return
                lastPunchOutClick = now
            }
            _uiState.update { it.copy(
                isLoading = true,
                successMessage = "Checking your location.",
            ) }

            val state = _uiState.value
            val currentShift = state.currentShift
            val shiftRule = currentShift?.shift_rule?.firstOrNull()

            if (shiftRule == null) {
                showError("No shift assigned. Please contact your administrator.")
                return
            }

            if (type == PunchType.IN && state.isCheckedIn) {
                showError("You are already punched in. Please punch out first.")
                return
            }

            if (type == PunchType.OUT && !state.isCheckedIn) {
                showError("You are not punched in. Please punch in first.")
                return
            }
            _uiState.update { it.copy(
                isLoading = true,
                successMessage = "Checking your shift timing."
            ) }
            // Time validation
            if (!validateTime(type, shiftRule)) return

            // Start flow
            _session.value = PunchSession(type = type)

            _uiState.update { it.copy(
                isLoading = true,
                successMessage = "Checking your Current Location."
            ) }
            _flowState.value = PunchFlowState.WaitingForLocation

            _uiState.update {
                it.copy(
                    isPunchingIn = (type == PunchType.IN),
                    isPunchingOut = (type == PunchType.OUT)
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in startPunch", e)
            showError("Failed to start punch: ${e.message}")
        }
    }

    private fun validateTime(type: PunchType, shiftRule: ShiftRule): Boolean {
        val currentTime = Calendar.getInstance()
        val shiftStartTime = parseShiftTime(shiftRule.start_time)
        val shiftEndTime = parseShiftTime(shiftRule.end_time)

        return when (type) {
            PunchType.IN -> {
                if (shiftStartTime == null) {
                    showError("Invalid shift start time. Please contact your administrator.")
                    false
                } else {
                    val timeDifferenceMinutes =
                        calculateTimeDifferenceMinutes(currentTime, shiftStartTime)
                    val gracePeriodMinutes = shiftRule.grace_period_in.toIntOrNull() ?: 10

                    Log.d(
                        TAG,
                        "Time validation: current vs shift = $timeDifferenceMinutes minutes, grace period = $gracePeriodMinutes minutes"
                    )

                    if (timeDifferenceMinutes < -gracePeriodMinutes) {
                        val minutesUntilAllowed = abs(timeDifferenceMinutes) - gracePeriodMinutes
                        showError(
                            "Too early to punch in.\n" +
                                    "You can punch in after $minutesUntilAllowed minutes.\n" +
                                    "Shift starts at: ${shiftRule.start_time}"
                        )
                        false
                    } else {
                        if (timeDifferenceMinutes > MAX_LATE_MINUTES_WARNING) {
                            showWarning(
                                "You are ${timeDifferenceMinutes} minutes late.\n" +
                                        "Please contact your supervisor if needed."
                            )
                        }
                        true
                    }
                }
            }

            PunchType.OUT -> {
                if (shiftEndTime != null) {
                    val timeDifferenceMinutes =
                        calculateTimeDifferenceMinutes(currentTime, shiftEndTime)
                    if (timeDifferenceMinutes < 0) {
                        val minutesEarly = abs(timeDifferenceMinutes)
                        showWarning(
                            "You are punching out $minutesEarly minutes early.\n" +
                                    "Shift ends at: ${shiftRule.end_time}\n" +
                                    "Your supervisor will be notified."
                        )
                    }
                }
                true
            }
        }
    }

    private fun handleLocation(latitude: Double, longitude: Double) {
        try {
            if (!isValidCoordinate(latitude, longitude)) {
                showError("Invalid location coordinates received. Please try again.")
                return
            }

            val expectedLat = _uiState.value.expectedLatitude
            val expectedLon = _uiState.value.expectedLongitude
            val maxDistance = _uiState.value.maxDistanceMeters

            val distance = calculateDistance(
                latitude,
                longitude,
                expectedLat,
                expectedLon
            )

            Log.d(
                TAG,
                "✓ Location received: distance = ${distance.toInt()}m, max allowed = ${maxDistance.toInt()}m"
            )

            if (distance > maxDistance) {
                showError(
                    "Location validation failed!\n\n" +
                            "You are ${distance.toInt()} meters away from the designated location.\n\n" +
                            "Please move closer (within ${maxDistance.toInt()} meters) and try again."
                )
                return
            }

            _session.update {
                it?.copy(location = latitude to longitude)
            }
            _uiState.update { it.copy(
                isLoading = true,
                successMessage = "Checking your face."
            ) }
            _flowState.value = PunchFlowState.WaitingForCamera

        } catch (e: Exception) {
            Log.e(TAG, "Error processing location", e)
            showError("Location error: ${e.message}")
        }
    }

    private fun handlePhoto(bitmap: Bitmap) {
        try {
            _uiState.update { it.copy(
                isLoading = true,
                successMessage = "Checking your face."
            ) }
            _session.update { it?.copy(bitmap = bitmap) }
            Log.d(TAG, "✓ Photo captured: ${bitmap.width}x${bitmap.height}")
            _flowState.value = PunchFlowState.ValidatingFace
            validateFace()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing captured photo", e)
            showError("Photo error: ${e.message}")
        }
    }

    private fun validateFace() {
        viewModelScope.launch {
            try {
                val session = _session.value
                val photo = session?.bitmap
                if (photo == null) {
                    showError("Photo unavailable. Please try again.")
                    return@launch
                }

                val profileImageUrl = _uiState.value.profileImageUrl

                if (!profileImageUrl.isNullOrEmpty()) {
                    Log.d(TAG, "Starting face comparison...")
                    val faceResult = faceCompareUseCase(profileImageUrl, photo)
                    Log.d(
                        TAG,
                        "✓ Face comparison result: match=${faceResult.isSame}, accuracy=${faceResult.accuracy}%"
                    )

                    if (!faceResult.isSame || faceResult.accuracy < FACE_MATCH_THRESHOLD) {
                        showError(
                            "Face verification failed!\n\n" +
                                    "Match accuracy: ${faceResult.accuracy.toInt()}%\n" +
                                    "Required: ${FACE_MATCH_THRESHOLD.toInt()}%\n\n" +
                                    "Please try again with better lighting."
                        )
                        return@launch
                    }
                } else {
                    Log.d(TAG, "⚠ Skipping face comparison (no profile image)")
                }

                _flowState.value = PunchFlowState.ValidatingBiometric
                validateBiometric()

            } catch (e: Exception) {
                Log.e(TAG, "Face comparison error", e)
                showError("Face verification error: ${e.message}")
            }
        }
    }

    private fun validateBiometric() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting biometric authentication...")
                val bioSuccess = MiniAppEntryPoint.superData?.authenticateBiometric()?.data
                if (bioSuccess != true) {
                    showError("Biometric authentication failed.\n\nPlease authenticate and try again.")
                    return@launch
                }
                Log.d(TAG, "✓ Biometric authentication successful")
                completePunch()
            } catch (e: Exception) {
                Log.e(TAG, "Biometric error", e)
                showError("Biometric error: ${e.message}")
            }
        }
    }

    private fun completePunch() {
        val session = _session.value
        val location = session?.location
        val currentShift = _uiState.value.currentShift
        val zoneId = _uiState.value.zoneId ?: 0

        if (session == null || location == null) {
            showError("Internal error. Please try again.")
            return
        }

        when (session.type) {
            PunchType.IN -> {
                _flowState.value = PunchFlowState.PunchingIn
                viewModelScope.launch {
                    try {// TODO: Check Shift_id Parameter.
                        checkInUseCase(shift_id = currentShift?.shift_type ?: 1,location.first, location.second, zoneId).collect { result ->
                            handlePunchResult(result, isIn = true)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Unexpected error in completePunch (IN)", e)
                        showError("Punch in error: ${e.message}")
                    }
                }
            }

            PunchType.OUT -> {
                _flowState.value = PunchFlowState.PunchingOut
                viewModelScope.launch {
                    try {
                        checkOutUseCase(location.first, location.second, zoneId).collect { result ->
                            handlePunchResult(result, isIn = false)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Unexpected error in completePunch (OUT)", e)
                        showError("Punch out error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun handlePunchResult(result: NetworkResult<AttendanceRecord>, isIn: Boolean) {
        when (result) {
            is NetworkResult.Success -> {

                viewModelScope.launch {
                    _toast.emit(
                        if (isIn) "Successfully punched in"
                        else "Successfully punched out"
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCheckedIn = isIn,
                        isPunchingIn = false,
                        isPunchingOut = false
                    )
                }
                viewModelScope.launch { preferenceStorage.setCheckedIn(isIn) }
//                resetFlow()
            }

            is NetworkResult.Error -> {
                showError(result.error.message ?: "Punch operation failed")
            }

            else -> Unit
        }
    }

    /* ---------- HELPERS ---------- */

    private fun resetFlow() {
        _session.value = null
        _flowState.value = PunchFlowState.Idle
        _uiState.update {
            it.copy(
                isPunchingIn = false,
                isPunchingOut = false
            )
        }
    }

    private fun showError(message: String) {
        _flowState.value = PunchFlowState.Error(message)
           _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = AppMessage.Error(
                    message = message,
                    messageKey = "error_message"
                ),
                showErrorToast = true,
                isPunchingIn = false,
                isPunchingOut = false
            )
        }
    }

    private fun showWarning(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = AppMessage.Error(
                    message = "⚠️ Warning\n$message",
                    messageKey = "warning_message"
                ),
                showErrorToast = true
            )
        }
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

    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }

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

    private fun safeParseDouble(value: String?, defaultValue: Double, description: String): Double {
        return try {
            value?.toDoubleOrNull() ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing $description", e)
            defaultValue
        }
    }

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

    private fun handleInitializationError(error: Exception) {
        Log.e(TAG, "Initialization error", error)
        _uiState.update {
            it.copy(
                isLoading = false,
                isLoginComplete = false,
                errorMessage = AppMessage.Error(
                    message = "Failed to initialize app. Please restart.",
                    messageKey = "init_error"
                ),
                showErrorToast = true
            )
        }
    }

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

    private fun calculateTimeDifferenceMinutes(
        currentTime: Calendar,
        shiftTime: Calendar
    ): Int {
        val diffMillis = currentTime.timeInMillis - shiftTime.timeInMillis
        return (diffMillis / (60 * 1000)).toInt()
    }
}

/* ---------- UI STATE ---------- */

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
    val showErrorToast: Boolean = false,
    val zoneId: Int? = null
)
