package com.scharfesicht.attendencesystem.core.utils

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

data class UserInfo(
    val nationalId: String,
    val fullNameAr: String,
    val fullNameEn: String,
    val firstNameAr: String,
    val token: String,
    val theme: ThemeMode,
    val language: AppLanguage,
    val isRTL: Boolean
)

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    ARABIC("ar");

    companion object {
        fun from(code: String?): AppLanguage {
            return when (code?.lowercase()) {
                "ar", "arabic" -> ARABIC
                else -> ENGLISH
            }
        }
    }
}
data class LocalizedText(
    val ar: String,
    val en: String
)