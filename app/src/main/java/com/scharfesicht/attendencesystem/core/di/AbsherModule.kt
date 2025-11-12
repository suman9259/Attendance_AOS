package com.scharfesicht.attendencesystem.core.di

import com.scharfesicht.attendencesystem.data.absher.repository.AbsherRepositoryImpl
import com.scharfesicht.attendencesystem.data.absher.source.AbsherDataSource
import com.scharfesicht.attendencesystem.data.absher.source.AbsherDataSourceImpl
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AbsherModule {

    @Binds
    @Singleton
    abstract fun bindAbsherDataSource(
        impl: AbsherDataSourceImpl
    ): AbsherDataSource

    @Binds
    @Singleton
    abstract fun bindAbsherRepository(
        impl: AbsherRepositoryImpl
    ): AbsherRepository
}