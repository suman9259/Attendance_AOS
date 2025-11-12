package com.scharfesicht.attendencesystem.features.attendance.domain.usecase

import com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceSummary
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAttendanceSummaryUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    operator fun invoke(month: String): Flow<Result<List<AttendanceSummary>>> {
        return repository.getAttendanceSummary(month)
    }
}