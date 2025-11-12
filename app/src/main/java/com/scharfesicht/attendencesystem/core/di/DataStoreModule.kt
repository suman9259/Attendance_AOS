package com.scharfesicht.attendencesystem.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.scharfesicht.attendencesystem.core.datastore.IPreferenceStorage
import com.scharfesicht.attendencesystem.core.datastore.PreferenceKeys.APP_DATASTORE_NAME
import com.scharfesicht.attendencesystem.core.datastore.PreferenceStorageImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = APP_DATASTORE_NAME
    )

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    // Bind the interface to the implementation
    @Provides
    @Singleton
    fun providePreferenceStorage(preferenceStorageImpl: PreferenceStorageImpl): IPreferenceStorage =
        preferenceStorageImpl
}
