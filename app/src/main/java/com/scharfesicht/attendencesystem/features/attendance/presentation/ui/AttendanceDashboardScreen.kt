package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.scharfesicht.attendencesystem.app.navigation.NavManager
import com.scharfesicht.attendencesystem.features.attendance.domain.model.Shift
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceDashboardViewModel
import sa.gov.moi.absherinterior.R
import sa.gov.moi.absherinterior.components.*
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.theme.base
import sa.gov.moi.absherinterior.theme.small
import sa.gov.moi.absherinterior.utils.*
import java.io.File


@Composable
fun AttendanceDashboardScreen(
    navManager: NavManager,
    viewModel: AttendanceDashboardViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val shouldRequestLocation by viewModel.shouldRequestLocation.collectAsState()
    val shouldOpenCamera by viewModel.shouldOpenCamera.collectAsState()

    // Photo file URI state
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestLocation(context, viewModel)
        } else {
            viewModel.onLocationError("Location permission denied")
        }
    }

    // Camera launcher - FULL SIZE IMAGE
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            try {
                // Load full-size image from file
                val bitmap = loadBitmapFromUri(context, photoUri!!)
                if (bitmap != null) {
                    viewModel.onPhotoCaptured(bitmap)
                } else {
                    viewModel.onCameraError("Failed to load captured photo")
                }
            } catch (e: Exception) {
                viewModel.onCameraError("Error loading photo: ${e.message}")
            }
        } else {
            viewModel.onCameraError("Camera cancelled or failed")
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoUri = createImageFileUri(context)
            if (photoUri != null) {
                cameraLauncher.launch(photoUri!!)
            } else {
                viewModel.onCameraError("Failed to create image file")
            }
        } else {
            viewModel.onCameraError("Camera permission denied")
        }
    }

    // Handle location request
    LaunchedEffect(shouldRequestLocation) {
        if (shouldRequestLocation) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                    requestLocation(context, viewModel)
                }
                else -> {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    // Handle camera request
    LaunchedEffect(shouldOpenCamera) {
        shouldOpenCamera?.let {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) -> {
                    photoUri = createImageFileUri(context)
                    if (photoUri != null) {
                        cameraLauncher.launch(photoUri!!)
                    } else {
                        viewModel.onCameraError("Failed to create image file")
                    }
                }
                else -> {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    MainScreenView(
        uiState = uiState.screenState,
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
                title = "Time Attendance",
                generalIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_as_admin),
                        contentDescription = "time attendance"
                    )
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        message = uiState.errorMessage?.let {
            AppMessage.Error(
                message = it.message,
                messageKey = "error_message"
            )
        },
        successComposable = {
            if (uiState.isLoading || !uiState.isLoginComplete) {
                AttendanceShimmerLoading()
            } else {
                AttendanceDashboardContent(
                    currentShift = uiState.currentShift,
                    selectedTab = selectedTab,
                    onTabChanged = viewModel::onTabChanged,
                    onPunchIn = viewModel::startPunchIn,
                    onPunchOut = viewModel::startPunchOut,
                    isPunchingIn = uiState.isPunchingIn,
                    isPunchingOut = uiState.isPunchingOut,
                    navManager = navManager
                )
            }
        }
    )

    LaunchedEffect(uiState.showSuccessToast, uiState.showErrorToast) {
        if (uiState.showSuccessToast) {
            viewModel.onToastShown()
        }
        if (uiState.showErrorToast) {
            viewModel.onToastShown()
        }
    }
}

/**
 * Create a temporary file URI for camera to save full-size image
 */
private fun createImageFileUri(context: android.content.Context): Uri? {
    return try {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "attendance_photo_$timeStamp.jpg"
        val imageFile = File(context.cacheDir, imageFileName)

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        android.util.Log.e("AttendanceScreen", "Error creating image file", e)
        null
    }
}

/**
 * Load full-size bitmap from URI
 */
private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        android.util.Log.d("AttendanceScreen", "Loaded bitmap: ${bitmap?.width}x${bitmap?.height}")
        bitmap
    } catch (e: Exception) {
        android.util.Log.e("AttendanceScreen", "Error loading bitmap", e)
        null
    }
}

private fun requestLocation(
    context: android.content.Context,
    viewModel: AttendanceDashboardViewModel
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                viewModel.onLocationReceived(location.latitude, location.longitude)
            } else {
                viewModel.onLocationError("Unable to get location")
            }
        }.addOnFailureListener { exception ->
            viewModel.onLocationError(exception.message ?: "Location request failed")
        }
    } catch (e: SecurityException) {
        viewModel.onLocationError("Location permission not granted")
    }
}

@Composable
private fun AttendanceShimmerLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shimmerEffect()
            )
        }

        16.0.MOIVerticalSpacer()

        MOICard(
            cornerSize = CardSize.LARGE,
            cardContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(20.dp)
                            .shimmerEffect()
                    )

                    8.0.MOIVerticalSpacer()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(24.dp)
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(20.dp)
                            .shimmerEffect()
                    )

                    12.0.MOIVerticalSpacer()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .shimmerEffect()
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun AttendanceDashboardContent(
    currentShift: Shift? = null,
    selectedTab: Int,
    onTabChanged: (Int) -> Unit,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    isPunchingIn: Boolean,
    isPunchingOut: Boolean,
    navManager: NavManager?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        CustomTabRow(
            selectedTabIndex = selectedTab,
            tabType = TabType.OUTLINED,
            backgroundColor = Color.Transparent,
            indicatorColor = colorResource(R.color.primary_main),
            tabs = {
                CustomIndicatorTab(
                    isSelected = selectedTab == 0,
                    tabTitle = "Mark Attendance",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
                    tabTitle = "Permission Application",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = { onTabChanged(1) }
                )
            }
        )

        16.0.MOIVerticalSpacer()

        PunchInOutCard(
            shiftTitle = "Assigned Shift",
            currentShift = currentShift,
            onPunchIn = onPunchIn,
            onPunchOut = onPunchOut,
            isPunchingIn = isPunchingIn,
            isPunchingOut = isPunchingOut
        )

        12.0.MOIVerticalSpacer()
    }
}

data class AttendanceData(
    val upcomingHoliday: String,
    val shiftName: String,
    val shiftTime: String,
    val summary: AttendanceSummary
)

data class AttendanceSummary(
    val attendance: Int,
    val lateLessThan1h: Int,
    val lateMoreThan1h: Int,
    val earlyPunchOut: Int,
    val absence: Int
)

@Composable
fun PunchInOutCard(
    modifier: Modifier = Modifier,
    shiftTitle: String = "",
    currentShift: Shift?,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    isPunchingIn: Boolean = false,
    isPunchingOut: Boolean = false
) {
    MOICard(
        cornerSize = CardSize.LARGE,
        cardContent = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = shiftTitle,
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.content_fg_color)
                )

                4.0.MOIVerticalSpacer()

                Text(
                    text = currentShift?.shift_name_lang ?: "Standard Shift",
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.dark_gray_100)
                )

                Text(
                    text = "${currentShift?.shift_rule?.get(0)?.start_time ?: "--"} - ${currentShift?.shift_rule?.get(0)?.end_time ?: "--"}",
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.primary_main)
                )

                8.0.MOIVerticalSpacer()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MOICard(
                        modifier = Modifier.weight(1f),
                        cornerSize = CardSize.MEDIUM,
                        cardColor = colorResource(R.color.green_main),
                        onCardClicked = if (!isPunchingIn && !isPunchingOut) onPunchIn else { {} },
                        cardContent = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp)
                            ) {
                                if (isPunchingIn) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .shimmerEffect()
                                    )
                                } else {
                                    CustomIconButton(
                                        icon = R.drawable.ic_exit_app,
                                        iconSize = 24,
                                        tint = Color.White,
                                        onClick = onPunchIn,
                                        showContainer = false
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isPunchingIn) "Processing..." else "Punch In",
                                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    )

                    MOICard(
                        modifier = Modifier.weight(1f),
                        cornerSize = CardSize.MEDIUM,
                        cardColor = colorResource(R.color.primary_main),
                        onCardClicked = if (!isPunchingIn && !isPunchingOut) onPunchOut else { {} },
                        cardContent = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp)
                            ) {
                                if (isPunchingOut) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .shimmerEffect()
                                    )
                                } else {
                                    CustomIconButton(
                                        icon = R.drawable.ic_exit_app,
                                        iconSize = 24,
                                        tint = Color.White,
                                        onClick = onPunchOut,
                                        showContainer = false
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isPunchingOut) "Processing..." else "Punch Out",
                                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    )
                }
            }
        }
    )
}