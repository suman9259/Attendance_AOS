package com.scharfesicht.attendencesystem.features.auth.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.data.aws.repository.RekognitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceCompareViewModel @Inject constructor(
    private val repository: RekognitionRepository
) : ViewModel() {

    private val _result = MutableStateFlow<FaceCompareResult>(FaceCompareResult.Idle)
    val result = _result.asStateFlow()

    fun compare(oldFace: Bitmap, newFace: Bitmap) {
        _result.value = FaceCompareResult.Loading
        viewModelScope.launch {
            val similarity = repository.compareFaces(oldFace, newFace)
            _result.value = when {
                similarity == null -> FaceCompareResult.Error("AWS Rekognition failed")
                similarity >= 90F -> FaceCompareResult.Match(similarity)
                else -> FaceCompareResult.NoMatch(similarity)
            }
        }
    }
}

sealed class FaceCompareResult {
    object Idle : FaceCompareResult()
    object Loading : FaceCompareResult()
    data class Match(val similarity: Float) : FaceCompareResult()
    data class NoMatch(val similarity: Float) : FaceCompareResult()
    data class Error(val message: String) : FaceCompareResult()
}