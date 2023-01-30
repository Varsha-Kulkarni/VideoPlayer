package dev.varshakulkarni.videoplayer

import android.content.Context

fun Int?.formatDurationString(context: Context): String {

    var seconds = (this?.toLong() ?: 0) / 1000

    val hours: Long = seconds / 3600
    seconds %= 3600
    val minutes: Long = seconds / 60
    seconds %= 60

    val formatString = if (hours == 0L) {
        R.string.durationformatshort
    } else {
        R.string.durationformatlong
    }
    val durationFormat = context.resources.getString(formatString)
    return String.format(durationFormat, hours, minutes, seconds)
}