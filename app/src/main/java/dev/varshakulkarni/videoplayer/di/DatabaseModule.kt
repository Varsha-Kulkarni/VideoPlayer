package dev.varshakulkarni.videoplayer.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.varshakulkarni.videoplayer.data.db.VideoDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(application: Application) = VideoDatabase.getInstance(application)

    @Singleton
    @Provides
    fun provideVideoDao(database: VideoDatabase) = database.getVideosDao()
}