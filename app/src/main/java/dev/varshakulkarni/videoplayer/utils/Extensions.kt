package dev.varshakulkarni.videoplayer.utils

import android.content.Context
import android.view.MotionEvent
import android.widget.EditText
import dev.varshakulkarni.videoplayer.R

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

fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}