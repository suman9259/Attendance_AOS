package com.scharfesicht.attendencesystem.core.di

import android.content.Context
import android.util.Log
import com.scharfesicht.attendencesystem.app.AttendanceSystemApp
import com.scharfesicht.attendencesystem.data.absher.repository.AbsherRepositoryImpl
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AbsherModule {

    private const val TAG = "AbsherModule"

    @Provides
    @Singleton
    fun provideAbsherHelper(@ApplicationContext context: Context): IAbsherHelper? {
        val helper = AttendanceSystemApp.absherHelper
        Log.d(TAG, "Providing Absher Helper, found: ${helper != null}")
        return helper
    }

    @Provides
    @Singleton
    fun provideAbsherRepository(absherHelper: IAbsherHelper?): AbsherRepository {
        return AbsherRepositoryImpl(absherHelper)
    }
}
