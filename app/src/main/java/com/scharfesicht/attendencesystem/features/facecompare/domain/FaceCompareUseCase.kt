package com.scharfesicht.attendencesystem.features.facecompare.domain

import android.graphics.Bitmap
import com.scharfesicht.attendencesystem.features.facecompare.data.RekognitionRepository
import com.scharfesicht.attendencesystem.features.facecompare.model.FaceCompareResult
import javax.inject.Inject

class FaceCompareUseCase @Inject constructor(
    private val repository: RekognitionRepository
) {
    suspend operator fun invoke(oldImageUrl: String, newImage: Bitmap): FaceCompareResult {
        return repository.compareFaces(oldImageUrl, newImage)
    }
}

