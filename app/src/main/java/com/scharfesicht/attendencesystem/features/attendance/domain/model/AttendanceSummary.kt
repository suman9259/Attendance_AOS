package com.scharfesicht.attendencesystem.features.attendance.domain.model

data class AttendanceSummary(
    val month: String,
    val attendanceHours: Float,
    val lateMoreThan1hHours: Float,
    val lateLessThan1hHours: Float,
    val earlyPunchOutHours: Float,
    val absenceHours: Float
)

data class AttendanceStats(
    val totalDays: Int,
    val presentDays: Int,
    val lateDays: Int,
    val absentDays: Int,
    val earlyPunchOutDays: Int
)