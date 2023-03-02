package dev.varshakulkarni.videoplayer.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dev.varshakulkarni.videoplayer.data.db.dao.VideosDao
import dev.varshakulkarni.videoplayer.data.db.entity.VideoEntity
import dev.varshakulkarni.videoplayer.ui.video.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface VideoDataSource {
    suspend fun getVideos(): List<VideoItem>
    suspend fun searchVideo(searchQuery: String): List<VideoEntity>
    suspend fun saveVideoMeta(videoEntity: VideoEntity)
    suspend fun getVideoByUrl(url: String): VideoEntity?
    suspend fun deleteLeastRecentVideos()
}

class VideoRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val videosDao: VideosDao
) : VideoDataSource {

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun getVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoItem>()


        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        // Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, sortOrder)

        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val title: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                val duration: Int =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                val size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))

                val video = VideoItem(
                    id,
                    ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ), title, duration, size
                )
                videos.add(video)
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return@withContext videos
    }

    override suspend fun searchVideo(searchQuery: String): List<VideoEntity> =
        videosDao.searchUrl(searchQuery)

    override suspend fun saveVideoMeta(videoEntity: VideoEntity) =
        videosDao.saveMediaMeta(videoEntity)

    override suspend fun getVideoByUrl(url: String): VideoEntity? = videosDao.getVideoByUrl(url)
    override suspend fun deleteLeastRecentVideos() = videosDao.deleteLeastRecentlyUsed()

}