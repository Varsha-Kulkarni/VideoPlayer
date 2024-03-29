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

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.varshakulkarni.videoplayer.data.VideoDataSource
import dev.varshakulkarni.videoplayer.data.db.entity.VideoEntity
import dev.varshakulkarni.videoplayer.utils.DataStoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val datasource: VideoDataSource,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

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

    fun deleteLeastRecentVideos() {
        job = viewModelScope.launch {

            val dateString = dataStoreRepository.getString(CLEAR_DATA_DATE)
            if (dateString != null) {
                Log.d("VideoViewModel", dateString)
            }
            try {
                if (dateString != null) {
                    val date = LocalDate.parse(dateString)
                    Log.d("VideoViewModel", "$date")

                    val today = LocalDate.now()
                    val days = Period.between(today, date).days
                    Log.d("VideoViewModel", "$days")

                    if (days > 7) {
                        datasource.deleteLeastRecentVideos()
                        dataStoreRepository.putString(CLEAR_DATA_DATE, LocalDate.now().toString())
                    }
                } else {
                    dataStoreRepository.putString(CLEAR_DATA_DATE, LocalDate.now().toString())
                }
            } catch (e: DateTimeParseException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val CLEAR_DATA_DATE = "CLEAR_DATA_DATE"
    }
}
