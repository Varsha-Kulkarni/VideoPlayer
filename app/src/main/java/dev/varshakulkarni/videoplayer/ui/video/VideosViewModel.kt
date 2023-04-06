/*
 * Copyright 2023 Varsha Kulkarni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    fun loadVideos() {
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
