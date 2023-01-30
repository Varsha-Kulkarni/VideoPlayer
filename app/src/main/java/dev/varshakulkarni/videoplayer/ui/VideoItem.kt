package dev.varshakulkarni.videoplayer.ui

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoItem(
    val id: Long,
    val uri: Uri?,
    val name: String?,
    val duration: Int,
    val size: Int
) : Parcelable