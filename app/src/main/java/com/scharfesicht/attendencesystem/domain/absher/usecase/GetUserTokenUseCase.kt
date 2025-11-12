package com.scharfesicht.attendencesystem.domain.absher.usecase

import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import javax.inject.Inject

class GetUserTokenUseCase @Inject constructor(
    private val repository: AbsherRepository
) {
    suspend operator fun invoke(): Result<String> {
        return repository.getUserToken()
    }
}