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
