package dev.varshakulkarni.videoplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dev.varshakulkarni.videoplayer.ui.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface VideoDataSource {
    suspend fun getVideos(): List<VideoItem>
}

class VideoRepository(
    val contentResolver: ContentResolver,
) : VideoDataSource {

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun getVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoItem>()


        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        // Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
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
}