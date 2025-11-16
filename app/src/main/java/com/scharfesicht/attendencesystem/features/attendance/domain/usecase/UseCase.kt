package com.scharfesicht.attendencesystem.features.attendance.domain.usecase

import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


// ========== SAFE DATA GENERATE ==========
private fun now(): String = try {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
} catch (e: Exception) {
    // fallback — NEVER crash because of date formatter
    ""
}

// ========== LOGIN ==========
class LoginUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        deviceToken: String = ""
    ): Flow<NetworkResult<LoginData>> {
        return repository.login(
            username.trim(),
            password.trim(),
            deviceToken.trim()
        )
    }
}

// ========== REFRESH USER USE CASE ==========

class RefreshUserUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): Flow<NetworkResult<LoginData>> {
        return repository.refreshUser()
    }
}

// ========== CHECK-IN USE CASE ==========

class CheckInUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        zoneId: Int? = null,
        mediaPath: String? = null,
        shiftIndex: Int = 0,
        authDeviceType: Int? = null,
        authDeviceValue: String? = null
    ): Flow<NetworkResult<AttendanceRecord>> {

        val request = AttendanceRequest(
            checkin_time = now(),
            checkin_coordinates = "$latitude,$longitude",
            checkin_zone_id = zoneId,
            checkin_media = mediaPath,
            shift_index = shiftIndex,
            checkin_auth_device_type = authDeviceType,
            checkin_auth_device_value = authDeviceValue
        )

        return repository.checkIn(request)
    }
}

// ========== MIDDLE PUNCH USE CASE ==========

class MiddlePunchUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        zoneId: Int? = null,
        mediaPath: String? = null,
        shiftIndex: Int = 0,
        authDeviceType: Int? = null,
        authDeviceValue: String? = null
    ): Flow<NetworkResult<AttendanceRecord>> {

        val request = AttendanceRequest(
            checkin_time = now(),
            checkin_coordinates = "$latitude,$longitude",
            checkin_zone_id = zoneId,
            checkin_media = mediaPath,
            shift_index = shiftIndex,
            checkin_auth_device_type = authDeviceType,
            checkin_auth_device_value = authDeviceValue
        )
        return repository.middlePunch(request)
    }
}

// ========== CHECK-OUT USE CASE ==========

class CheckOutUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        zoneId: Int? = null,
        mediaPath: String? = null,
        shiftIndex: Int = 0,
        authDeviceType: Int? = null,
        authDeviceValue: String? = null
    ): Flow<NetworkResult<AttendanceRecord>> {

        val coords = "$latitude,$longitude"

        val request = AttendanceRequest(
            checkout_time = now(),
            checkout_coordinates = coords,
            checkout_zone_id = zoneId,
            checkout_media = mediaPath,
            shift_index = shiftIndex,
            checkout_auth_device_type = authDeviceType,
            checkout_auth_device_value = authDeviceValue,
            checkin_coordinates = coords // required by backend sometimes
        )

        return repository.checkOut(request)
    }
}

// ========== GET LATEST RECORD USE CASE ==========

class GetLatestRecordUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): Flow<NetworkResult<List<AttendanceRecord>>> {
        return repository.getLatestRecord()
    }
}

// ========== GET USER SHIFTS USE CASE ==========

class GetUserShiftsUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): Flow<NetworkResult<List<Shift>>> {
        return repository.getUserShifts()
    }
}

// ========== LOGOUT USE CASE ==========

class LogoutUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): Flow<NetworkResult<Boolean>> {
        return repository.logout()
    }
}
