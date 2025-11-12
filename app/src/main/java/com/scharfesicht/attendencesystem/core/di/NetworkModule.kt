package com.scharfesicht.attendencesystem.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.scharfesicht.attendencesystem.core.network.TokenAuthenticator
import com.scharfesicht.attendencesystem.core.network.interceptor.*
import com.scharfesicht.attendencesystem.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RegularOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 60L
    private const val CACHE_SIZE = 10L * 1024 * 1024
    private const val MAX_IDLE_CONNECTIONS = 5
    private const val KEEP_ALIVE_DURATION = 5L

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    @Provides
    @Singleton
    fun provideCache(cacheDir: File): Cache {
        return Cache(
            directory = File(cacheDir, "http_cache"),
            maxSize = CACHE_SIZE
        )
    }

    @Provides
    @Singleton
    fun provideConnectionPool(): ConnectionPool {
        return ConnectionPool(
            maxIdleConnections = MAX_IDLE_CONNECTIONS,
            keepAliveDuration = KEEP_ALIVE_DURATION,
            timeUnit = TimeUnit.MINUTES
        )
    }

    // Special OkHttpClient for token refresh (NO TokenAuthenticator to break cycle)
    @Provides
    @Singleton
    @AuthOkHttpClient
    fun provideAuthOkHttpClient(
        networkInterceptor: NetworkConnectionInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        apiLoggerInterceptor: ApiLoggerInterceptor,
        cache: Cache,
        connectionPool: ConnectionPool
    ): OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(networkInterceptor)

        if (BuildConfig.DEBUG) {
            addInterceptor(loggingInterceptor)
        } else {
            addInterceptor(apiLoggerInterceptor)
        }

        cache(cache)

        connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        callTimeout(READ_TIMEOUT + CONNECT_TIMEOUT, TimeUnit.SECONDS)

        connectionPool(connectionPool)
        retryOnConnectionFailure(true)
        followRedirects(true)
        followSslRedirects(true)
    }.build()

    // Regular OkHttpClient with full interceptors including TokenAuthenticator
    @Provides
    @Singleton
    @RegularOkHttpClient
    fun provideRegularOkHttpClient(
        authInterceptor: AuthInterceptor,
        networkInterceptor: NetworkConnectionInterceptor,
        retryInterceptor: RetryInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        apiLoggerInterceptor: ApiLoggerInterceptor,
        cacheInterceptor: CacheInterceptor,
        rateLimitInterceptor: RateLimitInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        cache: Cache,
        connectionPool: ConnectionPool
    ): OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(networkInterceptor)
        addInterceptor(rateLimitInterceptor)
        addInterceptor(authInterceptor)
        addInterceptor(cacheInterceptor)
        cache(cache)

        if (BuildConfig.DEBUG) {
            addInterceptor(loggingInterceptor)
        } else {
            addInterceptor(apiLoggerInterceptor)
        }

        addNetworkInterceptor(retryInterceptor)
        authenticator(tokenAuthenticator)

        connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        callTimeout(READ_TIMEOUT + CONNECT_TIMEOUT, TimeUnit.SECONDS)

        connectionPool(connectionPool)
        retryOnConnectionFailure(true)
        followRedirects(true)
        followSslRedirects(true)
    }.build()

    // Default OkHttpClient (points to regular one)
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @RegularOkHttpClient okHttpClient: OkHttpClient
    ): OkHttpClient = okHttpClient
}