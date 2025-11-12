package com.scharfesicht.attendencesystem.domain.absher.model

import com.scharfesicht.attendencesystem.app.ui.theme.ThemeMode

data class AppPreferences(
    val isArabic: Boolean = false,
    val isDarkMode: Boolean = false,
    val language: String = "en" // "en" or "ar"
) {
    companion object {
        fun fromAbsher(userInfo: UserInfo): AppPreferences {
            return AppPreferences(
//                isArabic = userInfo.language.name.lowercase()== "ar",
                isArabic = true,
                isDarkMode = userInfo.theme == ThemeMode.DARK,
                language = userInfo.language.name
            )
        }
    }
}