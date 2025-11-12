package com.scharfesicht.attendencesystem.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthApiForRefresh

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

//    // Special AuthApiService for token refresh ONLY (uses AuthRetrofit - no auth interceptor)
//    @Provides
//    @Singleton
//    @AuthApiForRefresh
//    fun provideAuthApiForRefresh(
//        @AuthRetrofit retrofit: Retrofit
//    ): AuthApiService = retrofit.create(AuthApiService::class.java)
//
//    // Regular AuthApiService for normal use (uses regular Retrofit with all interceptors)
//    @Provides
//    @Singleton
//    fun provideAuthApi(
//        retrofit: Retrofit // This uses the default Retrofit (RegularRetrofit)
//    ): AuthApiService = retrofit.create(AuthApiService::class.java)
//
//    @Provides
//    @Singleton
//    fun provideChildApi(retrofit: Retrofit): ChildApiService =
//        retrofit.create(ChildApiService::class.java)
//
//    @Provides
//    @Singleton
//    fun providePaymentApi(retrofit: Retrofit): PaymentApiService =
//        retrofit.create(PaymentApiService::class.java)
}
