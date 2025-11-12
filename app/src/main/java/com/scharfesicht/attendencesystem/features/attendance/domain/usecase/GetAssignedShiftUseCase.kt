package com.scharfesicht.attendencesystem.features.attendance.domain.usecase

import com.scharfesicht.attendencesystem.features.attendance.domain.model.Shift
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAssignedShiftUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    operator fun invoke(): Flow<Result<Shift>> {
        return repository.getAssignedShift()
    }
}