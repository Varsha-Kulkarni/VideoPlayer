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
class VideoViewModel @Inject constructor(private val datasource: VideoDataSource) : ViewModel() {

    private val _ytVideo = MutableLiveData<VideoEntity>()
    val ytVideo: LiveData<VideoEntity>
        get() = _ytVideo

    private var job: Job? = null

    fun saveVideoMeta(videoEntity: VideoEntity) {
        job?.cancel()
        job = viewModelScope.launch {
            datasource.saveVideoMeta(videoEntity)
        }
    }

    fun getVideo(url: String) {
        job?.cancel()
        job = viewModelScope.launch {
            _ytVideo.postValue(datasource.getVideoByUrl(url))
        }
    }
}