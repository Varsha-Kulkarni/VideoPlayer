package dev.varshakulkarni.videoplayer.ui.player

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.util.SparseArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
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
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.google.android.material.slider.RangeSlider
import dagger.hilt.android.AndroidEntryPoint
import dev.varshakulkarni.videoplayer.R
import dev.varshakulkarni.videoplayer.data.db.entity.VideoEntity
import dev.varshakulkarni.videoplayer.databinding.ActivityPlayerBinding
import dev.varshakulkarni.videoplayer.ui.video.VideoViewModel
import dev.varshakulkarni.videoplayer.utils.Utils
import java.util.*
import kotlin.math.pow

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity(), Player.Listener {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    private val videoViewModel: VideoViewModel by viewModels()

    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private var localPlayback = false

    private var videoUri: String? = null

    private var videoSource: MediaSource? = null
    private var audioSource: MediaSource? = null

    private var popupView: View? = null
    private var popviewProgress: ProgressBar? = null
    private var pitchPosition = 60
    private var tempoPosition = 100

    private var videoDuration: Long? = null
    private var isDurationSet = false

    var sliderValues: List<Float>? = null

    var isInPipMode: Boolean = false
    var isPIPModeEnabled: Boolean = true

    private var videoTitle: String? = null
    private var videoEntity: VideoEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setupMenu()

        intent.getStringExtra("youtube_link")?.let {
            videoUri = it
            videoViewModel.getVideo(it)
        }

        if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                videoUri = it
                videoViewModel.getVideo(it)
            }
        }

        videoViewModel.ytVideo.observe(this) {
            videoEntity = it
        }

        intent.getStringExtra("uri")?.let {
            Log.d("Video Player URI", "uri $it")
            videoUri = it
            localPlayback = true
        }
    }

    public override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
        viewBinding.playerView.useController = true
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
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            finishAndRemoveTask()
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        viewBinding.playerView.player = player

        if (localPlayback) {
            Log.d("Video Player URI", "uri $videoUri")
            videoUri?.let {
                player?.addMediaItem(MediaItem.fromUri(it))
                prepareVideoPlayback()
            }
        } else {
            prepareYoutubePlayback()
        }

        val mediaSession = MediaSessionCompat(this, packageName)
        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(player)
        mediaSession.isActive = true
    }

    private fun prepareYoutubePlayback() {
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
                    audioSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(audioUrl))

                    videoSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(videoUrl))

                    videoTitle = videoMeta?.title
                    saveVideoMeta()
                    player?.setMediaSource(
                        MergingMediaSource(
                            true, audioSource as ProgressiveMediaSource,
                            videoSource as ProgressiveMediaSource
                        ), true
                    )
                    prepareVideoPlayback()
                }
            }

        }.extract(videoUri)
    }

    private fun prepareVideoPlayback() {
        player?.playWhenReady = playWhenReady
        player?.prepare()
        player?.seekTo(currentItem, playbackPosition)
        player?.addListener(this@PlayerActivity)
    }

    private fun saveVideoMeta() {
        videoUri?.let {
            if (videoEntity == null)
                videoEntity = VideoEntity(it, videoTitle, videoDuration)

            videoEntity?.let { video ->
                var count = video.searchCount
                count++
                video.searchCount = count
                videoViewModel.saveVideoMeta(video)
                videoViewModel.deleteLeastRecentVideos()
            }
        }
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
            if (!isDurationSet) {
                videoDuration = player?.duration
                isDurationSet = true
            }
            Log.d("videoDuration", "$videoDuration")
            viewBinding.progressBar.visibility = View.INVISIBLE
            popviewProgress?.visibility = View.INVISIBLE
        } else if (playbackState == Player.STATE_BUFFERING) {
            viewBinding.progressBar.visibility = View.VISIBLE
            popviewProgress?.visibility = View.VISIBLE
        }
    }

    private fun loopClip() {
        val millis = 1000
        val startMS: Long = sliderValues?.get(0)?.times(millis)?.toLong() ?: 0L
        val endMS = sliderValues?.get(1)?.times(millis)?.toLong() ?: videoDuration ?: 0L

        Log.d("loopclip", "Start = $startMS end=$endMS")
        if (localPlayback) {
            videoUri?.let {
                val localVideoSource = ClippingMediaSource(
                    ProgressiveMediaSource
                        .Factory(DefaultDataSource.Factory(this))
                        .createMediaSource(MediaItem.fromUri(it)), startMS, endMS
                )
                player?.setMediaSource(localVideoSource)
            }
        } else {
            val clippingAudioSource = audioSource?.let { ClippingMediaSource(it, startMS, endMS) }
            val clippingVideoSource = videoSource?.let { ClippingMediaSource(it, startMS, endMS) }

            if (clippingVideoSource != null && clippingAudioSource != null) {

                player?.setMediaSource(
                    MergingMediaSource(true, clippingVideoSource, clippingAudioSource), true
                )
            }
        }
        player?.prepare()
        player?.playWhenReady
    }

    private fun setupMenu() {
        val menuHost: MenuHost = this

        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.main_menu, menu)
                }

                @SuppressLint("SetTextI18n")
                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.action_settings -> {
                            player?.pause()
                            showSettingsPopup(this@PlayerActivity.viewBinding.parentLayout)
                            val precision = 10.0.pow(1.0)
                            val pitchValueText =
                                popupView?.findViewById<TextView>(R.id.pitchValueText)
                            val pitchSeekbar = popupView?.findViewById<SeekBar>(R.id.pitchSeekbar)
                            pitchValueText?.text =
                                ((precision * ((pitchPosition - 60) * 0.1f)).toInt() / precision).toString()
                            pitchSeekbar?.progress = pitchPosition
                            pitchSeekbar?.setOnSeekBarChangeListener(object :
                                SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    i: Int,
                                    b: Boolean
                                ) {
                                    if (i > 0) {
                                        pitchPosition = i
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
                                popupView?.findViewById<Button>(R.id.pitchResetButton)
                            pitchResetButton?.setOnClickListener {
                                pitchPosition = 60

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
                                        pitchPosition = newProgress
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
                                        pitchPosition = newProgress

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
                                String.format(
                                    resources.getString(R.string.tempo_value),
                                    tempoPosition
                                )
                            tempoSeekBar?.progress = tempoPosition
                            tempoSeekBar?.setOnSeekBarChangeListener(object :
                                SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    i: Int,
                                    b: Boolean
                                ) {
                                    tempoPosition = i
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
                                popupView?.findViewById<Button>(R.id.tempoResetButton)
                            tempoResetButton?.setOnClickListener {
                                tempoPosition = 100
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
                                    if (currentProgress < 200) {
                                        val newProgress = currentProgress.plus(1)
                                        tempoPosition = newProgress
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
                                        tempoPosition = newProgress
                                        tempoSeekBar.progress = newProgress
                                        tempoValueText?.text = newProgress.toString()

                                        val newTempo: Float = newProgress / 100f
                                        val newPlaybackParameters = PlaybackParameters(newTempo)
                                        updatePlaybackParameters(newPlaybackParameters)
                                    }
                                }
                            }
                            val timelineSlider = popupView?.findViewById<RangeSlider>(R.id.timeline)
                            timelineSlider?.setLabelFormatter { value ->
                                Utils.formatToDigitalClock(value.toLong())
                            }
                            popviewProgress = popupView?.findViewById(R.id.popviewProgressBar)

                            timelineSlider?.valueFrom = 0f
                            timelineSlider?.valueTo =
                                videoDuration?.toFloat() ?: 1f
                            timelineSlider?.values = sliderValues ?: arrayListOf(
                                0f,
                                videoDuration?.toFloat() ?: 1f
                            )
                            timelineSlider?.minSeparation = 1f
                            timelineSlider?.addOnSliderTouchListener(object :
                                RangeSlider.OnSliderTouchListener {

                                override fun onStartTrackingTouch(slider: RangeSlider) {

                                }

                                override fun onStopTrackingTouch(slider: RangeSlider) {
                                    sliderValues = slider.values
                                    Log.d("", "slider values, $sliderValues ")
                                    loopClip()
                                }
                            })


                            val isRepeatOn = popupView?.findViewById<CheckBox>(R.id.cbRepeat)
                            isRepeatOn?.isChecked = player?.repeatMode != Player.REPEAT_MODE_OFF
                            isRepeatOn?.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
                                if (isChecked)
                                    player?.repeatMode = Player.REPEAT_MODE_ONE
                                else
                                    player?.repeatMode = Player.REPEAT_MODE_OFF
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

        popupWindow.showAtLocation(view, Gravity.CENTER, 30, 30)

    }

    fun showYoutubePlaybackError() {
        AlertDialog.Builder(this)
            .setTitle("Playback Error")
            .setMessage("Please check the Url entered is the valid one!")
            .setPositiveButton(
                "Ok"
            ) { dialog, _ -> dialog.dismiss() }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        playbackPosition = player?.currentPosition ?: 0L
        isInPipMode = !isInPictureInPictureMode
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    //Called when the user touches the Home or Recents button to leave the app.
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPIPMode()
    }

    private fun enterPIPMode() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            popupView?.visibility = View.GONE
            playbackPosition = player?.currentPosition ?: 0L
            viewBinding.playerView.useController = false
            val params = PictureInPictureParams.Builder()
            this.enterPictureInPictureMode(params.build())
            Handler(Looper.getMainLooper()).postDelayed({ checkPIPPermission() }, 30)
        }
    }

    private fun checkPIPPermission() {
        isPIPModeEnabled = isInPictureInPictureMode
    }
}