package com.scharfesicht.attendencesystem.features.attendance.domain.repository

import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AttendanceRepository {
    fun getUpcomingHoliday(): Flow<Result<Holiday?>>
    fun getAssignedShift(): Flow<Result<Shift>>
    suspend fun markPunchIn(location: String?): Result<AttendanceRecord>
    suspend fun markPunchOut(location: String?): Result<AttendanceRecord>
    fun getTodayAttendance(): Flow<Result<AttendanceRecord?>>
    fun getAttendanceSummary(month: String): Flow<Result<List<AttendanceSummary>>>
    fun getAttendanceStats(startDate: LocalDate, endDate: LocalDate): Flow<Result<AttendanceStats>>
}