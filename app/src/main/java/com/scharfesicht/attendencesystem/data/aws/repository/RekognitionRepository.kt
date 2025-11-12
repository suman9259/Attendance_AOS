package com.scharfesicht.attendencesystem.data.aws.repository

import android.graphics.Bitmap
import android.util.Log
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.CompareFacesRequest
import com.amazonaws.services.rekognition.model.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

class RekognitionRepository @Inject constructor(
    private val rekognitionClient: AmazonRekognitionClient
) {

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return ByteBuffer.wrap(stream.toByteArray())
    }

    suspend fun compareFaces(oldFace: Bitmap, newFace: Bitmap): Float? =
        withContext(Dispatchers.IO) {
            try {
                val request = CompareFacesRequest()
                    .withSourceImage(Image().withBytes(bitmapToByteBuffer(oldFace)))
                    .withTargetImage(Image().withBytes(bitmapToByteBuffer(newFace)))
                    .withSimilarityThreshold(80F)

                val result = rekognitionClient.compareFaces(request)
                val similarity = result.faceMatches.firstOrNull()?.similarity
                Log.d("Rekognition", "Similarity = $similarity%")
                similarity
            } catch (e: Exception) {
                Log.e("Rekognition", "Error: ${e.localizedMessage}")
                null
            }
        }
}