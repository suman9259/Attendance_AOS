package com.scharfesicht.attendencesystem.core.di

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AwsModule {

    private const val IDENTITY_POOL_ID = "us-east-1:2f9d3c4f-b428-4c05-9478-5163fae204ef"

    @Provides
    @Singleton
    fun provideRekognitionClient(@ApplicationContext context: Context): AmazonRekognitionClient {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context, IDENTITY_POOL_ID, Regions.US_EAST_1
        )
        return AmazonRekognitionClient(credentialsProvider).apply {
            setRegion(Region.getRegion(Regions.US_EAST_1))
        }
    }
}
