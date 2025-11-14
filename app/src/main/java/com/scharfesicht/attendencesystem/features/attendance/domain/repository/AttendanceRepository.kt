package com.scharfesicht.attendencesystem.features.attendance.domain.repository
//
//import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
//import kotlinx.coroutines.flow.Flow
//
//interface AttendanceRepository {
//
//    suspend fun login(request: LoginRequest): Result<LoginResponse>
//    fun getAssignedShift(): Flow<Result<ShiftData>>
//    suspend fun markPunchIn(location: String?): Result<AttendanceRecord>
//    suspend fun markPunchOut(location: String?): Result<AttendanceRecord>
//    fun getTodayAttendance(): Flow<Result<AttendanceRecord?>>
//}


import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceLog
import com.scharfesicht.attendencesystem.presentation.dashboard.AttendanceData

interface AttendanceRepository {
    suspend fun getAttendanceData(): AttendanceData
    suspend fun getAttendanceLogs(month: String, type: String): List<AttendanceLog>
    suspend fun validateLocation(latitude: Double, longitude: Double): Boolean
    suspend fun punchIn(latitude: Double, longitude: Double): Boolean
    suspend fun punchOut(latitude: Double, longitude: Double): Boolean
}