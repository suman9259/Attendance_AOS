package com.scharfesicht.attendencesystem.domain.absher.repository

import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import kotlinx.coroutines.flow.Flow

interface AbsherRepository {
    fun isAbsherInitialized(): Boolean
    fun getUserInfo(): Flow<Result<UserInfo>>
    fun getUserToken(): Result<String>
    fun getCurrentTheme(): Result<String>
    fun getCurrentLanguage(): Result<String>
    fun getUserProfileImage(): Result<String>
    fun saveStringToLocal(key: String, value: String): Result<Boolean>
    fun readStringFromLocal(key: String): Result<String>
    fun getLocation(): Result<AbsherPosition>
    fun closeApp(data: Map<String, Any>?)
}

data class AbsherPosition(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double?
)