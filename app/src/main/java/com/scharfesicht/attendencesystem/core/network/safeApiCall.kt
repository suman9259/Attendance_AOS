package com.scharfesicht.attendencesystem.core.network

import android.util.Log
import com.scharfesicht.attendencesystem.core.network.interceptor.NoConnectivityException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(
                    message = "Response body is null",
                    code = response.code()
                )
            }
        } else {
            handleErrorResponse(response)
        }
    } catch (e: Exception) {
        handleException(e)
    }
}

private fun <T> handleErrorResponse(response: Response<T>): NetworkResult.Error {
    val errorBody = response.errorBody()?.string()
    val message = when (response.code()) {
        400 -> "Bad Request: $errorBody"
        401 -> "Unauthorized: Please login again"
        403 -> "Forbidden: You don't have permission"
        404 -> "Not Found: Resource doesn't exist"
        408 -> "Request Timeout: Please try again"
        409 -> "Conflict: $errorBody"
        422 -> "Validation Error: $errorBody"
        429 -> "Too Many Requests: Please wait"
        500 -> "Internal Server Error: Please try again later"
        502 -> "Bad Gateway: Server is down"
        503 -> "Service Unavailable: Please try again later"
        504 -> "Gateway Timeout: Server took too long to respond"
        else -> "Error ${response.code()}: ${response.message()}"
    }

    Log.e("SafeApiCall", "API Error: $message")

    return NetworkResult.Error(
        message = message,
        code = response.code()
    )
}

private fun handleException(exception: Exception): NetworkResult.Error {
    Log.e("SafeApiCall", "API Exception", exception)

    return when (exception) {
        is NoConnectivityException -> NetworkResult.Error(
            message = "No internet connection. Please check your network.",
            exception = exception
        )
        is SocketTimeoutException -> NetworkResult.Error(
            message = "Connection timeout. Please try again.",
            exception = exception
        )
        is UnknownHostException -> NetworkResult.Error(
            message = "Unable to connect to server. Please check your connection.",
            exception = exception
        )
        is HttpException -> NetworkResult.Error(
            message = "HTTP Error: ${exception.code()} ${exception.message()}",
            code = exception.code(),
            exception = exception
        )
        is IOException -> NetworkResult.Error(
            message = "Network error occurred. Please try again.",
            exception = exception
        )
        else -> NetworkResult.Error(
            message = exception.message ?: "Unknown error occurred",
            exception = exception
        )
    }
}

fun <T> safeApiCallBlocking(
    apiCall: () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(
                    message = "Response body is null",
                    code = response.code()
                )
            }
        } else {
            handleErrorResponse(response)
        }
    } catch (e: Exception) {
        handleException(e)
    }
}