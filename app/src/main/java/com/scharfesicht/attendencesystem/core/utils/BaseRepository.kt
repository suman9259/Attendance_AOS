package com.scharfesicht.attendencesystem.core.utils

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

open class BaseRepository {

    /**
     * Safe API call wrapper.
     * Handles exceptions, HTTP errors, IO errors, and returns ApiResponse.
     */
    /*suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResponse<T> {
        return try {
            val result = apiCall()
            ApiResponse(success = true, data = result, error = null)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = try {
                Gson().fromJson(errorBody, ErrorResponse::class.java)
            } catch (_: Exception) { null }
            ApiResponse(
                success = false,
                data = null,
                error = ApiError(
                    code = e.code(),
                    message = errorResponse?.message ?: "HttpException"
                )
            )
        } catch (e: IOException) {
            ApiResponse(success = false, data = null, error = ApiError(-1, "Network error"))
        } catch (e: Exception) {
            ApiResponse(success = false, data = null, error = ApiError(-1, e.message ?: "Unknown error"))
        }
    }*/
}