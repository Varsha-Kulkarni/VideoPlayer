package dev.varshakulkarni.videoplayer.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import dev.varshakulkarni.videoplayer.R
import dev.varshakulkarni.videoplayer.databinding.ActivityPlayerBinding
import kotlin.math.pow

class PlayerActivity : AppCompatActivity(), Player.Listener {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    private var youtubeLink: String? = null

    private var popupView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        setupMenu()

        Log.d("", "1.activity initialized")

        intent.getStringExtra("youtube_link")?.let {
            youtubeLink = it
        }

        if (intent?.action == Intent.ACTION_SEND) {
            Log.d("", "3.activity action send")

            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                Log.d("", "4.activity got intent extra $it")

                youtubeLink = it
            }
        }

    }

    public override fun onStart() {
        super.onStart()

        if (youtubeLink == null) {
            AlertDialog.Builder(this)
                .setTitle("Play Video")
                .setMessage("Play any youtube video through ${this.resources.getString(R.string.app_name)}, Go to youtube app, play any video,\n click on Share option to play the same video \n with custom options like Change Pitch, Tempo, loop through the clip")
                .setPositiveButton(
                    "Ok"
                ) { dialog, _ -> dialog.dismiss() }

        } else {
            initializePlayer()
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d("", "1.activity resumed")

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
                    player?.addListener(this@PlayerActivity)
                }
            }

        }.extract(youtubeLink)

    }

    private fun updatePlaybackParameters(playbackParameters: PlaybackParameters) {
        player?.playbackParameters = playbackParameters
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(this@PlayerActivity)
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

    private fun setupMenu() {
        val menuHost: MenuHost = this

        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onPrepareMenu(menu: Menu) {

                    super.onPrepareMenu(menu)

                }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.main_menu, menu)
                }

                @SuppressLint("SetTextI18n")
                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.action_settings -> {
                            player?.pause()
                            showSettingsPopup(this@PlayerActivity.viewBinding.parentLayout)
                            val precision = 10.0.pow(4.0)
                            val pitchValueText =
                                popupView?.findViewById<TextView>(R.id.pitchValueText)
                            val pitchSeekbar = popupView?.findViewById<SeekBar>(R.id.pitchSeekbar)
                            pitchSeekbar?.setOnSeekBarChangeListener(object :
                                SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    i: Int,
                                    b: Boolean
                                ) {
                                    if (i > 0) {
                                        val pitch: Float = i / 60f
                                        val currentPlaybackParameters = player?.playbackParameters
                                        val speed: Float = currentPlaybackParameters?.speed ?: 1f
                                        val value =
                                            (precision * ((i - 60) * 0.1f)).toInt() / precision
                                        pitchValueText?.text = value.toString()

                                        val newPlaybackParameters = PlaybackParameters(speed, pitch)
                                        updatePlaybackParameters(newPlaybackParameters)
                                    }
                                }

                                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                }

                                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                }
                            })
                            val pitchResetButton =
                                popupView?.findViewById<TextView>(R.id.pitchResetButton)
                            pitchResetButton?.setOnClickListener {
                                val pitch = 1f
                                pitchSeekbar?.progress = 60
                                pitchValueText?.text = 0f.toString()

                                val currentPlaybackParameters = player?.playbackParameters
                                val speed: Float = currentPlaybackParameters?.speed ?: 1.0f

                                val newPlaybackParameters = PlaybackParameters(speed, pitch)
                                updatePlaybackParameters(newPlaybackParameters)
                            }
                            val pitchMinusButton =
                                popupView?.findViewById<Button>(R.id.pitchMinusButton)
                            pitchMinusButton?.setOnClickListener {
                                val currentPlaybackParameters = player?.playbackParameters
                                val currentProgress = pitchSeekbar?.progress
                                if (currentProgress != null) {
                                    if (currentProgress > 1) {
                                        val newProgress = currentProgress.minus(1)
                                        pitchSeekbar.progress = newProgress
                                        val newPitch: Float = newProgress / 60f
                                        val value =
                                            (precision * (newProgress - 60) * 0.1f).toInt() / precision
                                        pitchValueText?.text = value.toString()

                                        val speed: Float = currentPlaybackParameters?.speed ?: 1f
                                        val newPlaybackParameters =
                                            PlaybackParameters(speed, newPitch)
                                        updatePlaybackParameters(newPlaybackParameters)
                                    }
                                }
                            }
                            val pitchPlusButton =
                                popupView?.findViewById<Button>(R.id.pitchPlusButton)
                            pitchPlusButton?.setOnClickListener {
                                val currentPlaybackParameters = player?.playbackParameters
                                val currentProgress = pitchSeekbar?.progress
                                if (currentProgress != null) {
                                    if (currentProgress < 100) {
                                        val newProgress = currentProgress.plus(1)
                                        pitchSeekbar.progress = newProgress
                                        val value =
                                            (precision * (newProgress - 60) * 0.1f).toInt() / precision
                                        pitchValueText?.text = value.toString()

                                        val newPitch: Float = newProgress / 60f
                                        val speed: Float = currentPlaybackParameters?.speed ?: 1.0f
                                        val newPlaybackParameters =
                                            PlaybackParameters(speed, newPitch)
                                        updatePlaybackParameters(newPlaybackParameters)
                                    }
                                }

                            }

                            val tempoSeekBar = popupView?.findViewById<SeekBar>(R.id.tempoSeekbar)
                            val tempoValueText =
                                popupView?.findViewById<TextView>(R.id.tempoValueText)
                            tempoValueText?.text =
                                String.format(resources.getString(R.string.tempo_value), 100)

                            tempoSeekBar?.setOnSeekBarChangeListener(object :
                                SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    i: Int,
                                    b: Boolean
                                ) {
                                    val tempo: Float = i.toFloat() / 100
                                    tempoValueText?.text =
                                        String.format(resources.getString(R.string.tempo_value), i)

                                    val newPlaybackParameters = PlaybackParameters(tempo)
                                    updatePlaybackParameters(newPlaybackParameters)
                                }

                                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                }

                                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                }
                            })

                            val tempoResetButton =
                                popupView?.findViewById<TextView>(R.id.tempoResetButton)
                            tempoResetButton?.setOnClickListener {
                                val tempo = 1f
                                tempoSeekBar?.progress = 100
                                tempoValueText?.text =
                                    String.format(resources.getString(R.string.tempo_value), 100)

                                val newPlaybackParameters = PlaybackParameters(tempo)
                                updatePlaybackParameters(newPlaybackParameters)
                            }

                            val tempoPlusButton =
                                popupView?.findViewById<Button>(R.id.tempoPlusButton)
                            tempoPlusButton?.setOnClickListener {
                                val currentProgress = tempoSeekBar?.progress
                                if (currentProgress != null) {
                                    if (currentProgress < 100) {
                                        val newProgress = currentProgress.plus(1)
                                        tempoSeekBar.progress = newProgress
                                        tempoValueText?.text = newProgress.toString()

                                        val newTempo: Float = newProgress / 100f
                                        val newPlaybackParameters = PlaybackParameters(newTempo)
                                        updatePlaybackParameters(newPlaybackParameters)
                                    }
                                }
                            }

                            val tempoMinusButton =
                                popupView?.findViewById<Button>(R.id.tempoMinusButton)
                            tempoMinusButton?.setOnClickListener {
                                val currentProgress = tempoSeekBar?.progress
                                if (currentProgress != null) {
                                    if (currentProgress > 1) {
                                        val newProgress = currentProgress.minus(1)
                                        tempoSeekBar.progress = newProgress
                                        tempoValueText?.text = newProgress.toString()

                                        val newTempo: Float = newProgress / 100f
                                        val newPlaybackParameters = PlaybackParameters(newTempo)
                                        updatePlaybackParameters(newPlaybackParameters)
                                    }
                                }
                            }

                        }

                    }
                    return false
                }
            },
            this, Lifecycle.State.RESUMED
        )
    }

    fun showSettingsPopup(view: View?) {

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.popup_settings, null)

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focus = true
        val popupWindow = PopupWindow(popupView, width, height, focus)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

    }
}