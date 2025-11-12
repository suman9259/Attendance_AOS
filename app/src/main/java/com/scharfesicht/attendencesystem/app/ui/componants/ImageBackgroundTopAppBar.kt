package com.scharfesicht.attendencesystem.app.ui.componants

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scharfesicht.attendencesystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppTopAppBar(
    titleEn: String,
    titleAr: String,
    isArabic: Boolean,
    isDark: Boolean,
    onBackClick: () -> Unit = {},
    onGroupIconClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Handle LTR/RTL layout based on Absher SDK language
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val title = if (isArabic) titleAr else titleEn
        val textColor = Color.White
        val gradientColors = if (isDark) {
            listOf(Color(0xFF1E1E1E), Color(0xFF2C2C2C))
        } else {
            listOf(Color(0xFFBFA253), Color(0xFFE3C16E))
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
//            // Gradient overlay
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        Brush.verticalGradient(
//                            colors = gradientColors
//                        )
//                    )
//            )

            // Background image
            Image(
                painter = painterResource(id = R.drawable.ic_top_app_bar_bg),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )



            // Top bar content
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = if (isArabic) TextAlign.Right else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = if (isArabic) "رجوع" else "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onGroupIconClick) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = if (isArabic) "إعدادات" else "Settings",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textColor
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEnglishLightTopAppBar() {
    MainAppTopAppBar(
        titleEn = "Time Attendance",
        titleAr = "نظام الحضور والانصراف",
        isArabic = false,
        isDark = false
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewArabicDarkTopAppBar() {
    MainAppTopAppBar(
        titleEn = "Time Attendance",
        titleAr = "نظام الحضور والانصراف",
        isArabic = true,
        isDark = true
    )
}
