package com.scharfesicht.attendencesystem.features.facecompare.data

import android.content.Context
import android.graphics.Bitmap
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.CompareFacesRequest
import com.amazonaws.services.rekognition.model.Image
import com.scharfesicht.attendencesystem.features.facecompare.model.FaceCompareResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.ByteBuffer

class RekognitionRepository(
    private val identityPoolId: String,
    private val region: Regions,
    private val appContext: Context
) {

    private fun getClient(): AmazonRekognitionClient {
        val provider = CognitoCachingCredentialsProvider(
            appContext,
            identityPoolId,
            region
        )
        return AmazonRekognitionClient(provider)
    }

    suspend fun compareFaces(oldImageUrl: String, newBitmap: Bitmap): FaceCompareResult =
        withContext(Dispatchers.IO) {

            val client = getClient()
            val oldBytes = URL(oldImageUrl).openStream().readBytes()
            val newBytes = newBitmap.toJpegBytes()

            val request = CompareFacesRequest()
                .withSourceImage(Image().withBytes(ByteBuffer.wrap(oldBytes)))
                .withTargetImage(Image().withBytes(ByteBuffer.wrap(newBytes)))
                .withSimilarityThreshold(80f)

            val result = client.compareFaces(request)

            val match = result.faceMatches.maxByOrNull { it.similarity ?: 0f }
            val similarity = match?.similarity ?: 0f

            return@withContext FaceCompareResult(
                isSame = similarity >= 80f,
                accuracy = similarity
            )
        }


    private fun Bitmap.toJpegBytes(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }
}
