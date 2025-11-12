package com.scharfesicht.attendencesystem.core.network

sealed class NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>()

    data class Error(
        val message: String,
        val code: Int? = null,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()

    object Loading : NetworkResult<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: Exception(message)
        is Loading -> throw IllegalStateException("Cannot get data from Loading state")
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
    }

    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Error) -> Unit): NetworkResult<T> {
        if (this is Error) action(this)
        return this
    }

    inline fun onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is Loading) action()
        return this
    }
}

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(message, code, exception)
        is NetworkResult.Loading -> NetworkResult.Loading
    }
}

inline fun <T, R> NetworkResult<T>.flatMap(transform: (T) -> NetworkResult<R>): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> transform(data)
        is NetworkResult.Error -> NetworkResult.Error(message, code, exception)
        is NetworkResult.Loading -> NetworkResult.Loading
    }
}