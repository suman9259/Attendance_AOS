package com.scharfesicht.attendencesystem.core.network

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val error: ApiException) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

sealed class ApiException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    data class NetworkException(
        override val message: String = "Network error occurred",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)

    data class ServerException(
        val code: Int,
        override val message: String = "Server error occurred"
    ) : ApiException(message)

    data class UnauthorizedException(
        override val message: String = "Unauthorized access"
    ) : ApiException(message)

    data class ValidationException(
        override val message: String = "Validation error",
        val errors: Map<String, List<String>>? = null
    ) : ApiException(message)

    data class UnknownException(
        override val message: String = "Unknown error occurred",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
}