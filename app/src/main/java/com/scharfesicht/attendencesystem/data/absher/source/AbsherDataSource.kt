package com.scharfesicht.attendencesystem.data.absher.source

import sa.gov.moi.absherinterior.core_logic.AbsherResponse


interface AbsherDataSource {
    suspend fun getUserNationalID(): AbsherResponse<String>
    suspend fun getUserFullNameAr(): AbsherResponse<String>
    suspend fun getUserFullNameEn(): AbsherResponse<String>
    suspend fun getUserFirstNameAr(): AbsherResponse<String>
    suspend fun getUserToken(): AbsherResponse<String>
    suspend fun getCurrentTheme(): AbsherResponse<String>
    fun isInitialized(): Boolean
}