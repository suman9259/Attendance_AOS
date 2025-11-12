package com.scharfesicht.attendencesystem.core.di

import com.scharfesicht.attendencesystem.features.attendance.data.remote.AttendanceApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAttendanceApi(retrofit: Retrofit): AttendanceApiService =
        retrofit.create(AttendanceApiService::class.java)
}
