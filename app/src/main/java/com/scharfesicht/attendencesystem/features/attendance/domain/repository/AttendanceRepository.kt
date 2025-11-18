package com.scharfesicht.attendencesystem.features.attendance.domain.repository

import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceRecord
import com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceRequest
import com.scharfesicht.attendencesystem.features.attendance.domain.model.LoginData
import com.scharfesicht.attendencesystem.features.attendance.domain.model.Shift
import com.scharfesicht.attendencesystem.features.attendance.domain.model.ShiftInfo
import com.scharfesicht.attendencesystem.features.attendance.domain.model.UserProfile
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceLog
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceData
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    suspend fun login(username: String, password: String, deviceToken: String): Flow<NetworkResult<LoginData>>
    suspend fun refreshUser(): Flow<NetworkResult<LoginData>>
    suspend fun checkIn(request: AttendanceRequest): Flow<NetworkResult<AttendanceRecord>>
    suspend fun middlePunch(request: AttendanceRequest): Flow<NetworkResult<AttendanceRecord>>
    suspend fun checkOut(request: AttendanceRequest): Flow<NetworkResult<AttendanceRecord>>
    suspend fun getLatestRecord(): Flow<NetworkResult<List<AttendanceRecord>>>
    suspend fun getUserShifts(): Flow<NetworkResult<List<Shift>>>
    suspend fun logout(): Flow<NetworkResult<Boolean>>

    // For logs screen - can be mock for now
    suspend fun getAttendanceLogs(month: String, type: String): List<AttendanceLog>
}