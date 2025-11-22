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
import kotlinx.coroutines.flow.catch
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val apiService: AttendanceApiService
) : AttendanceRepository {

    companion object {
        private const val TAG = "AttendanceRepository"
    }

    private fun <T> safeApiCall(apiCall: suspend () -> Response<ApiResponse<T>>)
            : Flow<NetworkResult<T>> = flow {

        emit(NetworkResult.Loading)

        val response = apiCall()
        emit(handleResponse(response))

    }.catch { e ->
        Log.e(TAG, "API error", e)
        emit(NetworkResult.Error(handleException(e)))
    }

    override suspend fun login(
        username: String,
        password: String,
        deviceToken: String
    ): Flow<NetworkResult<LoginData>> =
        safeApiCall {
            apiService.login(LoginRequest(username, password, deviceToken))
        }

    override suspend fun refreshUser(): Flow<NetworkResult<LoginData>> =
        safeApiCall { apiService.refreshUser() }

    override suspend fun checkIn(
        request: AttendanceRequest
    ): Flow<NetworkResult<AttendanceRecord>> =
        safeApiCall { apiService.checkIn(request) }

    override suspend fun middlePunch(
        request: AttendanceRequest
    ): Flow<NetworkResult<AttendanceRecord>> =
        safeApiCall { apiService.middlePunch(request) }

    override suspend fun checkOut(
        request: AttendanceRequest
    ): Flow<NetworkResult<AttendanceRecord>> =
        safeApiCall { apiService.checkOut(request) }

    override suspend fun getLatestRecord()
            : Flow<NetworkResult<List<AttendanceRecord>>> =
        safeApiCall { apiService.getLatestRecord() }

    override suspend fun getUserShifts()
            : Flow<NetworkResult<List<Shift>>> =
        safeApiCall { apiService.getUserShifts() }

    override suspend fun logout(): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)

        val response = apiService.logout()

        if (response.isSuccessful) {
            emit(NetworkResult.Success(true))
        } else {
            emit(
                NetworkResult.Error(
                    ApiException.ServerException(
                        code = response.code(),
                        msg = response.message()
                    )
                )
            )
        }

    }.catch { e ->
        Log.e(TAG, "Logout error", e)
        emit(NetworkResult.Error(handleException(e)))
    }

    override suspend fun getAttendanceLogs(month: String, type: String): List<AttendanceLog> {
        return listOf(
            AttendanceLog("Thursday", "15", "09:15am", "05:45pm", "08h30m"),
            AttendanceLog("Friday", "16", "09:00am", "05:30pm", "08h30m")
        )
    }

    private fun <T> handleResponse(
        response: Response<ApiResponse<T>>
    ): NetworkResult<T> {

        // ---- Extract error message (if any) ----
        val errorMessage: String? = try {
            val errorJson = response.errorBody()?.string()
            if (!errorJson.isNullOrBlank()) {
                // Example expected: {"message":"Account already logged in"}
                JSONObject(errorJson).optString("message", null)
            } else null
        } catch (e: Exception) {
            null
        }

        // ---- Success handling ----
        if (response.isSuccessful) {
            val apiBody = response.body()
            val data = apiBody?.data

            return if (data != null) {
                NetworkResult.Success(data)
            } else {
                NetworkResult.Error(
                    ApiException.ServerException(
                        code = response.code(),
                        msg = apiBody?.message ?: errorMessage ?: "Empty response"
                    )
                )
            }
        }

        // ---- Unauthorized (401) ----
        if (response.code() == 401) {
            return NetworkResult.Error(
                ApiException.UnauthorizedException(
                    msg = errorMessage ?: response.body()?.message ?: "Unauthorized access"
                )
            )
        }

        // ---- Client Errors (400–499) ----
        if (response.code() in 400..499) {
            return NetworkResult.Error(
                ApiException.ValidationException(
                    msg = errorMessage ?: response.message()
                )
            )
        }

        // ---- Server Errors (500–599) ----
        if (response.code() in 500..599) {
            return NetworkResult.Error(
                ApiException.ServerException(
                    code = response.code(),
                    msg = errorMessage ?: "Server error"
                )
            )
        }

        // ---- Unknown Error ----
        return NetworkResult.Error(
            ApiException.UnknownException(
                msg = errorMessage ?: response.message()
            )
        )
    }

    private fun handleException(exception: Throwable): ApiException {
        return when (exception) {
            is java.net.UnknownHostException ->
                ApiException.NetworkException("No internet connection", exception)

            is java.net.SocketTimeoutException ->
                ApiException.NetworkException("Request timeout", exception)

            is java.io.IOException ->
                ApiException.NetworkException("Network error", exception)

            is ApiException -> exception

            else -> ApiException.UnknownException(
                msg = exception.message ?: "Unknown error",
                errorCause = exception
            )
        }
    }
}