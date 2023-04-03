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
package dev.varshakulkarni.videoplayer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.varshakulkarni.videoplayer.data.db.entity.VideoEntity

@Dao
interface VideosDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertVideo(varargs: Array<VideoEntity>?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMediaMeta(videoEntity: VideoEntity)

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :searchQuery || '%' LIMIT 3")
    suspend fun searchUrl(searchQuery: String): List<VideoEntity>

    @Query("SELECT * FROM videos WHERE url=:url")
    suspend fun getVideoByUrl(url: String): VideoEntity?

    @Query("DELETE FROM videos WHERE searchCount=1")
    suspend fun deleteLeastRecentlyUsed()
}
