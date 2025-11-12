package com.scharfesicht.attendencesystem.domain.absher.usecase


import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val repository: AbsherRepository
) {
    operator fun invoke(): Flow<Result<UserInfo>> {
        return repository.getUserInfo()
    }
}