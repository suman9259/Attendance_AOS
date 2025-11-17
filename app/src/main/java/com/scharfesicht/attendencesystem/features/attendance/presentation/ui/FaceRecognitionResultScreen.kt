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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getFaceNotRecognizedText
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getSuccessfullyPunchedInText
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint.Companion.getTryAgainText
import sa.gov.moi.absherinterior.R
import sa.gov.moi.absherinterior.components.*
import sa.gov.moi.absherinterior.theme.AbsherInteriorTheme
import sa.gov.moi.absherinterior.utils.*
import kotlinx.coroutines.delay



@Composable
fun FaceRecognitionResultScreen(
    navController: NavController,
    isSuccess: Boolean = false,
    message: String = "",
    onTryAgain: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // Auto dismiss success after 3 seconds
    if (isSuccess) {
        LaunchedEffect(key1 = isSuccess) {
            delay(3000)
            onDismiss()
        }
    }

    MainScreenView(
        uiState = ScreenState.Success(true),
        topBar ={
            AbsherAppBar(
                showEventTheme = false,
                title = MiniAppEntryPoint.getServiceTitle(),
                generalIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_as_admin),
                        contentDescription = "time attendance"
                    )
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        successComposable = {
            if (isSuccess) {
                SuccessContent(message = message.ifBlank { /*stringResource(R.string.successfully_punched_in)*/ "Successfully punched in"})
            } else {
                FailureContent(onTryAgain = onTryAgain)
            }
        }
    )
}

@Composable
private fun SuccessContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        CustomButton(
            buttonTitle = message,
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
            onClick = {}
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

@Composable
fun FaceNotRecognizedScreen(
    navController: NavController?,
    onTryAgain: () -> Unit = {}
) {
    MainScreenView(
        uiState = ScreenState.Success(true),
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
                title = MiniAppEntryPoint.getServiceTitle(),
                generalIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_as_admin),
                        contentDescription = "time attendance"
                    )
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        successComposable = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_face_id),
//                    contentDescription = stringResource(R.string.face_not_recognized),
                    contentDescription = "Face not recognized",
                    modifier = Modifier.size(56.dp),
                    colorFilter = ColorFilter.tint(colorResource(R.color.danger_main))
                )

                CustomButton(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
//                    buttonTitle = stringResource(R.string.try_again),
                    buttonTitle = getTryAgainText() ?: "",
                    titleStyle = Typography().bodyLarge,
                    titleColor = Color.White,
                    buttonSize = ButtonSize.MEDIUM,
                    buttonType = ButtonType.SOLID,
                    buttonStyle = ButtonStyle.PRIMARY,
                    onClick = onTryAgain,
                    buttonIcon = null
                )
            }
        }
    )
}


@Composable
fun SuccessMessageScreen(
    navController: NavController,
    message: String = "",
    onDismiss: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }

    MainScreenView(
        uiState = ScreenState.Success(true),
        topBar = {
            AbsherAppBarLarge(
                showEventTheme = false,
                appBarContent = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        CustomIconButton(
                            icon = R.drawable.ic_arrow_right,
                            onClick = { navController.navigateUp() },
                            iconSize = 18,
                            tint = colorResource(R.color.header_fg_color)
                        )
                    }
                },
                centerLogo = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.profile_off),
                            contentDescription = "Profile",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        successComposable = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                MOICard(
                    modifier = Modifier.padding(top = 32.dp),
                    cornerSize = CardSize.MEDIUM,
                    cardColor = colorResource(R.color.green_main),
                    padding = PaddingValues(16.dp),
                    cardContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.ifBlank { /*stringResource(R.string.successfully_punched_in)*/  getSuccessfullyPunchedInText() ?: "" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_info_outline_white_24dp),
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            }
        }
    )
}
/*
@Composable
fun FaceRecognitionResultScreen(
    navController: NavController,
    isSuccess: Boolean = false,
    message: String = "Successfully punched in",
    onTryAgain: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // Auto dismiss success message after 3 seconds
    if (isSuccess) {
        LaunchedEffect(Unit) {
            delay(3000)
            onDismiss()
        }
    }

    MainScreenView(
        uiState = ScreenState.Success(true),
        topBar = {
            AbsherAppBarLarge(
                showEventTheme = false,
                appBarContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomIconButton(
                            icon = R.drawable.ic_arrow_right,
                            onClick = { navController.navigateUp() },
                            iconSize = 18,
                            tint = colorResource(R.color.header_fg_color)
                        )
                    }
                },
                centerLogo = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        SvgImageRender(
                            url = R.drawable.ic_download,
                            imageSize = 64.dp,
                            desc = "MOI Logo",
                            imageType = ImageType.LOCAL
                        )
                    }
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        successComposable = {
            if (isSuccess) {
                SuccessContent(message = message)
            } else {
                FailureContent(onTryAgain = onTryAgain)
            }
        }
    )
}

@Composable
private fun SuccessContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Success Banner at top
//        CustomInfoWidget(
//            modifier = Modifier.padding(top = 32.dp).height(50.dp),
//            title = "",
//            messages = message,
//            leadingIcon = R.drawable.ic_info_outline_white_24dp,
//            backgroundColor = colorResource(R.color.green_main),
//            borderColor = colorResource(R.color.primary_main),
//            contentColor = Color.White
//        )
        CustomButton(
            buttonTitle = "Successfully punched in",
            titleStyle = Typography().bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            titleColor = Color.White,
            buttonIcon = R.drawable.ic_info_outline_white_24dp,
            iconTint = Color.White,
            iconSize = 24.dp,
            buttonSize = ButtonSize.LARGE,
            buttonType = ButtonType.SOLID,
            buttonStyle = ButtonStyle.GREEN,  // ← Green color
            contentPadding = 16.dp,
            topLeftRadius = 12.dp,
            topRightRadius = 12.dp,
            bottomLeftRadius = 12.dp,
            bottomRightRadius = 12.dp,
            onClick = { }
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
        Spacer(modifier = Modifier.weight(0.3f))

        // Face Not Recognized Icon
        Icon(
            painter = painterResource(id = R.drawable.ic_face_id),
            contentDescription = "Face not recognized",
            tint = colorResource(R.color.danger_main),
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error Message
        Text(
            text = "Face not recognized",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.content_fg_color),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(0.4f))

        // Try Again Button
        CustomButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            buttonTitle = "Try again",
            titleStyle = Typography().bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            titleColor = Color.White,
            buttonSize = ButtonSize.LARGE,
            buttonType = ButtonType.SOLID,
            buttonStyle = ButtonStyle.TERTIARY,
            buttonIcon = null,
            onClick = {  },
        )

        Spacer(modifier = Modifier.weight(0.3f))
    }
}

// Alternative version with simpler CustomInfoWidget for success
@Composable
fun SuccessMessageScreen(
    navController: NavController,
    message: String = "Successfully punched in",
    onDismiss: () -> Unit = {}
) {
    // Auto dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }

    MainScreenView(
        uiState = ScreenState.Success(true),
        topBar = {
            AbsherAppBarLarge(
                showEventTheme = false,
                appBarContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        CustomIconButton(
                            icon = R.drawable.ic_arrow_right,
                            onClick = { navController.navigateUp() },
                            iconSize = 18,
                            tint = colorResource(R.color.header_fg_color)
                        )
                    }
                },
                centerLogo = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        SvgImageRender(
                            url = R.drawable.profile_off,
                            imageSize = 64.dp,
                            desc = "MOI Logo",
                            imageType = ImageType.LOCAL
                        )
                    }
                }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        successComposable = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Success Banner using MOICard
                MOICard(
                    modifier = Modifier.padding(top = 32.dp),
                    cornerSize = CardSize.MEDIUM,
                    cardColor = colorResource(R.color.green_main),
                    padding = PaddingValues(16.dp),
                    cardContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_info_outline_white_24dp),
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            }
        }
    )
}

// Face Not Recognized Screen
@Composable
fun FaceNotRecognizedScreen(
    navController: NavController?,
    onTryAgain: () -> Unit = {}
) {
    MainScreenView(
        uiState = ScreenState.Success(true),
        topBar = {
            AbsherAppBar(
                showEventTheme = false,
//                title = "stringResource(R.string.attendance_dashboard)",
                title = "Time Attendance",
                generalIcon = { Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = null) }
            )
        },
        contentPadding = PaddingValues(AppPadding.NON.padding()),
        successComposable = {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
//                SvgImageRender(
//                    url = R.drawable.ic_face_id,
//                    imageSize = 56.dp,
//                    desc = "Punch Out",
//                    imageType = ImageType.LOCAL
//                )
                Image(
                    painter = painterResource(R.drawable.ic_face_id),
                    contentDescription = "desc",
                    modifier = Modifier.size(56.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Red)
                )

//                CustomIconButton(
//                    icon = R.drawable.ic_face_id,
//                    iconSize = 500,
//                    tint = Color.Red,
//                    onClick = { },
//                    showContainer = false
//                )

                CustomButton(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    buttonTitle = "Try again",
                    titleStyle = Typography().bodyLarge,
                    titleColor = Color.White,
                    buttonSize = ButtonSize.MEDIUM,
                    buttonType = ButtonType.SOLID,
                    buttonStyle = ButtonStyle.PRIMARY,
                    onClick = { },
                    buttonIcon = null
                )
            }

        }
    )
}

// Previews
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SuccessMessagePreview() {
    AbsherInteriorTheme {
        SuccessContent(message = "Successfully punched in")
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FaceNotRecognizedPreview() {
    AbsherInteriorTheme {
//        FaceNotRecognizedContent(onTryAgain = {})
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FaceNotRecognizedScreenPreview() {
    AbsherInteriorTheme {
        FaceNotRecognizedScreen(
            navController = null,
            onTryAgain = {}
        )
    }
}*/
