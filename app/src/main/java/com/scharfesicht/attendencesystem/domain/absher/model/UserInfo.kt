package com.scharfesicht.attendencesystem.domain.absher.model

import com.scharfesicht.attendencesystem.app.ui.theme.ThemeMode

data class UserInfo(
    val nationalId: String,
    val fullNameAr: String,
    val fullNameEn: String,
    val firstNameAr: String,
    val token: String,
    val theme: ThemeMode
)