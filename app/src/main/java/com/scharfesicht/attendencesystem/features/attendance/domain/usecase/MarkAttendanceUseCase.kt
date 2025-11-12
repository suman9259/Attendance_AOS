package com.scharfesicht.attendencesystem.features.attendance.domain.usecase

import com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceRecord
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class MarkAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend fun punchIn(location: String? = null): Result<AttendanceRecord> {
        return repository.markPunchIn(location)
    }

    suspend fun punchOut(location: String? = null): Result<AttendanceRecord> {
        return repository.markPunchOut(location)
    }
}