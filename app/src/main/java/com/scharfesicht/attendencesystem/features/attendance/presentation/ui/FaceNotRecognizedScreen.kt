package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import android.R.attr.contentDescription
import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scharfesicht.attendencesystem.app.ui.componants.MainAppTopAppBar
import com.scharfesicht.attendencesystem.app.ui.theme.AttendanceSystemTheme
import com.scharfesicht.attendencesystem.R

@Composable
fun FaceNotRecognizedScreen(
    isSuccess : Boolean = false,
    isArabic: Boolean = false,
    onSuccessClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isSuccess){
                // ----------- Try Again Button -----------
                Button(
                    onClick = onSuccessClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),     // gold
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isArabic) "تم تسجيل الحضور بنجاح" else "Successfully punched in",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }else{// ---------- Red Face Error Icon ----------
                /*Icon(
                    painter = painterResource(id = R.drawable.ic_face_id_error), // YOU WILL PROVIDE THIS
                    contentDescription = null,
                    modifier = Modifier.size(90.dp).align(Alignment.CenterHorizontally),
                )*/
//            Image(
//                painter = painterResource(R.drawable.ic_face_id_error),
//                contentDescription = "Face not Recognization error",
//                modifier = Modifier.size(90.dp).align(Alignment.CenterHorizontally),
//            )

                Spacer(modifier = Modifier.height(20.dp))

                // ----------- Error Text -----------
                Text(
                    text = if (isArabic) "الوجه غير معرّف" else "Face not recognized",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(40.dp))

                // ----------- Try Again Button -----------
                Button(
                    onClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,     // gold
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isArabic) "حاول مرة أخرى" else "Try again",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewFaceNotRecognizedScreen() {
    AttendanceSystemTheme(true) {
        FaceNotRecognizedScreen(
            isArabic = true,
            onSuccessClick = {},
            onRetryClick = {},
            isSuccess = true
        )
    }

}