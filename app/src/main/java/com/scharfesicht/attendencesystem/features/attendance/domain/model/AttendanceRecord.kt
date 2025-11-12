package com.scharfesicht.attendencesystem.features.attendance.domain.model

import java.time.LocalDateTime

data class AttendanceRecord(
    val id: String,
    val userId: String,
    val punchInTime: LocalDateTime?,
    val punchOutTime: LocalDateTime?,
    val shiftId: String,
    val date: LocalDateTime,
    val status: AttendanceStatus,
    val location: String? = null
)

enum class AttendanceStatus {
    PRESENT,
    LATE_LESS_THAN_1H,
    LATE_MORE_THAN_1H,
    EARLY_PUNCH_OUT,
    ABSENT
}