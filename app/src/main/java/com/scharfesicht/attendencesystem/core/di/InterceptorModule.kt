package com.scharfesicht.attendencesystem.core.di

import android.content.Context
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import com.scharfesicht.attendencesystem.core.network.interceptor.*
import com.scharfesicht.attendencesystem.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InterceptorModule {

    @Provides
    @Singleton
    fun provideCacheDir(@ApplicationContext context: Context): File {
        return context.cacheDir
    }

    @Provides
    @Singleton
    fun provideApiLoggerInterceptor(): ApiLoggerInterceptor = ApiLoggerInterceptor()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = when {
                BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
                else -> HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
            redactHeader("Cookie")
        }

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferenceStorage: IPreferenceStorage): AuthInterceptor =
        AuthInterceptor(preferenceStorage)

    @Provides
    @Singleton
    fun provideNetworkConnectionInterceptor(
        @ApplicationContext context: Context
    ): NetworkConnectionInterceptor = NetworkConnectionInterceptor(context)

    @Provides
    @Singleton
    fun provideRetryInterceptor(): RetryInterceptor = RetryInterceptor(maxRetries = 3)

    @Provides
    @Singleton
    fun provideCacheInterceptor(
        @ApplicationContext context: Context
    ): CacheInterceptor = CacheInterceptor(context)

    @Provides
    @Singleton
    fun provideRateLimitInterceptor(): RateLimitInterceptor = RateLimitInterceptor()
}