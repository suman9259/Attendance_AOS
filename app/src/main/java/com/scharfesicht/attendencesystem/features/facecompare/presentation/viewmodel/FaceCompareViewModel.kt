package com.scharfesicht.attendencesystem.features.facecompare.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.features.facecompare.domain.FaceCompareUseCase
import com.scharfesicht.attendencesystem.features.facecompare.model.FaceCompareResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FaceCompareUiState {
    object Idle : FaceCompareUiState()
    object Loading : FaceCompareUiState()
    data class Success(val isSame: Boolean, val accuracy: Float) : FaceCompareUiState()
    data class Error(val message: String) : FaceCompareUiState()
}

@HiltViewModel
class FaceCompareViewModel @Inject constructor(
    private val useCase: FaceCompareUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FaceCompareUiState>(FaceCompareUiState.Idle)
    val uiState: StateFlow<FaceCompareUiState> = _uiState

    fun compareFaces(oldUrl: String, newBitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _uiState.value = FaceCompareUiState.Loading

                val result: FaceCompareResult = useCase(oldUrl, newBitmap)

                _uiState.value = FaceCompareUiState.Success(
                    isSame = result.isSame,
                    accuracy = result.accuracy
                )

            } catch (e: Exception) {
                _uiState.value = FaceCompareUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}
