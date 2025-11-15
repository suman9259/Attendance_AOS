package com.scharfesicht.attendencesystem.core.di

import com.scharfesicht.attendencesystem.features.attendance.data.repository.AttendanceRepositoryImpl
import com.scharfesicht.attendencesystem.features.attendance.domain.repository.AttendanceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        impl: AttendanceRepositoryImpl
    ): AttendanceRepository
}