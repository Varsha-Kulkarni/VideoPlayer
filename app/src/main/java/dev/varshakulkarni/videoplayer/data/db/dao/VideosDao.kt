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
