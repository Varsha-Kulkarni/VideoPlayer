package dev.varshakulkarni.videoplayer.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.varshakulkarni.videoplayer.data.VideoDataSource
import kotlinx.coroutines.launch

class VideoViewModel(private val datasource: VideoDataSource) : ViewModel() {

    private val _videos = MutableLiveData<List<VideoItem>>()
    val videos: LiveData<List<VideoItem>>
        get() = _videos

    init {
        viewModelScope.launch {

            _videos.postValue(datasource.getVideos())
        }
    }
}