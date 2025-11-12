package com.scharfesicht.attendencesystem.features.attendance.domain.usecase

import com.scharfesicht.attendencesystem.features.attendance.domain.model.LoginRequest
import com.scharfesicht.attendencesystem.features.attendance.domain.model.LoginResponse
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(request: LoginRequest): Result<LoginResponse> {
        return repository.login(request)
    }
}