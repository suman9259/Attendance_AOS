package com.scharfesicht.attendencesystem.features.attendance.domain.usecase

import com.scharfesicht.attendencesystem.features.attendance.domain.model.Holiday
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUpcomingHolidayUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    operator fun invoke(): Flow<Result<Holiday?>> {
        return repository.getUpcomingHoliday()
    }
}