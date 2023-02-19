package dev.varshakulkarni.videoplayer.utils

object Utils {
    fun isYoutubeUrl(youTubeURl: String): Boolean {
        val success: Boolean
        val pattern = Regex("^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+")
        success = youTubeURl.isNotEmpty() && youTubeURl.matches(pattern)
        return success
    }
}