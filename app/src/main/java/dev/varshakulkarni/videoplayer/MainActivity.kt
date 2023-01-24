package dev.varshakulkarni.videoplayer

import android.os.Bundle
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import dev.varshakulkarni.videoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), Player.Listener {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    private lateinit var youtubeLink: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }

    public override fun onStart() {
        super.onStart()
        val videoId = ""  //pass any video Id or replace with youtube video link
        youtubeLink = "https://www.youtube.com/watch?v=$videoId"

        initializePlayer()

    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        viewBinding.playerView.player = player

        object : YouTubeExtractor(this) {
            override fun onExtractionComplete(
                ytFiles: SparseArray<YtFile>?,
                videoMeta: VideoMeta?
            ) {
                if (ytFiles != null) {

                    val iTag = 137//tag of video 1080
                    val audioTag = 140 //tag m4a audio
                    // 720, 1080, 480
                    var videoUrl = ""
                    val iTags: List<Int> = listOf(22, 137, 18)
                    for (i in iTags) {
                        val ytFile = ytFiles.get(i)
                        if (ytFile != null) {
                            val downloadUrl = ytFile.url
                            if (downloadUrl != null && downloadUrl.isNotEmpty()) {
                                videoUrl = downloadUrl
                            }
                        }
                    }
                    if (videoUrl == "")
                        videoUrl = ytFiles[iTag].url
                    val audioUrl = ytFiles[audioTag].url
                    val audioSource: MediaSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(audioUrl))
                    val videoSource: MediaSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(videoUrl))
                    player?.setMediaSource(
                        MergingMediaSource(true, videoSource, audioSource), true
                    )
                    player?.prepare()
                    player?.playWhenReady = playWhenReady
                    player?.seekTo(currentItem, playbackPosition)
                    player?.addListener(this@MainActivity)
                }
            }

        }.extract(youtubeLink)

    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(this@MainActivity)
            exoPlayer.release()
        }
        player = null
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            viewBinding.progressBar.visibility = View.INVISIBLE
        } else {
            viewBinding.progressBar.visibility = View.VISIBLE
        }
    }
}