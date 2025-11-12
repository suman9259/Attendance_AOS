package com.scharfesicht.attendencesystem.features.attendance.domain.model

import java.time.LocalDate

data class Holiday(
    val id: String,
    val name: String,
    val nameAr: String,
    val date: LocalDate,
    val isOfficial: Boolean
)