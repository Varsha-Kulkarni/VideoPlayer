package dev.varshakulkarni.videoplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import dev.varshakulkarni.videoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), Player.Listener {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.btnPlayVideo.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("youtube_link", viewBinding.etVideoLink.text.toString())
            startActivity(intent)
        }
    }
}