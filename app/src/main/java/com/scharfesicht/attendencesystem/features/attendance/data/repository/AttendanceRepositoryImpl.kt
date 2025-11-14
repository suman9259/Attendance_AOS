package com.scharfesicht.attendencesystem.features.attendance.data.repository

import android.util.Log
import com.scharfesicht.attendencesystem.features.attendance.data.remote.AttendanceApiService
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceLog
import com.scharfesicht.attendencesystem.presentation.dashboard.AttendanceData
import com.scharfesicht.attendencesystem.presentation.dashboard.AttendanceSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

//@Singleton
//class AttendanceRepositoryImpl @Inject constructor(
//    private val apiService: AttendanceApiService
//) : AttendanceRepository {
//
//    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
//        return try {
//            val response = apiService.login(request)
//            if (response.isSuccessful && response.body() != null) {
//                val loginResponse = response.body()!!
////                loginResponse.data?.token?.let { tokenManager.saveJwtToken(it) }
//                Result.success(loginResponse)
//            } else {
//                Result.failure(Exception("Login failed: ${response.message()}"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override fun getAssignedShift(): Flow<Result<ShiftData>> = flow {
//        try {
//            val response = apiService.getUserShifts()
//            if (response.isSuccessful) {
//                val shifts = response.body()
//                val shift = shifts?.firstOrNull()?.let {
////                    ShiftData(
////                        id = it.id,
////                        name = it.name,
////                        nameAr = it.name_ar,
////                        startTime = LocalDateTime.parse(it.start_time).toLocalTime(),
////                        endTime = LocalDateTime.parse(it.end_time).toLocalTime(),
////                        type = ShiftType.valueOf(it.type.uppercase())
////                    )
//                }
////                emit(Result.success(shift!!))
//            } else {
//                emit(Result.failure(Exception("Failed to load shifts")))
//            }
//        } catch (e: Exception) {
//            Log.e("AttendanceRepo", "Shift error", e)
//            emit(Result.failure(e))
//        }
//    }
//
//    override suspend fun markPunchIn(location: String?): Result<AttendanceRecord> {
//        return try {
//            val body = AttendanceRequest(
//                latitude = null,
//                longitude = null,
//                timestamp = LocalDateTime.now().toString()
//            )
//            val response = apiService.checkIn(body)
//            if (response.isSuccessful) {
//                val record = response.body()?.toDomain()
//                Result.success(record!!)
//            } else {
//                Result.failure(Exception("Check-in failed"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun markPunchOut(location: String?): Result<AttendanceRecord> {
//        return try {
//            val body = AttendanceRequest(
//                latitude = null,
//                longitude = null,
//                timestamp = LocalDateTime.now().toString()
//            )
//            val response = apiService.checkOut(body)
//            if (response.isSuccessful) {
//                val record = response.body()?.toDomain()
//                Result.success(record!!)
//            } else {
//                Result.failure(Exception("Check-out failed"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override fun getTodayAttendance(): Flow<Result<AttendanceRecord?>> = flow {
//        try {
//            val response = apiService.getLatestRecord()
//            if (response.isSuccessful) {
//                emit(Result.success(response.body()?.toDomain()))
//            } else {
//                emit(Result.failure(Exception("Failed to get attendance record")))
//            }
//        } catch (e: Exception) {
//            emit(Result.failure(e))
//        }
//    }
//
//    // Mapping extension
//    private fun AttendanceResponse.toDomain(): AttendanceRecord {
//        return AttendanceRecord(
//            id = id ?: "",
//            userId = userId ?: "",
//            punchInTime = punchInTime?.let { LocalDateTime.parse(it) },
//            punchOutTime = punchOutTime?.let { LocalDateTime.parse(it) },
//            shiftId = shiftId ?: "",
//            date = date?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now(),
//            status = AttendanceStatus.valueOf(status?.uppercase() ?: "PRESENT"),
//            location = location
//        )
//    }
//}

/**
 * Repository implementation for attendance data
 * Uses mock data for now - replace with actual API calls
 */
class AttendanceRepositoryImpl @Inject constructor(
    // Add your API service here when ready
    // private val apiService: AttendanceApiService
) : AttendanceRepository {

    override suspend fun getAttendanceData(): AttendanceData {
        // Simulate network delay
        delay(1000)

        // Mock data - replace with actual API call
        return AttendanceData(
            upcomingHoliday = "OCT 12",
            shiftName = "standard Shift",
            shiftTime = "07:00 AM - 12:00 PM",
            summary = AttendanceSummary(
                attendance = 16,
                lateLessThan1h = 8,
                lateMoreThan1h = 4,
                earlyPunchOut = 6,
                absence = 2
            )
        )
    }

    override suspend fun getAttendanceLogs(month: String, type: String): List<AttendanceLog> {
        // Simulate network delay
        delay(1000)

        // Mock data - replace with actual API call
        return listOf(
            AttendanceLog(
                dayName = "الخميس",
                dayNumber = "15",
                punchInTime = "09:15am",
                punchOutTime = "05:45pm",
                workingHours = "08h30m"
            ),
            AttendanceLog(
                dayName = "الخميس",
                dayNumber = "15",
                punchInTime = "09:15am",
                punchOutTime = "05:45pm",
                workingHours = "08h30m"
            ),
            AttendanceLog(
                dayName = "الخميس",
                dayNumber = "15",
                punchInTime = "09:15am",
                punchOutTime = "05:45pm",
                workingHours = "08h30m"
            ),
            AttendanceLog(
                dayName = "الخميس",
                dayNumber = "15",
                punchInTime = "09:15am",
                punchOutTime = "",
                workingHours = ""
            )
        )
    }

    override suspend fun validateLocation(latitude: Double, longitude: Double): Boolean {
        // Implement actual location validation logic here
        // For now, return true for demo purposes

        // Example validation:
        // val allowedLocation = LatLng(24.7136, 46.6753) // Office location
        // val distance = calculateDistance(latitude, longitude, allowedLocation)
        // return distance < 100 // Within 100 meters

        return true
    }

    override suspend fun punchIn(latitude: Double, longitude: Double): Boolean {
        // Simulate API call
        delay(500)

        // TODO: Replace with actual API call
        // Example:
        // val response = apiService.punchIn(
        //     PunchInRequest(
        //         latitude = latitude,
        //         longitude = longitude,
        //         timestamp = System.currentTimeMillis()
        //     )
        // )
        // return response.isSuccessful

        return true
    }

    override suspend fun punchOut(latitude: Double, longitude: Double): Boolean {
        // Simulate API call
        delay(500)

        // TODO: Replace with actual API call
        // Example:
        // val response = apiService.punchOut(
        //     PunchOutRequest(
        //         latitude = latitude,
        //         longitude = longitude,
        //         timestamp = System.currentTimeMillis()
        //     )
        // )
        // return response.isSuccessful

        return true
    }
}