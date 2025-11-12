package com.scharfesicht.attendencesystem.data.absher.source

import android.util.Log
import sa.gov.moi.absherinterior.core_logic.AbsherResponse
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbsherDataSourceImpl @Inject constructor() : AbsherDataSource {

    @Volatile
    private var absherHelper: IAbsherHelper? = null

    fun setAbsherHelper(helper: IAbsherHelper?) {
        absherHelper = helper
        Log.d(TAG, "AbsherHelper set: ${helper != null}")
    }

    override fun isInitialized(): Boolean = absherHelper != null

    override suspend fun getUserNationalID(): AbsherResponse<String> {
        return absherHelper?.getUserNationalID()
            ?: createErrorResponse("AbsherHelper not initialized")
    }

    override suspend fun getUserFullNameAr(): AbsherResponse<String> {
        return absherHelper?.getUserFullNameAr()
            ?: createErrorResponse("AbsherHelper not initialized")
    }

    override suspend fun getUserFullNameEn(): AbsherResponse<String> {
        return absherHelper?.getUserFullNameEn()
            ?: createErrorResponse("AbsherHelper not initialized")
    }

    override suspend fun getUserFirstNameAr(): AbsherResponse<String> {
        return absherHelper?.getUserFirstNameAr()
            ?: createErrorResponse("AbsherHelper not initialized")
    }

    override suspend fun getUserToken(): AbsherResponse<String> {
        return absherHelper?.getUserToken()
            ?: createErrorResponse("AbsherHelper not initialized")
    }

    override suspend fun getCurrentTheme(): AbsherResponse<String> {
        return absherHelper?.getCurrentTheme()
            ?: createErrorResponse("AbsherHelper not initialized")
    }

    private fun createErrorResponse(message: String): AbsherResponse<String> {
        Log.e(TAG, message)
        return AbsherResponse(
            success = false,
            data = null,
            message = message
        )
    }

    companion object {
        private const val TAG = "AbsherDataSource"
    }
}