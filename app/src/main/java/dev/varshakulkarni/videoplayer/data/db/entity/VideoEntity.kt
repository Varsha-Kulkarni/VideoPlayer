package dev.varshakulkarni.videoplayer.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "videos", indices = [Index(value = ["title"], unique = true)])
data class VideoEntity(
    val url: String,
    val title: String?,
    val duration: Long?,
    var searchCount: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}


