package com.scharfesicht.attendencesystem.core.di

import com.google.gson.Gson
import com.scharfesicht.attendencesystem.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RegularRetrofit

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    // Retrofit for token refresh (uses AuthOkHttpClient)
    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(
        @AuthOkHttpClient okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Regular Retrofit (uses RegularOkHttpClient)
    @Provides
    @Singleton
    @RegularRetrofit
    fun provideRegularRetrofit(
        @RegularOkHttpClient okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Default Retrofit (points to regular one)
    @Provides
    @Singleton
    fun provideRetrofit(
        @RegularRetrofit retrofit: Retrofit
    ): Retrofit = retrofit
}