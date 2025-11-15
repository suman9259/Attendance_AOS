package com.scharfesicht.attendencesystem.features.attendance.data.repository

import android.util.Log
import com.scharfesicht.attendencesystem.core.network.ApiException
import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.features.attendance.data.remote.AttendanceApiService
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import com.scharfesicht.attendencesystem.features.attendance.presentation.ui.AttendanceLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val apiService: AttendanceApiService
) : AttendanceRepository {

    companion object {
        private const val TAG = "AttendanceRepository"
    }

    override suspend fun login(
        username: String,
        password: String,
        deviceToken: String
    ): Flow<NetworkResult<LoginData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.login(
                LoginRequest(username, password, deviceToken)
            )
            emit(handleResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            emit(NetworkResult.Error(handleException(e)))
        }
    }

    override suspend fun refreshUser(): Flow<NetworkResult<LoginData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.refreshUser()
            emit(handleResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Refresh user error", e)
            emit(NetworkResult.Error(handleException(e)))
        }
    }

    override suspend fun checkIn(
        request: AttendanceRequest
    ): Flow<NetworkResult<AttendanceRecord>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.checkIn(request)
            emit(handleResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Check-in error", e)
            emit(NetworkResult.Error(handleException(e)))
        }
    }

    override suspend fun middlePunch(
        request: AttendanceRequest
    ): Flow<NetworkResult<AttendanceRecord>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.middlePunch(request)
            emit(handleResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Middle punch error", e)
            emit(NetworkResult.Error(handleException(e)))
        }
    }

    override suspend fun checkOut(
        request: AttendanceRequest
    ): Flow<NetworkResult<AttendanceRecord>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.checkOut(request)
            emit(handleResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Check-out error", e)
            emit(NetworkResult.Error(handleException(e)))
        }
    }

    override suspend fun getLatestRecord(): Flow<NetworkResult<List<AttendanceRecord>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getLatestRecord()
            emit(handleResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Get latest record error", e)
            emit(NetworkResult.Error(handleException(e)))
        }
    }

    override suspend fun getUserShifts(): Flow<NetworkResult<List<Shift>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getUserShifts()
            emit(handleResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Get user shifts error", e)
            emit(NetworkResult.Error(handleException(e)))
        }
    }

    override suspend fun logout(): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error(ApiException.ServerException(
                    code = response.code(),
                    message = response.message()
                )))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Logout error", e)
            emit(NetworkResult.Error(handleException(e)))
        }
    }

    // Mock implementation for getAttendanceLogs
    override suspend fun getAttendanceLogs(month: String, type: String): List<AttendanceLog> {
        // TODO: Replace with actual API call when endpoint is available
        // For now, return mock data
        return listOf(
            AttendanceLog(
                dayName = "Thursday",
                dayNumber = "15",
                punchInTime = "09:15am",
                punchOutTime = "05:45pm",
                workingHours = "08h30m"
            ),
            AttendanceLog(
                dayName = "Friday",
                dayNumber = "16",
                punchInTime = "09:00am",
                punchOutTime = "05:30pm",
                workingHours = "08h30m"
            )
        )
    }

    // Helper function to handle API responses
    private fun <T> handleResponse(
        response: Response<ApiResponse<T>>
    ): NetworkResult<T> {
        return when {
            response.isSuccessful -> {
                response.body()?.let { apiResponse ->
                    apiResponse.data?.let { data ->
                        NetworkResult.Success(data)
                    } ?: NetworkResult.Error(
                        ApiException.ServerException(
                            code = response.code(),
                            message = apiResponse.message
                        )
                    )
                } ?: NetworkResult.Error(
                    ApiException.ServerException(
                        code = response.code(),
                        message = "Empty response"
                    )
                )
            }
            response.code() == 401 -> {
                NetworkResult.Error(
                    ApiException.UnauthorizedException("Session expired")
                )
            }
            response.code() in 400..499 -> {
                NetworkResult.Error(
                    ApiException.ValidationException(
                        message = response.message()
                    )
                )
            }
            response.code() in 500..599 -> {
                NetworkResult.Error(
                    ApiException.ServerException(
                        code = response.code(),
                        message = "Server error"
                    )
                )
            }
            else -> {
                NetworkResult.Error(
                    ApiException.UnknownException(
                        message = response.message()
                    )
                )
            }
        }
    }

    // Helper function to handle exceptions
    private fun handleException(exception: Exception): ApiException {
        return when (exception) {
            is java.net.UnknownHostException -> {
                ApiException.NetworkException(
                    message = "No internet connection",
                    cause = exception
                )
            }
            is java.net.SocketTimeoutException -> {
                ApiException.NetworkException(
                    message = "Request timeout",
                    cause = exception
                )
            }
            is java.io.IOException -> {
                ApiException.NetworkException(
                    message = "Network error",
                    cause = exception
                )
            }
            is ApiException -> exception
            else -> ApiException.UnknownException(
                message = exception.message ?: "Unknown error",
                cause = exception
            )
        }
    }
}