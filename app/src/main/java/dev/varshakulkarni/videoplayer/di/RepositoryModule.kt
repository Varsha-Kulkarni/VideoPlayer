package dev.varshakulkarni.videoplayer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.varshakulkarni.videoplayer.data.VideoDataSource
import dev.varshakulkarni.videoplayer.data.VideoRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindsVideoRepository(videoRepository: VideoRepository): VideoDataSource
}