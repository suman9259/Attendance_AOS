package com.scharfesicht.attendencesystem.features.attendance.data.remote

import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import retrofit2.Response
import retrofit2.http.*

interface AttendanceApiService {

    // 1️⃣ Login
    @POST("api/v1/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginData>>

    // 2️⃣ Refresh user info
    @GET("api/v1/refresh-user")
    suspend fun refreshUser(): Response<ApiResponse<LoginData>>

    // 3️⃣ Check-in
    @POST("api/v1/attendance/checkin")
    suspend fun checkIn(
        @Body body: AttendanceRequest
    ): Response<ApiResponse<AttendanceRecord>>

    // 4️⃣ Middle Punch (optional)
    @POST("api/v1/attendance/middle-punch")
    suspend fun middlePunch(
        @Body body: AttendanceRequest
    ): Response<ApiResponse<AttendanceRecord>>

    // 5️⃣ Check-out
    @POST("api/v1/attendance/checkout")
    suspend fun checkOut(
        @Body body: AttendanceRequest
    ): Response<ApiResponse<AttendanceRecord>>

    // 6️⃣ Latest Attendance Record
    @GET("api/v1/attendance/latest-record")
    suspend fun getLatestRecord(): Response<ApiResponse<List<AttendanceRecord>>>

    // 7️⃣ Get Assigned Shifts
    @GET("api/v1/get-user-shifts")
    suspend fun getUserShifts(): Response<ApiResponse<List<Shift>>>

    // 8️⃣ Logout
    @POST("api/v1/logout")
    suspend fun logout(): Response<ApiResponse<Any>>
}