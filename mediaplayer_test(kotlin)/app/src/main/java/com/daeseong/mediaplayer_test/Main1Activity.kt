package com.daeseong.mediaplayer_test

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Main1Activity : AppCompatActivity() {

    private val tag = Main1Activity::class.java.simpleName

    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNextgo: ImageButton
    private lateinit var btnSearch: ImageButton
    private lateinit var txtStartTime: TextView
    private lateinit var txtEndTime: TextView
    private lateinit var timeBar: SeekBar
    private lateinit var volumeBar: SeekBar

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var playerTimer: PlayerTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTitleBar()

        setContentView(R.layout.activity_main1)

        initControl()

        initVolume()

        initPlaytime()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        stopPlayerTimer()
    }

    private fun initTitleBar() {
        try {
            //안드로이드 8.0 오레오 버전에서만 오류 발생
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        } catch (ex: Exception) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    private fun initVolume() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        volumeBar = findViewById(R.id.volumeBar)
        volumeBar.max = maxVolume
        volumeBar.progress = currentVolume

        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initPlaytime() {
        timeBar = findViewById(R.id.TimeBar)
        timeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && !mediaPlayer!!.isPlaying && fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initControl() {
        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)

        btnSearch = findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener { initMediaPlayer() }

        btnPlay = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener { playMediaPlayer() }

        btnPause = findViewById(R.id.btnPause)
        btnPause.setOnClickListener { pauseMediaPlayer() }

        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious.setOnClickListener { seekMediaPlayer(-5000) }

        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo.setOnClickListener { seekMediaPlayer(5000) }
    }

    private fun initMediaPlayer() {
        releaseMediaPlayer()
        mediaPlayer = MediaPlayer.create(this, R.raw.a)
        mediaPlayer?.setOnCompletionListener {
            stopPlayerTimer()
        }
        setSeekBarProgress()
        btnPlay.visibility = View.VISIBLE
        btnPause.visibility = View.INVISIBLE
    }

    private fun playMediaPlayer() {
        mediaPlayer?.start()
        btnPlay.visibility = View.INVISIBLE
        btnPause.visibility = View.VISIBLE
    }

    private fun pauseMediaPlayer() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            btnPlay.visibility = View.VISIBLE
            btnPause.visibility = View.INVISIBLE
        }
    }

    private fun seekMediaPlayer(milliseconds: Int) {
        mediaPlayer?.let {
            val currentPosition = it.currentPosition + milliseconds
            it.seekTo(Math.max(0, currentPosition))
        }
    }

    private fun setSeekBarProgress() {
        stopPlayerTimer()
        playerTimer = PlayerTimer()
        playerTimer?.setCallback(object : PlayerTimer.Callback {
            override fun onTick(timeMillis: Long) {
                updateSeekBar()
            }
        })
        playerTimer?.start()
    }

    private fun updateSeekBar() {
        val position = mediaPlayer?.currentPosition ?: 0
        val duration = mediaPlayer?.duration ?: 0

        if (duration <= 0) return

        timeBar.max = duration
        timeBar.progress = position

        val nEndTime = duration / 1000
        val nEndMinutes = (nEndTime / 60) % 60
        val nEndSeconds = nEndTime % 60

        val nCurrentTime = position / 1000
        val nCurrentMinutes = (nCurrentTime / 60) % 60
        val nCurrentSeconds = nCurrentTime % 60

        txtStartTime.text = String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds)
        txtEndTime.text = String.format("%02d:%02d", nEndMinutes, nEndSeconds)
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun stopPlayerTimer() {
        playerTimer?.stop()
        playerTimer = null
    }
}
