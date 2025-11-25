package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getMarkAttendanceTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getPermissionTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getPunchCardSmallTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getPunchCardTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getPunchInCardTitle
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getPunchOutCardTitle
import com.scharfesicht.attendencesystem.app.navigation.NavManager
import com.scharfesicht.attendencesystem.app.navigation.ScreenRoutes
import com.scharfesicht.attendencesystem.core.utils.toAmPm
import com.scharfesicht.attendencesystem.features.attendance.domain.model.Shift
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceDashboardViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.PunchFlowState
import kotlinx.coroutines.delay
import sa.gov.moi.absherinterior.R
import sa.gov.moi.absherinterior.components.*
import sa.gov.moi.absherinterior.models.AppMessage
import sa.gov.moi.absherinterior.theme.base
import sa.gov.moi.absherinterior.theme.small
import sa.gov.moi.absherinterior.utils.*
import java.lang.IllegalStateException

@Composable
fun AttendanceDashboardScreen(
    navManager: NavManager,
    viewModel: AttendanceDashboardViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val flowState by viewModel.flowState.collectAsState()
    val toastMessage by viewModel.toast.collectAsState(initial = null)



    // ─────────────────────────────
    // CAMERA: TakePicturePreview()  (NO FILE / NO FILEPROVIDER)
    // ─────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            Log.d("AttendanceScreen", "Camera bitmap received: ${bitmap.width}x${bitmap.height}")
            viewModel.onPhotoCaptured(bitmap)
        } else {
            Log.e("AttendanceScreen", "Camera returned null bitmap")
            viewModel.onCameraError("Camera cancelled or failed")
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (isGranted) {
            // Directly open camera preview
            cameraLauncher.launch(null)
        } else {
            viewModel.onCameraError("Camera permission denied")
        }
    }

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

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is AttendanceDashboardViewModel.AttendanceNavigationEvent.FaceRecognitionSuccess -> {
                    navManager.navigate(
                        ScreenRoutes.FaceRecognitionSuccess.createRoute(isSuccess = event.isSuccess, isIn = event.isIn)
                    )
                    Log.d("AttendanceScreen", "Navigating to FaceRecognitionSuccess with = ${event.isSuccess}")
                }
            }
        }
    }



    LaunchedEffect(flowState) {
        when (flowState) {

            is PunchFlowState.WaitingForLocation -> {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    requestLocation(context, viewModel)
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            is PunchFlowState.WaitingForCamera -> {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            else -> Unit
        }
    }


    // ─────────────────────────────
    // MAIN SCREEN CONTENT
    // ─────────────────────────────
    MainScreenView(
        uiState = uiState.screenState,
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
                title = MiniAppEntryPoint.getServiceTitle(),
                generalIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_as_admin),
                        contentDescription = "time attendance"
                    )
                },
                onBackClicked = {navManager.navigateBack()}
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),

        errorComposable = { msg ->
            ErrorComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                errorMessage = msg
            )
        },

        message = when {
            uiState.errorMessage != null -> uiState.errorMessage
            uiState.successMessage != null -> AppMessage.Success(
                message = uiState.successMessage ?: "",
                messageKey = "success_toast"
            )
            else -> null
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
                    isCheckedIn = uiState.isCheckedIn,
                    navManager = navManager
                )
            }
        }
    )

}

/**
 * Request current location & send it to ViewModel
 */
private fun requestLocation(
    context: Context,
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
                Log.d(
                    "AttendanceScreen",
                    "Location received: ${location.latitude}, ${location.longitude}"
                )
                viewModel.onLocationReceived(location.latitude, location.longitude)
            } else {
                viewModel.onLocationError("Unable to get location")
            }
        }.addOnFailureListener { exception ->
            viewModel.onLocationError(exception.message ?: "Location request failed")
        }
    } catch (e: SecurityException) {
        viewModel.onLocationError("Location permission not granted")
    } catch (e: Exception) {
        viewModel.onLocationError(e.message ?: "Unknown location error")
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
    isCheckedIn: Boolean,
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
                    tabTitle = getMarkAttendanceTitle() ?: "",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = { onTabChanged(0) }
                )
                CustomIndicatorTab(
                    isSelected = selectedTab == 1,
                    tabTitle = getPermissionTitle() ?: "",
                    tabTitleStyle = Typography().small,
                    tabSelectedColor = colorResource(R.color.primary_main),
                    tabUnSelectedColor = colorResource(R.color.white),
                    onTabClick = {
                        onTabChanged(1)
                    }
                )
            }
        )

        16.0.MOIVerticalSpacer()

        PunchInOutCard(
            shiftTitle = getPunchCardTitle() ?: "",
            currentShift = currentShift,
            onPunchIn = onPunchIn,
            onPunchOut = onPunchOut,
            isPunchingIn = isPunchingIn,
            isPunchingOut = isPunchingOut,
            isCheckedIn = isCheckedIn
        )

        12.0.MOIVerticalSpacer()
    }
}

@Composable
fun PunchInOutCard(
    modifier: Modifier = Modifier,
    shiftTitle: String = "",
    currentShift: Shift?,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    isPunchingIn: Boolean = false,
    isPunchingOut: Boolean = false,
    isCheckedIn: Boolean = false
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

                Text(
                    text = currentShift?.shift_type_name ?: getPunchCardSmallTitle() ?: "",
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.dark_gray_100)
                )

                Text(
                    text = "${currentShift?.shift_rule?.get(0)?.start_time?.toAmPm() ?: "--"}" +
                            " - ${currentShift?.shift_rule?.get(0)?.end_time?.toAmPm() ?: "--"}",
                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.primary_main)
                )

                8.0.MOIVerticalSpacer()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    val enablePunchIn = !isCheckedIn && !isPunchingOut && !isPunchingIn

                    MOICard(
                        modifier = Modifier.weight(1f),
                        cornerSize = CardSize.MEDIUM,
                        cardColor = colorResource(R.color.green_main),
                        onCardClicked = {
                           /* if (enablePunchIn) */onPunchIn()
                        },
                        cardContent = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp)
                            ) {
                                CustomIconButton(
                                    icon = R.drawable.ic_exit_app,
                                    iconSize = 24,
                                    tint = Color.White,
                                    onClick = {
                                        /*if (enablePunchIn)*/ onPunchIn()
                                    },
                                    showContainer = false
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = getPunchInCardTitle() ?: "",
                                    style = Typography().base.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    )

                    // -----------------------------
                    //  PUNCH OUT BUTTON
                    // -----------------------------
                    val enablePunchOut = isCheckedIn && !isPunchingIn && !isPunchingOut
                    // TODO : Could we add toast on already punch in or punch in first.

                    MOICard(
                        modifier = Modifier.weight(1f),
                        cornerSize = CardSize.MEDIUM,
                        cardColor = colorResource(R.color.primary_main),
                        onCardClicked = {
                            /*if (enablePunchOut)*/ onPunchOut()
                        },
                        cardContent = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp)
                            ) {
                                CustomIconButton(
                                    icon = R.drawable.ic_exit_app,
                                    iconSize = 24,
                                    tint = Color.White,
                                    onClick = { /*if (enablePunchOut)*/ onPunchOut() },
                                    showContainer = false
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = getPunchOutCardTitle() ?: "",
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