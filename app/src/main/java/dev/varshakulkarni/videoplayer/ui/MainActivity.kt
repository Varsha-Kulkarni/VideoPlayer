package dev.varshakulkarni.videoplayer.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import dagger.hilt.android.AndroidEntryPoint
import dev.varshakulkarni.videoplayer.R
import dev.varshakulkarni.videoplayer.databinding.ActivityMainBinding
import dev.varshakulkarni.videoplayer.ui.player.PlayerActivity
import dev.varshakulkarni.videoplayer.ui.video.VideoItem
import dev.varshakulkarni.videoplayer.ui.video.VideoViewModel
import dev.varshakulkarni.videoplayer.ui.video.VideosListAdapter
import dev.varshakulkarni.videoplayer.utils.Utils

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Player.Listener {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val videoViewModel: VideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val videoAdapter = VideosListAdapter { video -> onVideoClick(video) }
        viewBinding.rvVideos.adapter = videoAdapter

        videoViewModel.videos.observe(this) {
            it?.let {
                videoAdapter.submitList(it as MutableList<VideoItem>)
            }
        }
        viewBinding.btnPlayVideo.setOnClickListener {
            val url = viewBinding.etVideoLink.text.toString()
            if (Utils.isYoutubeUrl(url)) {
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("youtube_link", url)
                startActivity(intent)
            } else {
                viewBinding.etVideoLink.error = resources.getString(R.string.error_yt_url)
            }
        }

        viewBinding.btnHelp.setOnClickListener {
            val balloon = Balloon.Builder(this)
                .setWidthRatio(1.0f)
                .setHeight(BalloonSizeSpec.WRAP)
                .setText(resources.getString(R.string.url_help))
                .setTextSize(15f)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowSize(10)
                .setArrowPosition(0.5f)
                .setPadding(12)
                .setCornerRadius(8f)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setLifecycleOwner(this)
                .build()
            balloon.showAtCenter(it, 0, 30)
        }
    }

    private fun onVideoClick(video: VideoItem) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("uri", video.uri.toString())
        startActivity(intent)
    }
}