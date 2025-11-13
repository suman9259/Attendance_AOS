package com.scharfesicht.attendencesystem.features.facecompare.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.features.facecompare.domain.FaceCompareUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FaceCompareUiState {
    object Idle : FaceCompareUiState()
    object Capturing : FaceCompareUiState()
    object Loading : FaceCompareUiState()
    data class Success(val isSamePerson: Boolean, val accuracy: Float): FaceCompareUiState()
    data class Error(val message: String) : FaceCompareUiState()
}


@HiltViewModel
class FaceCompareViewModel @Inject constructor(
    private val useCase: FaceCompareUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FaceCompareUiState>(FaceCompareUiState.Idle)
    val uiState: StateFlow<FaceCompareUiState> = _uiState

    private var oldImageUrl: String = ""

    fun setOldImageUrl(url: String) {
        oldImageUrl = url
    }

    fun compareWithAws(newBitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _uiState.value = FaceCompareUiState.Loading

                val result = useCase(oldImageUrl, newBitmap)

                _uiState.value = FaceCompareUiState.Success(
                    isSamePerson = result.isSame,
                    accuracy = result.accuracy
                )

            } catch (e: Exception) {
                _uiState.value = FaceCompareUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

