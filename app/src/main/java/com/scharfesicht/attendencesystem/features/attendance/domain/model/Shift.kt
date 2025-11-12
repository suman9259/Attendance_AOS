package com.scharfesicht.attendencesystem.features.attendance.domain.model

import java.time.LocalTime

data class Shift(
    val id: String,
    val name: String,
    val nameAr: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val type: ShiftType
)

enum class ShiftType {
    STANDARD,
    FIXED,
    FLEXIBLE
}