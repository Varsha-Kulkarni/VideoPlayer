package dev.varshakulkarni.videoplayer.utils

import java.util.concurrent.TimeUnit

object Utils {
    fun isYoutubeUrl(youTubeURl: String): Boolean {
        val success: Boolean
        val pattern = Regex("^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+")
        success = youTubeURl.isNotEmpty() && youTubeURl.matches(pattern)
        return success
    }

    fun formatToDigitalClock(milliSeconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliSeconds).toInt() % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliSeconds).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliSeconds).toInt() % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            seconds > 0 -> String.format("00:%02d", seconds)
            else -> {
                "00:00"
            }
        }
    }
}