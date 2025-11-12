package com.scharfesicht.attendencesystem.core.di

import com.scharfesicht.attendencesystem.app.navigation.DefaultNavManager
import com.scharfesicht.attendencesystem.app.navigation.NavManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindNavManager(
        impl: DefaultNavManager
    ): NavManager
}
