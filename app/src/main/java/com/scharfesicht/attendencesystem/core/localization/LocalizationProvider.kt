package com.scharfesicht.attendencesystem.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.scharfesicht.attendencesystem.core.utils.AppLanguage

val LocalLanguage = compositionLocalOf { AppLanguage.ENGLISH }
val LocalStrings = compositionLocalOf { LocalizedStrings(AppLanguage.ENGLISH) }

@Composable
fun LocalizationProvider(
    language: AppLanguage,
    isRTL: Boolean = language == AppLanguage.ARABIC,
    content: @Composable () -> Unit
) {
    val layoutDirection = if (isRTL) LayoutDirection.Rtl else LayoutDirection.Ltr
    val strings = rememberLocalizedStrings(language)

    CompositionLocalProvider(
        LocalLanguage provides language,
        LocalStrings provides strings,
        LocalLayoutDirection provides layoutDirection
    ) {
        content()
    }
}