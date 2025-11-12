package com.scharfesicht.attendencesystem.core.utils

data class BaseResponseModel<T>(
    val message: String,
    val success: Boolean,
    val data : T?,
    val status: Int
)