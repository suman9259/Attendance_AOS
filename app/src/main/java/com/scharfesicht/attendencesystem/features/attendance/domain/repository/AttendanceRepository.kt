package com.scharfesicht.attendencesystem.features.attendance.domain.repository

import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {

    suspend fun login(request: LoginRequest): Result<LoginResponse>
    fun getAssignedShift(): Flow<Result<ShiftData>>
    suspend fun markPunchIn(location: String?): Result<AttendanceRecord>
    suspend fun markPunchOut(location: String?): Result<AttendanceRecord>
    fun getTodayAttendance(): Flow<Result<AttendanceRecord?>>
}