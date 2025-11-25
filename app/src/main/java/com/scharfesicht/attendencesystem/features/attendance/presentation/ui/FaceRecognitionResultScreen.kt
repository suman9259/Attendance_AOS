package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getFaceNotRecognizedText
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getSuccessfullyPunchedText
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getTryAgainText
import com.scharfesicht.attendencesystem.app.navigation.NavManager
import com.scharfesicht.attendencesystem.app.navigation.ScreenRoutes
import sa.gov.moi.absherinterior.R
import sa.gov.moi.absherinterior.components.*
import sa.gov.moi.absherinterior.utils.*
import kotlinx.coroutines.delay



@Composable
fun FaceRecognitionResultScreen(
    navManager: NavManager,
    isSuccess: Boolean,
    isIn: Boolean,
    onTryAgain: () -> Unit = {
        navManager.navigateBack()
    },
) {
    // TODO:  FIx Punch out text.
    // Auto dismiss success after 3 seconds
//    if (isSuccess) {
//        LaunchedEffect(key1 = isSuccess) {
//            delay(3000)
//            navManager.navigateBack()
//        }
//    }

    MainScreenView(
        uiState = ScreenState.Success(true),
        // TODO: Add back click
        topBar ={
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
        successComposable = {
            if (isSuccess) {
                // TODO: How to get Success or Failed message from sdk?
                SuccessContent(navManager = navManager, isIn = isIn)
            } else {
                FailureContent(onTryAgain = onTryAgain)
            }
        }
    )
}

@Composable
private fun SuccessContent(navManager: NavManager, isIn: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        CustomButton(
            buttonTitle = getSuccessfullyPunchedText(isIn) ?: "Successfully punched in",
            titleStyle = Typography().bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            titleColor = Color.White,
            buttonIcon = R.drawable.ic_info_outline_white_24dp,
            iconTint = Color.White,
            iconSize = 24.dp,
            buttonSize = ButtonSize.LARGE,
            buttonType = ButtonType.SOLID,
            buttonStyle = ButtonStyle.GREEN,
            contentPadding = 16.dp,
            topLeftRadius = 12.dp,
            topRightRadius = 12.dp,
            bottomLeftRadius = 12.dp,
            bottomRightRadius = 12.dp,
            onClick = {
                navManager.navigate(ScreenRoutes.AttendanceLogs.route)

            }
        )
    }
}

@Composable
private fun FailureContent(onTryAgain: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_face_id),
//            contentDescription = stringResource(R.string.face_not_recognized),
            contentDescription = "Face not recognized",
            tint = colorResource(R.color.danger_main),
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
//            text = stringResource(R.string.face_not_recognized),
            text = getFaceNotRecognizedText() ?: "",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.content_fg_color)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
//            buttonTitle = stringResource(R.string.try_again),
            buttonTitle = getTryAgainText() ?: "",
            titleStyle = Typography().bodyLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            titleColor = Color.White,
            buttonSize = ButtonSize.LARGE,
            buttonType = ButtonType.SOLID,
            buttonStyle = ButtonStyle.TERTIARY,
            buttonIcon = null,
            onClick = onTryAgain
        )
    }
}