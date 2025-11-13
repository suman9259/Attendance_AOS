package com.scharfesicht.attendencesystem.features.facecompare.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onCapture: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView?.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera binding failed: $e")
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            factory = {
                PreviewView(it).also { previewView = it }
            }
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            captureImage(context, imageCapture, onCapture, cameraExecutor)
        }) {
            Text("Capture Image")
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onCapture: (Bitmap) -> Unit,
    cameraExecutor: ExecutorService
) {
    imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {

        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = imageProxyToBitmap(image)
            image.close()
            onCapture(bitmap)
        }

        override fun onError(exc: ImageCaptureException) {
            Log.e("CameraPreview", "Capture failed: ${exc.message}", exc)
        }
    })
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val matrix = Matrix().apply {
        postRotate(image.imageInfo.rotationDegrees.toFloat())
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
