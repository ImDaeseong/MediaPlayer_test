package com.daeseong.mediaplayer_test

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Main2Activity : AppCompatActivity() {

    private val tag = Main2Activity::class.java.simpleName

    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNextgo: ImageButton
    private lateinit var btnSearch: ImageButton
    private lateinit var txtStartTime: TextView
    private lateinit var txtEndTime: TextView
    private lateinit var TimeBar: SeekBar
    private lateinit var volumeBar: SeekBar

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var playerTimer: PlayerTimer? = null

    private val onPreparedListener = MediaPlayer.OnPreparedListener {
        Log.e(tag, "onPrepared")
    }

    private val onCompletionListener = MediaPlayer.OnCompletionListener {
        Log.e(tag, "onCompletion")
        stopPlayerTimer()
    }

    private val onErrorListener = MediaPlayer.OnErrorListener { mp, what, extra ->
        Log.e(tag, "onError")
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTitleBar()

        setContentView(R.layout.activity_main2)

        InitControl()

        initVolume()

        initPlaytime()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        stopPlayerTimer()
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.let {
                it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (ex: Exception) {
            Log.e(tag, ex.message ?: "")
        }
    }

    private fun stopPlayerTimer() {
        playerTimer?.stop()
        playerTimer = null
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
        TimeBar = findViewById(R.id.TimeBar)
        TimeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun InitControl() {
        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)

        btnSearch = findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener {
            try {
                releaseMediaPlayer()
                mediaPlayer = MediaPlayer.create(this@Main2Activity, R.raw.a)
                mediaPlayer?.setOnPreparedListener(onPreparedListener)
                mediaPlayer?.setOnCompletionListener(onCompletionListener)
                mediaPlayer?.setOnErrorListener(onErrorListener)

                setSeekBarProgress()

                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
            } catch (ex: Exception) {
                Log.e(tag, ex.message ?: "")
            }
        }

        btnPlay = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener {
            mediaPlayer?.start()
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        btnPause = findViewById(R.id.btnPause)
        btnPause.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
            }
        }

        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious.setOnClickListener {
            mediaPlayer?.let {
                it.seekTo(it.currentPosition - 5000)
            }
        }

        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo.setOnClickListener {
            mediaPlayer?.let {
                it.seekTo(it.currentPosition + 5000)
            }
        }
    }

    private fun setSeekBarProgress() {
        stopPlayerTimer()

        playerTimer = PlayerTimer()
        playerTimer?.setCallback(object : PlayerTimer.Callback {
            override fun onTick(timeMillis: Long) {
                val position = mediaPlayer?.currentPosition ?: 0
                val duration = mediaPlayer?.duration ?: 0

                if (duration <= 0) return

                TimeBar.max = duration
                TimeBar.progress = position

                val nEndTime = duration / 1000
                val nEndMinutes = (nEndTime / 60) % 60
                val nEndSeconds = nEndTime % 60

                val nCurrentTime = position / 1000
                val nCurrentMinutes = (nCurrentTime / 60) % 60
                val nCurrentSeconds = nCurrentTime % 60

                txtStartTime.text = String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds)
                txtEndTime.text = String.format("%02d:%02d", nEndMinutes, nEndSeconds)
            }
        })
        playerTimer?.start()
    }
}
