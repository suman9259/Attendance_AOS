package com.scharfesicht.attendencesystem.features.auth.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.scharfesicht.attendencesystem.core.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FaceCompareScreen(
    oldImageUrl: String,
    modifier: Modifier = Modifier,
    viewModel: FaceCompareViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState()

    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var cameraController: LifecycleCameraController? by remember { mutableStateOf(null) }

    val oldBitmap by produceState<Bitmap?>(initialValue = null, key1 = oldImageUrl) {
        value = BitmapUtils.loadBitmapFromUrl(oldImageUrl)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Face Comparison", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        if (capturedImage == null) {
            val previewView = remember { PreviewView(context) }
            cameraController = remember {
                LifecycleCameraController(context).apply {
                    bindToLifecycle(context as androidx.lifecycle.LifecycleOwner)
                    setEnabledUseCases(
                        LifecycleCameraController.IMAGE_CAPTURE or
                                LifecycleCameraController.VIDEO_CAPTURE
                    )
                }
            }
            previewView.controller = cameraController

            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )

            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                val file = File(context.cacheDir, "captured_face.jpg")
                val output = ImageCapture.OutputFileOptions.Builder(file).build()
                cameraController?.imageCapture?.takePicture(
                    output,
                    Dispatchers.IO.asExecutor(),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            capturedImage = BitmapFactory.decodeFile(file.absolutePath)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                        }
                    }
                )
            }) {
                Text("Capture Image")
            }

        } else {
            Image(
                painter = rememberAsyncImagePainter(capturedImage),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        oldBitmap?.let { old ->
                            viewModel.compare(old, capturedImage!!)
                        }
                    }
                },
                enabled = oldBitmap != null
            ) {
                Text("Compare Faces")
            }
        }

        Spacer(Modifier.height(24.dp))

        when (val state = result) {
            is FaceCompareResult.Idle -> Text("Waiting for input...")
            is FaceCompareResult.Loading -> CircularProgressIndicator()
            is FaceCompareResult.Match -> Text("✅ Match (${String.format("%.1f", state.similarity)}%)", color = MaterialTheme.colorScheme.primary)
            is FaceCompareResult.NoMatch -> Text("❌ No Match (${String.format("%.1f", state.similarity)}%)", color = MaterialTheme.colorScheme.error)
            is FaceCompareResult.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
        }
    }
}