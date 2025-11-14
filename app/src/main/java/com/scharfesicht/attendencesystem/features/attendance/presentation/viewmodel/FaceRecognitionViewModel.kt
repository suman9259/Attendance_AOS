package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.utils.ScreenState
import javax.inject.Inject

@HiltViewModel
class FaceRecognitionViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FaceRecognitionUiState())
    val uiState: StateFlow<FaceRecognitionUiState> = _uiState

    private val _recognitionState = MutableStateFlow(RecognitionState.SCANNING)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState

    init {
        _uiState.value = _uiState.value.copy(screenState = ScreenState.Success(true))
    }

    fun onAuthenticationSuccess() {
        viewModelScope.launch {
            // Simulate processing
            delay(1000)
            _recognitionState.value = RecognitionState.SUCCESS

            // Navigate back after success
            delay(2000)
            // Navigation would be handled by the screen
        }
    }

    fun onAuthenticationFailed() {
        viewModelScope.launch {
            delay(500)
            _recognitionState.value = RecognitionState.FAILED
        }
    }

    fun onTryAgain() {
        _recognitionState.value = RecognitionState.SCANNING
        // Trigger biometric again - this would be handled by the screen
    }
}

data class FaceRecognitionUiState(
    val screenState: ScreenState? = null,
    val errorMessage: String? = null
)

enum class RecognitionState {
    SCANNING,
    FAILED,
    SUCCESS
}
