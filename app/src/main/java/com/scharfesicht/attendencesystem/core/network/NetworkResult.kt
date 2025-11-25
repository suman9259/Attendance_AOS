package com.scharfesicht.attendencesystem.core.network

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val error: ApiException) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

sealed class ApiException(
    open val msg: String,
    open val errorCause: Throwable? = null
) : Exception(msg, errorCause) {

    data class NetworkException(
        override val msg: String = "Network error occurred",
        override val errorCause: Throwable? = null
    ) : ApiException(msg, errorCause)

    data class ServerException(
        val code: Int,
        override val msg: String = "Server error occurred",
        override val errorCause: Throwable? = null
    ) : ApiException(msg, errorCause)

    data class UnauthorizedException(
        override val msg: String = "Unauthorized access",
        override val errorCause: Throwable? = null
    ) : ApiException(msg, errorCause)

    data class ValidationException(
        override val msg: String = "Validation error",
        val errors: Map<String, List<String>>? = null,
        override val errorCause: Throwable? = null
    ) : ApiException(msg, errorCause)

    data class UnknownException(
        override val msg: String = "Unknown error occurred",
        override val errorCause: Throwable? = null
    ) : ApiException(msg, errorCause)
}