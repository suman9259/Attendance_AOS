package com.scharfesicht.attendencesystem.features.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.scharfesicht.attendencesystem.app.MiniAppEntryPoint
import com.scharfesicht.attendencesystem.core.localization.LocalLanguage
import com.scharfesicht.attendencesystem.core.localization.LocalStrings

@Composable
fun SampleUserInfoUI() {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Show English + Arabic name
        Text(text = "English: ${MiniAppEntryPoint.getUserFullName()}")
        Text(text = "Arabic: ${MiniAppEntryPoint.getUserFullName()}")

        // Show UI based on language
        Text(text = strings.language.toString())

        // Debug info
        Text(text = "Current Language: $lang")
    }
}
