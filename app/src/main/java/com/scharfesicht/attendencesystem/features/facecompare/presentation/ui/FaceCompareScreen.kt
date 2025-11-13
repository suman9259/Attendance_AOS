package com.scharfesicht.attendencesystem.features.facecompare.presentation.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.scharfesicht.attendencesystem.features.facecompare.presentation.viewmodel.*

@Composable
fun FaceCompareScreen(
    oldImageUrl: String,                     // OLD image URL from the server
    viewModel: FaceCompareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var newImage by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current

    CameraPermission {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Face Compare (AWS Rekognition)",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(20.dp))

            // -------------------- OLD IMAGE FROM URL --------------------
            Text("Old Image (From Server)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            Image(
                painter = rememberAsyncImagePainter(oldImageUrl),
                contentDescription = "Old Image",
                modifier = Modifier.size(220.dp)
            )

            Spacer(Modifier.height(20.dp))

            // -------------------- NEW IMAGE FROM CAMERA --------------------
            Text("Capture New Image", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            CameraPreview { bitmap ->
                newImage = bitmap
            }

            newImage?.let {
                Spacer(Modifier.height(10.dp))
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "New Image",
                    modifier = Modifier.size(220.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // -------------------- COMPARE BUTTON --------------------
            Button(
                onClick = {
                    if (newImage != null) {

                        viewModel.setOldImageUrl(oldImageUrl)
                        viewModel.compareWithAws(newImage!!)
                    }
                },
                enabled = newImage != null
            ) {
                Text("Compare Faces (AWS)")
            }

            Spacer(Modifier.height(20.dp))

            // -------------------- UI STATE --------------------
            when (uiState) {

                is FaceCompareUiState.Loading ->
                    CircularProgressIndicator()

                is FaceCompareUiState.Success -> {
                    val result = uiState as FaceCompareUiState.Success

                    Text(
                        text = if (result.isSamePerson)
                            "✔ SAME PERSON\nAccuracy: ${"%.2f".format(result.accuracy)}%"
                        else
                            "❌ DIFFERENT PERSON\nAccuracy: ${"%.2f".format(result.accuracy)}%",
                        color = if (result.isSamePerson) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                is FaceCompareUiState.Error -> {
                    Text(
                        text = "Error: ${(uiState as FaceCompareUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                else -> {}
            }
        }
    }
}
