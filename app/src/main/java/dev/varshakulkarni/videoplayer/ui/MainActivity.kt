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
package dev.varshakulkarni.videoplayer.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
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
import dev.varshakulkarni.videoplayer.data.db.entity.VideoEntity
import dev.varshakulkarni.videoplayer.databinding.ActivityMainBinding
import dev.varshakulkarni.videoplayer.ui.player.PlayerActivity
import dev.varshakulkarni.videoplayer.ui.video.SearchAdapter
import dev.varshakulkarni.videoplayer.ui.video.VideoItem
import dev.varshakulkarni.videoplayer.ui.video.VideosListAdapter
import dev.varshakulkarni.videoplayer.ui.video.VideosViewModel
import dev.varshakulkarni.videoplayer.utils.Utils

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Player.Listener {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val videosViewModel: VideosViewModel by viewModels()
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var videoAdapter: VideosListAdapter

    private val ytVideos = ArrayList<VideoEntity>()
    private var ytVideo: VideoEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        searchAdapter = SearchAdapter(
            this@MainActivity,
            R.layout.item_video_title, ytVideos
        )

        viewBinding.lvHistory.adapter = searchAdapter
        viewBinding.lvHistory.setOnItemClickListener { adapterView: AdapterView<*>, view2: View, i: Int, l: Long ->
            ytVideo = adapterView.getItemAtPosition(i) as VideoEntity
            viewBinding.etVideoLink.text =
                Editable.Factory.getInstance().newEditable(ytVideo?.title)
        }

        viewBinding.etVideoLink.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                videosViewModel.searchVideo(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit
        })

        videosViewModel.ytVideos.observe(this) {
            ytVideos.clear()
            ytVideos.addAll(it)
            searchAdapter.notifyDataSetChanged()
            Log.i("VarshaSearch", "")
        }

        viewBinding.btnPlayVideo.setOnClickListener {
            val url = viewBinding.etVideoLink.text.toString()
            var data: String? = null

            if (Utils.isYoutubeUrl(url))
                data = url
            else if (ytVideo != null)
                data = ytVideo?.url

            if (data == null) {
                viewBinding.etVideoLink.error = resources.getString(R.string.error_yt_url)
            } else {
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("youtube_link", data)
                startActivity(intent)
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
            if (!permissionsGranted()) {
                showPermissionDialog()
            } else {
                videosViewModel.loadVideos()
            }
        }

        videoAdapter = VideosListAdapter { video -> onVideoClick(video) }
        viewBinding.rvVideos.adapter = videoAdapter

        // Request local media permissions
        if (permissionsGranted()) {
            videosViewModel.loadVideos()
        } else {
            viewBinding.apply {
                tvNoData.visibility = View.VISIBLE
                btnRefresh.visibility = View.VISIBLE
            }
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS.toTypedArray(), REQUEST_CODE_PERMISSIONS
            )
        }

        videosViewModel.videos.observe(this) { data ->
            viewBinding.run {
                tvNoData.visibility = View.GONE
                btnRefresh.visibility = View.GONE
                videoAdapter.submitList(data)
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
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (permissionsGranted()) {
                videosViewModel.loadVideos()
            } else {
                viewBinding.apply {
                    tvNoData.visibility = View.VISIBLE
                    btnRefresh.visibility = View.VISIBLE
                }
                showPermissionDialog()
            }
        }
    }

    private fun showPermissionDialog() {
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

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
