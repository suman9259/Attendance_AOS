package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.core_logic.AbsherPosition
import sa.gov.moi.absherinterior.core_logic.AbsherResponse
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import javax.inject.Inject

sealed class AbsherUiState {
    object Idle : AbsherUiState()
    object Loading : AbsherUiState()
    data class Success(val userInfo: UserInfo) : AbsherUiState()
    data class Error(val message: String) : AbsherUiState()
    object NotInitialized : AbsherUiState()
}

data class AbsherUserData(
    // Basic Info
    val nationalId: String = "",
    val fullNameAr: String = "",
    val fullNameEn: String = "",
    val firstNameAr: String = "",
    val fatherNameAr: String = "",
    val grandFatherNameAr: String = "",
    val lastNameAr: String = "",
    val firstNameEn: String = "",
    val fatherNameEn: String = "",
    val grandFatherNameEn: String = "",
    val lastNameEn: String = "",

    // Personal Details
    val nationality: String = "",
    val gender: String = "",
    val bloodType: String = "",
    val maritalStatus: String = "",
    val birthDate: String = "",
    val birthDateHijri: String = "",
    val placeOfBirth: String = "",

    // Contact Info
    val mobile: String = "",
    val workPhone: String = "",
    val email: String = "",

    // Employment Info
    val userRankID: String = "",
    val userRankCode: String = "",
    val userRank: String = "",
    val userRankDate: String = "",
    val basicSalary: String = "",
    val hireDate: String = "",
    val governmentHireDateRank: String = "",
    val employeeType: String = "",
    val sector: String = "",
    val department: String = "",

    // App Data
    val token: String = "",
    val currentTheme: String = "",
    val currentLanguage: String = "",
    val profileImage: String = ""
)

@HiltViewModel
class AbsherViewModel @Inject constructor(
    private val absherHelper: IAbsherHelper?,
    private val absherRepository: AbsherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AbsherUiState>(AbsherUiState.Idle)
    val uiState: StateFlow<AbsherUiState> = _uiState.asStateFlow()

    private val _userData = MutableStateFlow(AbsherUserData())
    val userData: StateFlow<AbsherUserData> = _userData.asStateFlow()

    private val _isAbsherInitialized = MutableStateFlow(false)
    val isAbsherInitialized: StateFlow<Boolean> = _isAbsherInitialized.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _locationData = MutableStateFlow<AbsherPosition?>(null)
    val locationData: StateFlow<AbsherPosition?> = _locationData.asStateFlow()

    private val _isLocationAuthenticated = MutableStateFlow(false)
    val isLocationAuthenticated: StateFlow<Boolean> = _isLocationAuthenticated.asStateFlow()

    private val _serviceTitleAr = MutableStateFlow("")
    val serviceTitleAr: StateFlow<String> = _serviceTitleAr.asStateFlow()

    private val _serviceTitleEn = MutableStateFlow("")
    val serviceTitleEn: StateFlow<String> = _serviceTitleEn.asStateFlow()

    init {
        checkAbsherInitialization()
    }

    private fun checkAbsherInitialization() {
        _isAbsherInitialized.value = absherHelper != null
        if (absherHelper != null) {
            loadServiceTitles()
        }
    }

    // ============================================================
    // SECTION 1: USER DATA METHODS
    // ============================================================

    /**
     * Load complete user information from Absher SDK
     */
    fun loadUserInfo() {
        viewModelScope.launch {
            if (absherHelper == null) {
                _uiState.value = AbsherUiState.NotInitialized
                return@launch
            }

            _uiState.value = AbsherUiState.Loading

            try {
                absherRepository.getUserInfo().collect { result ->
                    result.fold(
                        onSuccess = { userInfo ->
                            _uiState.value = AbsherUiState.Success(userInfo)
                        },
                        onFailure = { exception ->
                            _uiState.value = AbsherUiState.Error(
                                exception.message ?: "Unknown error"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AbsherUiState.Error(e.message ?: "Failed to load user info")
            }
        }
    }

    /**
     * Load all user data including employment details
     */
    fun loadCompleteUserData() {
        viewModelScope.launch {
            if (absherHelper == null) {
                _errorMessage.value = "Absher SDK not initialized"
                return@launch
            }

            try {
                val userData = AbsherUserData(
                    // Basic Info
                    nationalId = getUserNationalID(),
                    fullNameAr = getUserFullNameAr(),
                    fullNameEn = getUserFullNameEn(),
                    firstNameAr = getUserFirstNameAr(),
                    fatherNameAr = getUserFatherNameAr(),
                    grandFatherNameAr = getUserGrandFatherNameAr(),
                    lastNameAr = getUserLastNameAr(),
                    firstNameEn = getUserFirstNameEn(),
                    fatherNameEn = getUserFatherNameEn(),
                    grandFatherNameEn = getUserGrandFatherNameEn(),
                    lastNameEn = getUserLastNameEn(),

                    // Personal Details
                    nationality = getUserNationality(),
                    gender = getUserGender(),
                    bloodType = getUserBloodType(),
                    maritalStatus = getUserMaritalStatus(),
                    birthDate = getUserBirthDate(),
                    birthDateHijri = getUserBirthDateHijri(),
                    placeOfBirth = getUserPlaceOfBirth(),

                    // Contact Info
                    mobile = getUserMobile(),
                    workPhone = getUserWorkPhone(),
                    email = getUserEmail(),

                    // Employment Info
                    userRankID = getUserRankID(),
                    userRankCode = getUserRankCode(),
                    userRank = getUserRank(),
                    userRankDate = getUserRankDate(),
                    basicSalary = getUserBasicSalary(),
                    hireDate = getUserHireDate(),
                    governmentHireDateRank = getUserGovernmentHireDateRank(),
                    employeeType = getEmployeeType(),
                    sector = getUserSector(),
                    department = getUserDepartment(),

                    // App Data
                    token = getUserToken(),
                    currentTheme = getCurrentTheme(),
                    currentLanguage = getCurrentLanguage(),
                    profileImage = getUserProfileImage()
                )

                _userData.value = userData
                _successMessage.value = "User data loaded successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load complete user data: ${e.message}"
            }
        }
    }

    // ============================================================
    // BASIC USER INFO GETTERS
    // ============================================================

    fun getUserNationalID(): String {
        return absherHelper?.getUserNationalID()?.getDataOrDefault("") ?: ""
    }

    fun getUserFullNameAr(): String {
        return absherHelper?.getUserFullNameAr()?.getDataOrDefault("") ?: ""
    }

    fun getUserFullNameEn(): String {
        return absherHelper?.getUserFullNameEn()?.getDataOrDefault("") ?: ""
    }

    fun getUserFirstNameAr(): String {
        return absherHelper?.getUserFirstNameAr()?.getDataOrDefault("") ?: ""
    }

    fun getUserFatherNameAr(): String {
        return absherHelper?.getUserFatherNameAr()?.getDataOrDefault("") ?: ""
    }

    fun getUserGrandFatherNameAr(): String {
        return absherHelper?.getUserGrandFatherNameAr()?.getDataOrDefault("") ?: ""
    }

    fun getUserLastNameAr(): String {
        return absherHelper?.getUserLastNameAr()?.getDataOrDefault("") ?: ""
    }

    fun getUserFirstNameEn(): String {
        return absherHelper?.getUserFirstNameEn()?.getDataOrDefault("") ?: ""
    }

    fun getUserFatherNameEn(): String {
        return absherHelper?.getUserFatherNameEn()?.getDataOrDefault("") ?: ""
    }

    fun getUserGrandFatherNameEn(): String {
        return absherHelper?.getUserGrandFatherNameEn()?.getDataOrDefault("") ?: ""
    }

    fun getUserLastNameEn(): String {
        return absherHelper?.getUserLastNameEn()?.getDataOrDefault("") ?: ""
    }

    // ============================================================
    // PERSONAL DETAILS GETTERS
    // ============================================================

    fun getUserNationality(): String {
        return absherHelper?.getUserNationality()?.getDataOrDefault("") ?: ""
    }

    fun getUserGender(): String {
        return absherHelper?.getUserGender()?.getDataOrDefault("") ?: ""
    }

    fun getUserBloodType(): String {
        return absherHelper?.getUserBloodType()?.getDataOrDefault("") ?: ""
    }

    fun getUserMaritalStatus(): String {
        return absherHelper?.getUserMaritalStatus()?.getDataOrDefault("") ?: ""
    }

    fun getUserBirthDate(): String {
        return absherHelper?.getUserBirthDate()?.getDataOrDefault("") ?: ""
    }

    fun getUserBirthDateHijri(): String {
        return absherHelper?.getUserBirthDateHijri()?.getDataOrDefault("") ?: ""
    }

    fun getUserPlaceOfBirth(): String {
        return absherHelper?.getUserPlaceOfBirth()?.getDataOrDefault("") ?: ""
    }

    // ============================================================
    // CONTACT INFO GETTERS
    // ============================================================

    fun getUserMobile(): String {
        return absherHelper?.getUserMobile()?.getDataOrDefault("") ?: ""
    }

    fun getUserWorkPhone(): String {
        return absherHelper?.getUserWorkPhone()?.getDataOrDefault("") ?: ""
    }

    fun getUserEmail(): String {
        return absherHelper?.getUserEmail()?.getDataOrDefault("") ?: ""
    }

    // ============================================================
    // EMPLOYMENT INFO GETTERS
    // ============================================================

    fun getUserRankID(): String {
        return absherHelper?.getUserRankID()?.getDataOrDefault("") ?: ""
    }

    fun getUserRankCode(): String {
        return absherHelper?.getUserRankCode()?.getDataOrDefault("") ?: ""
    }

    fun getUserRank(): String {
        return absherHelper?.getUserRank()?.getDataOrDefault("") ?: ""
    }

    fun getUserRankDate(): String {
        return absherHelper?.getUserRankDate()?.getDataOrDefault("") ?: ""
    }

    fun getUserBasicSalary(): String {
        return absherHelper?.getUserBasicSalary()?.getDataOrDefault("") ?: ""
    }

    fun getUserHireDate(): String {
        return absherHelper?.getUserHireDate()?.getDataOrDefault("") ?: ""
    }

    fun getUserGovernmentHireDateRank(): String {
        return absherHelper?.getUserGovernmentHireDateRank()?.getDataOrDefault("") ?: ""
    }

    fun getEmployeeType(): String {
        return absherHelper?.getEmployeeType()?.getDataOrDefault("") ?: ""
    }

    fun getUserSector(): String {
        return absherHelper?.getUserSector()?.getDataOrDefault("") ?: ""
    }

    fun getUserDepartment(): String {
        return absherHelper?.getUserDepartment()?.getDataOrDefault("") ?: ""
    }

    // ============================================================
    // SECTION 2: APP DATA METHODS
    // ============================================================

    fun getUserToken(): String {
        return absherHelper?.getUserToken()?.getDataOrDefault("") ?: ""
    }

    fun getCurrentTheme(): String {
        return absherHelper?.getCurrentTheme()?.getDataOrDefault("light") ?: "light"
    }

    fun getCurrentLanguage(): String {
        return absherHelper?.getCurrentLanguage()?.getDataOrDefault("en") ?: "en"
    }

    fun getUserProfileImage(): String {
        return absherHelper?.getUserProfileImage()?.getDataOrDefault("") ?: ""
    }

    private fun loadServiceTitles() {
        viewModelScope.launch {
            _serviceTitleAr.value = absherHelper?.getServiceTitleAr()?.getDataOrDefault("") ?: ""
            _serviceTitleEn.value = absherHelper?.getServiceTitleEn()?.getDataOrDefault("") ?: ""
        }
    }

    // ============================================================
    // SECTION 3: LOCAL STORAGE METHODS
    // ============================================================

    /**
     * Save string value to local storage
     */
    fun saveStringToLocal(key: String, value: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.saveStringToLocal(key, value)?.getDataOrDefault(false) ?: false
                onResult(result)
                if (result) {
                    _successMessage.value = "Data saved successfully"
                } else {
                    _errorMessage.value = "Failed to save data"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error saving data: ${e.message}"
                onResult(false)
            }
        }
    }

    /**
     * Read string value from local storage
     */
    fun readStringFromLocal(key: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.readStringFromLocal(key)?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Error reading data: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Save boolean value to local storage
     */
    fun saveBoolToLocal(key: String, value: Boolean, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.saveBoolToLocal(key, value)?.getDataOrDefault(false) ?: false
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Error saving boolean: ${e.message}"
                onResult(false)
            }
        }
    }

    /**
     * Read boolean value from local storage
     */
    fun readBoolFromLocal(key: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.readBoolFromLocal(key)?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Error reading boolean: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Save integer value to local storage
     */
    fun saveIntToLocal(key: String, value: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.saveIntToLocal(key, value)?.getDataOrDefault(false) ?: false
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Error saving integer: ${e.message}"
                onResult(false)
            }
        }
    }

    /**
     * Read integer value from local storage
     */
    fun readIntFromLocal(key: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.readIntFromLocal(key)?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Error reading integer: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Delete data from local storage
     */
    fun deleteDataFromLocal(key: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.deleteDataFromLocal(key)?.getDataOrDefault(false) ?: false
                onResult(result)
                if (result) {
                    _successMessage.value = "Data deleted successfully"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting data: ${e.message}"
                onResult(false)
            }
        }
    }

    // ============================================================
    // SECTION 4: NATIVE FEATURES - MEDIA
    // ============================================================

    /**
     * Get image from camera
     */
    fun getImageFromCamera(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getImageFromCamera()?.getDataOrDefault("")
                onResult(result)
                if (result.isNullOrEmpty()) {
                    _errorMessage.value = "Failed to capture image"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Camera error: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Get image from gallery
     */
    fun getImageFromGallery(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getImageFromGallery()?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Gallery error: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Get multiple images from gallery
     */
    fun getImagesFromGallery(onResult: (List<String>?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getImagesFromGallery()?.getDataOrDefault(emptyList())
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Gallery error: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Get video from camera
     */
    fun getVideoFromCamera(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getVideoFromCamera()?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Video camera error: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Get video from gallery
     */
    fun getVideoFromGallery(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getVideoFromGallery()?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Video gallery error: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Get multiple videos from gallery
     */
    fun getVideosFromGallery(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getVideosFromGallery()?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Videos gallery error: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Get file from device
     */
    fun getFile(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getFile()?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "File picker error: ${e.message}"
                onResult(null)
            }
        }
    }

    // ============================================================
    // SECTION 5: LOCATION FEATURES
    // ============================================================

    /**
     * Get precise location from device
     */
    fun getPreciseLocation(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getPreciseLocation()?.getDataOrDefault("")
                onResult(result)
            } catch (e: Exception) {
                _errorMessage.value = "Location error: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Get location with AbsherPosition object
     */
    fun getLocation() {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getLocation()?.getDataOrDefault(AbsherPosition(
                    latitude = 0.0,
                    longitude = 0.0,
                ))
                _locationData.value = result
                if (result == null) {
                    _errorMessage.value = "Failed to get location"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Location error: ${e.message}"
            }
        }
    }

    /**
     * Check if location is authenticated
     */
    fun checkLocationAuthentication() {
        viewModelScope.launch {
            try {
                val result = absherHelper?.getIsLocationAuthenticated()?.getDataOrDefault(false) ?: false
                _isLocationAuthenticated.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Location auth check error: ${e.message}"
            }
        }
    }

    // ============================================================
    // SECTION 6: SECURITY FEATURES
    // ============================================================

    /**
     * Authenticate using biometric
     */
    fun authenticateBiometric(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.authenticateBiometric()?.getDataOrDefault(false) ?: false
                onResult(result)
                if (result) {
                    _successMessage.value = "Biometric authentication successful"
                } else {
                    _errorMessage.value = "Biometric authentication failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Biometric error: ${e.message}"
                onResult(false)
            }
        }
    }

    // ============================================================
    // SECTION 7: SHARING FEATURES
    // ============================================================

    /**
     * Share text content
     */
    fun shareText(text: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.shareText(text)?.getDataOrDefault(false) ?: false
                onResult(result)
                if (!result) {
                    _errorMessage.value = "Failed to share text"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Share error: ${e.message}"
                onResult(false)
            }
        }
    }

    /**
     * Share media file
     */
    fun shareMedia(filePath: String, message: String? = null, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = absherHelper?.shareMedia(filePath, message)?.getDataOrDefault(false) ?: false
                onResult(result)
                if (!result) {
                    _errorMessage.value = "Failed to share media"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Share media error: ${e.message}"
                onResult(false)
            }
        }
    }

    // ============================================================
    // SECTION 8: APP CONTROL
    // ============================================================

    /**
     * Close the mini app and return data to Super App
     */
    fun closeApp(data: Map<String, Any>? = null) {
        try {
            absherHelper?.closeApp(data)
        } catch (e: Exception) {
            _errorMessage.value = "Close app error: ${e.message}"
        }
    }

    /**
     * Close app with attendance data
     */
    fun closeAppWithAttendanceData(
        totalHours: String,
        presentDays: Int,
        lateDays: Int,
        absentDays: Int
    ) {
        val data = mapOf(
            "total_hours" to totalHours,
            "present_days" to presentDays,
            "late_days" to lateDays,
            "absent_days" to absentDays,
            "timestamp" to System.currentTimeMillis()
        )
        closeApp(data)
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    fun dismissError() {
        _errorMessage.value = null
    }

    fun dismissSuccess() {
        _successMessage.value = null
    }

    fun refreshUserData() {
        loadUserInfo()
        loadCompleteUserData()
    }

    /**
     * Get formatted user display name based on language
     */
    fun getDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            getUserFullNameAr()
        } else {
            getUserFullNameEn()
        }
    }

    /**
     * Check if user has complete profile data
     */
    fun hasCompleteProfile(): Boolean {
        return getUserNationalID().isNotEmpty() &&
                getUserFullNameAr().isNotEmpty() &&
                getUserFullNameEn().isNotEmpty()
    }

    /**
     * Export user data as JSON string
     */
    fun exportUserDataAsJson(): String {
        val data = _userData.value
        return """
            {
                "nationalId": "${data.nationalId}",
                "fullNameEn": "${data.fullNameEn}",
                "fullNameAr": "${data.fullNameAr}",
                "email": "${data.email}",
                "mobile": "${data.mobile}",
                "department": "${data.department}",
                "rank": "${data.userRank}",
                "employeeType": "${data.employeeType}"
            }
        """.trimIndent()
    }
}

// ============================================================
// EXTENSION FUNCTIONS
// ============================================================

/**
 * Extension function to safely get data from AbsherResponse with default value
 */
private fun <T> AbsherResponse<T>.getDataOrDefault(default: T): T {
    return if (this.success && this.data != null) {
        this.data!!
    } else {
        default
    }
}

/**
 * Extension function to get data or throw exception
 */
private fun <T> AbsherResponse<T>.getDataOrThrow(): T {
    return when {
        this.success && this.data != null -> this.data!!
        else -> throw Exception(this.message ?: "Unknown error from Absher SDK")
    }
}