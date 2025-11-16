package com.scharfesicht.attendencesystem.features.facecompare.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
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

    companion object {
        private const val TAG = "RekognitionRepository"
        private const val MIN_IMAGE_SIZE = 80 // AWS minimum
        private const val RECOMMENDED_SIZE = 600 // Good for face recognition
        private const val JPEG_QUALITY = 90
    }

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
            try {
                val client = getClient()

                // Download and process old image
                Log.d(TAG, "Downloading profile image from: $oldImageUrl")
                val oldBytes = downloadAndProcessImage(oldImageUrl)
                Log.d(TAG, "Profile image size: ${oldBytes.size} bytes")

                // Process new bitmap
                Log.d(TAG, "Processing captured image. Original size: ${newBitmap.width}x${newBitmap.height}")
                val processedBitmap = resizeBitmapIfNeeded(newBitmap)
                val newBytes = processedBitmap.toJpegBytes()
                Log.d(TAG, "Captured image size after processing: ${newBytes.size} bytes")

                // Validate images meet AWS requirements
                validateImageBytes(oldBytes, "Profile image")
                validateImageBytes(newBytes, "Captured image")

                // Create request
                val request = CompareFacesRequest()
                    .withSourceImage(Image().withBytes(ByteBuffer.wrap(oldBytes)))
                    .withTargetImage(Image().withBytes(ByteBuffer.wrap(newBytes)))
                    .withSimilarityThreshold(70f) // Lower threshold for better matching

                Log.d(TAG, "Calling AWS Rekognition CompareFaces API...")
                val result = client.compareFaces(request)

                Log.d(TAG, "API Response - Face matches found: ${result.faceMatches.size}")
                Log.d(TAG, "Unmatched faces: ${result.unmatchedFaces.size}")

                val match = result.faceMatches.maxByOrNull { it.similarity ?: 0f }
                val similarity = match?.similarity ?: 0f

                Log.d(TAG, "Best match similarity: $similarity%")

                return@withContext FaceCompareResult(
                    isSame = similarity >= 70f,
                    accuracy = similarity
                )
            } catch (e: Exception) {
                Log.e(TAG, "Face comparison error: ${e.message}", e)
                throw e
            }
        }

    private fun downloadAndProcessImage(url: String): ByteArray {
        val imageBytes = URL(url).openStream().readBytes()

        // Decode and re-encode to ensure proper format
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw IllegalArgumentException("Failed to decode profile image")

        val resizedBitmap = resizeBitmapIfNeeded(bitmap)
        return resizedBitmap.toJpegBytes()
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // If bitmap is too small, AWS will reject it
        if (width < MIN_IMAGE_SIZE || height < MIN_IMAGE_SIZE) {
            Log.w(TAG, "Image too small (${width}x${height}). Minimum is ${MIN_IMAGE_SIZE}x${MIN_IMAGE_SIZE}")
            throw IllegalArgumentException("Image too small for face recognition. Minimum size is ${MIN_IMAGE_SIZE}x${MIN_IMAGE_SIZE} pixels")
        }

        // If image is already good size, return as-is
        if (width <= RECOMMENDED_SIZE && height <= RECOMMENDED_SIZE) {
            Log.d(TAG, "Image size is good: ${width}x${height}")
            return bitmap
        }

        // Resize large images to recommended size
        val scale = minOf(
            RECOMMENDED_SIZE.toFloat() / width,
            RECOMMENDED_SIZE.toFloat() / height
        )

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        Log.d(TAG, "Resizing image from ${width}x${height} to ${newWidth}x${newHeight}")

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun Bitmap.toJpegBytes(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
        return stream.toByteArray()
    }

    private fun validateImageBytes(bytes: ByteArray, imageName: String) {
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("$imageName is empty")
        }

        // AWS Rekognition limits
        val minSize = 1024 // 1 KB
        val maxSize = 15 * 1024 * 1024 // 15 MB

        when {
            bytes.size < minSize -> {
                throw IllegalArgumentException("$imageName is too small (${bytes.size} bytes). Minimum is $minSize bytes")
            }
            bytes.size > maxSize -> {
                throw IllegalArgumentException("$imageName is too large (${bytes.size} bytes). Maximum is $maxSize bytes")
            }
            else -> {
                Log.d(TAG, "$imageName validation passed: ${bytes.size} bytes")
            }
        }
    }
}