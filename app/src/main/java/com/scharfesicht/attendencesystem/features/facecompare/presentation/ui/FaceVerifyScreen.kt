package com.scharfesicht.attendencesystem.features.facecompare.presentation.ui

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.scharfesicht.attendencesystem.features.facecompare.presentation.viewmodel.FaceCompareUiState
import com.scharfesicht.attendencesystem.features.facecompare.presentation.viewmodel.FaceCompareViewModel

@Composable
fun FaceVerifyScreen(
    onResult: (Boolean, Float) -> Unit,
    viewModel: FaceCompareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    BackHandler(enabled = true) {}

    CameraPermission {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            // ---------------- CAMERA PREVIEW ----------------
            if (uiState is FaceCompareUiState.Idle ||
                uiState is FaceCompareUiState.Capturing
            ) {
                CameraPreview(
//                    modifier = Modifier.fillMaxSize(),
                    onCapture = { bitmap ->
                        capturedImage = bitmap
                        viewModel.compareWithAws(bitmap)
                    }
                )

                CaptureBottomButton(
                    onCapture = {
//                        viewModel.setCapturing()
                    }
                )
            }

            // ---------------- LOADING STATE ----------------
            if (uiState is FaceCompareUiState.Loading) {
                LoadingView()
            }

            // ---------------- SUCCESS RESULT ----------------
            if (uiState is FaceCompareUiState.Success) {
                val data = uiState as FaceCompareUiState.Success

                FaceMatchResult(
                    isMatch = data.isSamePerson,
                    accuracy = data.accuracy,
                    onDone = { onResult(data.isSamePerson, data.accuracy) }
                )
            }

            // ---------------- ERROR STATE ----------------
            if (uiState is FaceCompareUiState.Error) {
                ErrorRetryView(
                    message = (uiState as FaceCompareUiState.Error).message,
                    onRetry = {
//                        viewModel.resetState()
                    },
                    onCancel = {
                        onResult(false, 0f)
                    }
                )
            }
        }
    }
}


@Composable
fun ErrorRetryView(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(message, color = Color.Red)
            Spacer(Modifier.height(20.dp))

            Button(onClick = onRetry) {
                Text("Retry")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onCancel) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}


@Composable
fun BoxScope.CaptureBottomButton(onCapture: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
            .align(Alignment.BottomCenter)
    ) {
        Button(
            onClick = onCapture,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.Center)
        ) {}
    }
}
@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White)
        Spacer(Modifier.height(10.dp))
    }
}


@Composable
fun FaceMatchResult(
    isMatch: Boolean,
    accuracy: Float,
    onDone: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(true) {
        scale.animateTo(
            1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = if (isMatch) "Face Matched" else "Face Mismatch",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Accuracy: ${"%.2f".format(accuracy)}%",
                color = Color.White
            )

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMatch) Color.Green else Color.Red
                )
            ) {
                Text("Continue", color = Color.White)
            }
        }
    }
}
