package com.scharfesicht.attendencesystem.features.attendance.data.repository

import android.util.Log
import com.scharfesicht.attendencesystem.features.attendance.data.remote.AttendanceApiService
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val apiService: AttendanceApiService
) : AttendanceRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
//                loginResponse.data?.token?.let { tokenManager.saveJwtToken(it) }
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAssignedShift(): Flow<Result<ShiftData>> = flow {
        try {
            val response = apiService.getUserShifts()
            if (response.isSuccessful) {
                val shifts = response.body()
                val shift = shifts?.firstOrNull()?.let {
//                    ShiftData(
//                        id = it.id,
//                        name = it.name,
//                        nameAr = it.name_ar,
//                        startTime = LocalDateTime.parse(it.start_time).toLocalTime(),
//                        endTime = LocalDateTime.parse(it.end_time).toLocalTime(),
//                        type = ShiftType.valueOf(it.type.uppercase())
//                    )
                }
//                emit(Result.success(shift!!))
            } else {
                emit(Result.failure(Exception("Failed to load shifts")))
            }
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Shift error", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun markPunchIn(location: String?): Result<AttendanceRecord> {
        return try {
            val body = AttendanceRequest(
                latitude = null,
                longitude = null,
                timestamp = LocalDateTime.now().toString()
            )
            val response = apiService.checkIn(body)
            if (response.isSuccessful) {
                val record = response.body()?.toDomain()
                Result.success(record!!)
            } else {
                Result.failure(Exception("Check-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markPunchOut(location: String?): Result<AttendanceRecord> {
        return try {
            val body = AttendanceRequest(
                latitude = null,
                longitude = null,
                timestamp = LocalDateTime.now().toString()
            )
            val response = apiService.checkOut(body)
            if (response.isSuccessful) {
                val record = response.body()?.toDomain()
                Result.success(record!!)
            } else {
                Result.failure(Exception("Check-out failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTodayAttendance(): Flow<Result<AttendanceRecord?>> = flow {
        try {
            val response = apiService.getLatestRecord()
            if (response.isSuccessful) {
                emit(Result.success(response.body()?.toDomain()))
            } else {
                emit(Result.failure(Exception("Failed to get attendance record")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Mapping extension
    private fun AttendanceResponse.toDomain(): AttendanceRecord {
        return AttendanceRecord(
            id = id ?: "",
            userId = userId ?: "",
            punchInTime = punchInTime?.let { LocalDateTime.parse(it) },
            punchOutTime = punchOutTime?.let { LocalDateTime.parse(it) },
            shiftId = shiftId ?: "",
            date = date?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now(),
            status = AttendanceStatus.valueOf(status?.uppercase() ?: "PRESENT"),
            location = location
        )
    }
}
