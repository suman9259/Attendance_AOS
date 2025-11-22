package com.scharfesicht.attendencesystem.features.facecompare.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.CompareFacesRequest
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.services.rekognition.model.InvalidParameterException
import com.amazonaws.AmazonServiceException
import com.scharfesicht.attendencesystem.features.facecompare.model.FaceCompareResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.ByteBuffer
import androidx.core.graphics.scale

class RekognitionRepository(
    private val identityPoolId: String,
    private val region: Regions,
    private val appContext: Context
) {

    companion object {
        private const val TAG = "RekognitionRepository"
        private const val MIN_DIMENSION = 80 // AWS minimum dimension
        private const val MAX_DIMENSION = 4096 // AWS maximum dimension
        private const val RECOMMENDED_SIZE = 800 // Optimal for face recognition
        private const val JPEG_QUALITY = 92 // Better quality for face details
        private const val MIN_BYTES = 1024 // 1 KB
        private const val MAX_BYTES = 5 * 1024 * 1024 // 5 MB (safe limit, AWS allows 15MB)
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
                Log.d(TAG, "Profile image processed: ${oldBytes.size} bytes")

                // Process new bitmap
                Log.d(TAG, "Processing captured image. Original: ${newBitmap.width}x${newBitmap.height}")
                val processedBitmap = resizeBitmapIfNeeded(newBitmap)
                val newBytes = processedBitmap.toJpegBytes()
                Log.d(TAG, "Captured image processed: ${newBytes.size} bytes, ${processedBitmap.width}x${processedBitmap.height}")

                // Validate images meet AWS requirements
                validateImageBytes(oldBytes, "Profile image")
                validateImageBytes(newBytes, "Captured image")

                // Create request with optimal settings
                val request = CompareFacesRequest()
                    .withSourceImage(Image().withBytes(ByteBuffer.wrap(oldBytes)))
                    .withTargetImage(Image().withBytes(ByteBuffer.wrap(newBytes)))
                    .withSimilarityThreshold(70f)
                    .withQualityFilter("AUTO") // Filter low quality faces

                Log.d(TAG, "Calling AWS Rekognition CompareFaces API...")
                val result = client.compareFaces(request)

                Log.d(TAG, "API Success - Face matches: ${result.faceMatches?.size ?: 0}")
                Log.d(TAG, "Unmatched faces: ${result.unmatchedFaces?.size ?: 0}")

                // Handle results safely
                val faceMatches = result.faceMatches ?: emptyList()

                if (faceMatches.isEmpty()) {
                    Log.w(TAG, "No matching faces found")
                    return@withContext FaceCompareResult(
                        isSame = false,
                        accuracy = 0f
                    )
                }

                val match = faceMatches.maxByOrNull { it.similarity ?: 0f }
                val similarity = match?.similarity ?: 0f

                Log.d(TAG, "Best match similarity: $similarity%")

                return@withContext FaceCompareResult(
                    isSame = similarity >= 70f,
                    accuracy = similarity
                )

            } catch (e: InvalidParameterException) {
                // Log detailed AWS error information
                Log.e(TAG, "AWS InvalidParameterException: ${e.message}")
                Log.e(TAG, "Error Code: ${e.errorCode}")
                Log.e(TAG, "Error Type: ${e.errorType}")
                Log.e(TAG, "Request ID: ${e.requestId}")
                Log.e(TAG, "Status Code: ${e.statusCode}")

                // Return specific error result
                return@withContext FaceCompareResult(
                    isSame = false,
                    accuracy = 0f
                )
            } catch (e: AmazonServiceException) {
                // Handle other AWS service errors
                Log.e(TAG, "AWS Service Error: ${e.message}")
                Log.e(TAG, "Error Code: ${e.errorCode}")
                Log.e(TAG, "Request ID: ${e.requestId}")
                throw Exception("AWS Rekognition error: ${e.errorMessage ?: e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Face comparison error: ${e.message}", e)
                throw Exception("Face comparison failed: ${e.message}")
            }
        }

    private fun downloadAndProcessImage(url: String): ByteArray {
        return try {
            val imageBytes = URL(url).openStream().use { it.readBytes() }

            Log.d(TAG, "Downloaded ${imageBytes.size} bytes from URL")

            // Decode the image
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

            Log.d(TAG, "Original image dimensions: ${options.outWidth}x${options.outHeight}")

            // Now decode the actual bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: throw IllegalArgumentException("Failed to decode profile image from URL")

            val processedBitmap = resizeBitmapIfNeeded(bitmap)
            val result = processedBitmap.toJpegBytes()

            // Clean up
            if (bitmap != processedBitmap) {
                bitmap.recycle()
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading/processing image from URL", e)
            throw Exception("Failed to load profile image: ${e.message}")
        }
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        Log.d(TAG, "Checking image size: ${width}x${height}")

        // Validate minimum dimensions
        if (width < MIN_DIMENSION || height < MIN_DIMENSION) {
            val error = "Image too small: ${width}x${height}. Minimum: ${MIN_DIMENSION}x${MIN_DIMENSION}"
            Log.e(TAG, error)
            throw IllegalArgumentException(error)
        }

        // Validate maximum dimensions
        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            Log.w(TAG, "Image exceeds maximum AWS dimensions, will resize")
        }

        // Calculate if resizing is needed
        val needsResize = width > RECOMMENDED_SIZE ||
                height > RECOMMENDED_SIZE ||
                width > MAX_DIMENSION ||
                height > MAX_DIMENSION

        if (!needsResize) {
            Log.d(TAG, "Image size is optimal: ${width}x${height}")
            return bitmap
        }

        // Calculate target dimensions
        val maxAllowedSize = minOf(RECOMMENDED_SIZE, MAX_DIMENSION)
        val scale = minOf(
            maxAllowedSize.toFloat() / width,
            maxAllowedSize.toFloat() / height
        )

        val newWidth = (width * scale).toInt().coerceAtLeast(MIN_DIMENSION)
        val newHeight = (height * scale).toInt().coerceAtLeast(MIN_DIMENSION)

        Log.d(TAG, "Resizing image from ${width}x${height} to ${newWidth}x${newHeight}")

        return bitmap.scale(newWidth, newHeight).also {
            Log.d(TAG, "Resize complete")
        }
    }

    private fun Bitmap.toJpegBytes(): ByteArray {
        val stream = ByteArrayOutputStream()
        val compressed = compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)

        if (!compressed) {
            Log.e(TAG, "Failed to compress bitmap to JPEG")
            throw IllegalStateException("Failed to compress image")
        }

        return stream.toByteArray()
    }

    private fun validateImageBytes(bytes: ByteArray, imageName: String) {
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("$imageName is empty")
        }

        when {
            bytes.size < MIN_BYTES -> {
                val error = "$imageName too small: ${bytes.size} bytes. Minimum: $MIN_BYTES bytes"
                Log.e(TAG, error)
                throw IllegalArgumentException(error)
            }
            bytes.size > MAX_BYTES -> {
                val error = "$imageName too large: ${bytes.size} bytes. Maximum: $MAX_BYTES bytes"
                Log.e(TAG, error)
                throw IllegalArgumentException(error)
            }
            else -> {
                Log.d(TAG, "$imageName validation passed: ${bytes.size} bytes")
            }
        }
    }
}