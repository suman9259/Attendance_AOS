package com.scharfesicht.attendencesystem.domain.absher.repository

import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import kotlinx.coroutines.flow.Flow

interface AbsherRepository {
    fun getUserInfo(): Flow<Result<UserInfo>>
    suspend fun getUserNationalId(): Result<String>
    suspend fun getUserToken(): Result<String>
    suspend fun getCurrentTheme(): Result<String>
    fun isAbsherInitialized(): Boolean
}