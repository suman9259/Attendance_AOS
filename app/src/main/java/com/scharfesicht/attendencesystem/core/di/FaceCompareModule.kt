package com.scharfesicht.attendencesystem.di

import android.content.Context
import com.amazonaws.regions.Regions
import com.scharfesicht.attendencesystem.features.facecompare.data.RekognitionRepository
import com.scharfesicht.attendencesystem.features.facecompare.domain.FaceCompareUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FaceCompareModule {

    private const val COGNITO_POOL_ID = "us-east-1:2f9d3c4f-b428-4c05-9478-5163fae204ef"

    @Provides
    @Singleton
    fun provideRekognitionRepo(
        @ApplicationContext context: Context
    ): RekognitionRepository {
        return RekognitionRepository(
            identityPoolId = COGNITO_POOL_ID,
            region = Regions.US_EAST_1,
            appContext = context
        )
    }

    @Provides
    @Singleton
    fun provideFaceCompareUseCase(
        repo: RekognitionRepository
    ): FaceCompareUseCase {
        return FaceCompareUseCase(repo)
    }
}