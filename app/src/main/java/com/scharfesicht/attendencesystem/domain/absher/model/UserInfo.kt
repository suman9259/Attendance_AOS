package com.scharfesicht.attendencesystem.domain.absher.model

import com.scharfesicht.attendencesystem.app.ui.theme.ThemeMode

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