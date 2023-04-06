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
