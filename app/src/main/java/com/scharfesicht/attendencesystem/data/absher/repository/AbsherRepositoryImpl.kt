package com.scharfesicht.attendencesystem.data.absher.repository

import android.util.Log
import com.scharfesicht.attendencesystem.app.ui.theme.ThemeMode
import com.scharfesicht.attendencesystem.data.absher.source.AbsherDataSource
import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbsherRepositoryImpl @Inject constructor(
    private val dataSource: AbsherDataSource
) : AbsherRepository {

    override fun getUserInfo(): Flow<Result<UserInfo>> = flow {
        try {
            if (!dataSource.isInitialized()) {
                emit(Result.failure(Exception("Absher SDK not initialized")))
                return@flow
            }

            val nationalIdResponse = dataSource.getUserNationalID()
            val fullNameArResponse = dataSource.getUserFullNameAr()
            val fullNameEnResponse = dataSource.getUserFullNameEn()
            val firstNameArResponse = dataSource.getUserFirstNameAr()
            val tokenResponse = dataSource.getUserToken()
            val themeResponse = dataSource.getCurrentTheme()

            // Check if all responses are successful
            if (nationalIdResponse.success == true &&
                fullNameArResponse.success == true &&
                fullNameEnResponse.success == true &&
                firstNameArResponse.success == true &&
                tokenResponse.success == true &&
                themeResponse.success == true) {

                val userInfo = UserInfo(
                    nationalId = nationalIdResponse.data.orEmpty(),
                    fullNameAr = fullNameArResponse.data.orEmpty(),
                    fullNameEn = fullNameEnResponse.data.orEmpty(),
                    firstNameAr = firstNameArResponse.data.orEmpty(),
                    token = tokenResponse.data.orEmpty(),
                    theme = ThemeMode.fromString(themeResponse.data ?: ThemeMode.LIGHT.name)
                )
                emit(Result.success(userInfo))
            } else {
                val message = listOfNotNull(
                    nationalIdResponse.message,
                    fullNameArResponse.message,
                    fullNameEnResponse.message,
                    firstNameArResponse.message,
                    tokenResponse.message,
                    themeResponse.message
                ).firstOrNull() ?: "Failed to fetch user info"

                emit(Result.failure(Exception(message)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun getUserNationalId(): Result<String> {
        return try {
            val response = dataSource.getUserNationalID()
            val data = response.data
            if (response.success && data != null && data.isNotEmpty()) {
                Result.success(data) // Explicitly using the non-null variable
            } else {
                Result.failure(Exception(response.message ?: "Failed to get national ID"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching national ID", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserToken(): Result<String> {
        return try {
            val response = dataSource.getUserToken()
            val data = response.data
            if (response.success && data != null && data.isNotEmpty()) {
                Result.success(data) // Explicitly using the non-null variable
            } else {
                Result.failure(Exception(response.message ?: "Failed to get token"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching token", e)
            Result.failure(e)
        }
    }

    override suspend fun getCurrentTheme(): Result<String> {
        return try {
            val response = dataSource.getCurrentTheme()
            val data = response.data
            if (response.success && data != null && data.isNotEmpty()) {
                Result.success(data) // Explicitly using the non-null variable
            } else {
                Result.failure(Exception(response.message ?: "Failed to get theme"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching theme", e)
            Result.failure(e)
        }
    }

    override fun isAbsherInitialized(): Boolean {
        return dataSource.isInitialized()
    }

    companion object {
        private const val TAG = "AbsherRepository"
    }
}