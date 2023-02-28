package dev.varshakulkarni.videoplayer.ui.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.varshakulkarni.videoplayer.data.VideoDataSource
import dev.varshakulkarni.videoplayer.data.db.entity.VideoEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideosViewModel @Inject constructor(private val datasource: VideoDataSource) : ViewModel() {
    private val _videos = MutableLiveData<List<VideoItem>>()
    val videos: LiveData<List<VideoItem>>
        get() = _videos

    private val _ytVideos = MutableLiveData<List<VideoEntity>>()
    val ytVideos: LiveData<List<VideoEntity>>
        get() = _ytVideos

    private var job: Job? = null

    init {
        viewModelScope.launch {
            _videos.postValue(datasource.getVideos())
        }
    }

    fun searchVideo(searchQuery: String) {
        job?.cancel()
        viewModelScope.launch {
            _ytVideos.postValue(datasource.searchVideo(searchQuery))
        }
    }
}