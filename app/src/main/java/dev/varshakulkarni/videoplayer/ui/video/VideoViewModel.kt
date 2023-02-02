package dev.varshakulkarni.videoplayer.ui.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.varshakulkarni.videoplayer.data.VideoDataSource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(private val datasource: VideoDataSource) : ViewModel() {

    private val _videos = MutableLiveData<List<VideoItem>>()
    val videos: LiveData<List<VideoItem>>
        get() = _videos

    init {
        viewModelScope.launch {

            _videos.postValue(datasource.getVideos())
        }
    }
}