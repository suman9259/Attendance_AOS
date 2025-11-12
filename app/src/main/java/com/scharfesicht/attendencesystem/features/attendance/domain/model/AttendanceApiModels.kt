package com.scharfesicht.attendencesystem.features.attendance.domain.model

import com.google.gson.annotations.SerializedName
import kotlin.random.Random

data class BaseApiResponse(
    val success: Boolean,
    val message: String?
)

data class AttendanceRequest(
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("timestamp") val timestamp: String // ISO 8601
)

data class AttendanceResponse(
    val id: String?,
    val userId: String?,
    val punchInTime: String?,
    val punchOutTime: String?,
    val shiftId: String?,
    val date: String?,
    val status: String?,
    val location: String?
)

data class ShiftResponse(
    val id: String,
    val name: String,
    val name_ar: String,
    val start_time: String,
    val end_time: String,
    val type: String
)

data class LoginRequest(
    val username: String = "rahul_kumar_1999",
    val password: String = "Rahul@#2131",
    val device_token: String? = Random.nextInt(1,100000).toString(),
    val language: String? = null,
)


data class UserProfileResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)
