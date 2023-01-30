package dev.varshakulkarni.videoplayer.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import dev.varshakulkarni.videoplayer.databinding.ActivityMainBinding

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
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("youtube_link", viewBinding.etVideoLink.text.toString())
            startActivity(intent)
        }
    }

    private fun onVideoClick(video: VideoItem) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("uri", video.uri)
        startActivity(intent)
    }
}