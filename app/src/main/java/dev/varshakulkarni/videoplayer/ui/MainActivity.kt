package dev.varshakulkarni.videoplayer.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        // Request local media permissions
        if (permissionsGranted()) {
            initLocalVideos()
        } else {
            viewBinding.tvNoData.visibility = View.VISIBLE
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS.toTypedArray(), REQUEST_CODE_PERMISSIONS
            )
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

        viewBinding.btnRefresh.setOnClickListener {
            initLocalVideos()
        }
    }

    private fun initLocalVideos() {
        val videoAdapter = VideosListAdapter { video -> onVideoClick(video) }
        viewBinding.rvVideos.adapter = videoAdapter

        videoViewModel.videos.observe(this) {
            it?.let {
                viewBinding.apply {
                    tvNoData.visibility = View.GONE
                    btnRefresh.visibility = View.GONE
                }
                videoAdapter.submitList(it as MutableList<VideoItem>)
            }
        }
    }

    private fun onVideoClick(video: VideoItem) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("uri", video.uri.toString())
        startActivity(intent)
    }

    private fun permissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (permissionsGranted()) {
                initLocalVideos()
            } else {
                viewBinding.tvNoData.visibility = View.VISIBLE
                AlertDialog.Builder(this)
                    .setTitle("Local Playback permissions")
                    .setMessage("${resources.getString(R.string.app_name)}  requires this permission for local videos playback, \n 1. Scroll to Permissions \n 2. Allow \" Files and Media permission\"")
                    .setPositiveButton(
                        "Ok"
                    ) { dialog, which ->
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", packageName, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}