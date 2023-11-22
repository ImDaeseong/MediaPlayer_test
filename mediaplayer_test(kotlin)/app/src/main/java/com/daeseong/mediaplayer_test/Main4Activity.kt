package com.daeseong.mediaplayer_test

import android.content.Context
import android.media.AudioManager
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

class Main4Activity : AppCompatActivity() {

    private val tag = Main4Activity::class.java.simpleName

    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNextgo: ImageButton
    private lateinit var btnSearch: ImageButton
    private lateinit var txtStartTime: TextView
    private lateinit var txtEndTime: TextView
    private lateinit var TimeBar: SeekBar
    private lateinit var volumeBar: SeekBar
    private lateinit var audioManager: AudioManager

    private var mp3Player: Mp3Player? = null
    private var playerTimer: PlayerTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTitleBar()

        setContentView(R.layout.activity_main4)

        InitControl()

        initVolume()

        initPlaytime()
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            mp3Player?.release()
            stopPlayerTimer()
        } catch (ex: Exception) {
            Log.d(tag, ex.message.toString())
        }
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
                if (!mp3Player?.isPlaying()!!) {
                    mp3Player?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initDisplaytime() {
        val nEndTime = mp3Player?.getDuration()?.div(1000)
        val nEndMinutes = (nEndTime?.div(60))?.rem(60)
        val nEndSeconds = nEndTime?.rem(60)

        val nCurrentTime = mp3Player?.getCurrentPosition()?.div(1000)
        val nCurrentMinutes = (nCurrentTime?.div(60))?.rem(60)
        val nCurrentSeconds = nCurrentTime?.rem(60)

        txtStartTime.text = String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds)
        txtEndTime.text = String.format("%02d:%02d", nEndMinutes, nEndSeconds)
    }

    private fun InitControl() {
        mp3Player = Mp3Player.getInstance()

        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)

        btnSearch = findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener {
            try {
                mp3Player?.release()
                mp3Player?.play(this, object : Mp3Player.OnMediaPlayerListener {
                    override fun onCompletion(bComplete: Boolean) {
                        Log.e(tag, "onCompletion")
                        stopPlayerTimer()
                    }

                    override fun onPrepared(mDuration: Int) {
                        Log.e(tag, "onPrepared")
                        setSeekBarProgress()
                    }
                })

                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
            } catch (ex: Exception) {
                Log.d(tag, ex.message.toString())
            }
        }

        // Play
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener {
            mp3Player?.start()
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        // Pause
        btnPause = findViewById(R.id.btnPause)
        btnPause.setOnClickListener {
            if (mp3Player?.isPlaying() == true) {
                mp3Player?.pause()
                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
            }
        }

        // 5 seconds backward
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious.setOnClickListener {
            mp3Player?.seekTo(mp3Player?.getCurrentPosition()!! - 5000)
        }

        // 5 seconds forward
        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo.setOnClickListener {
            mp3Player?.seekTo(mp3Player?.getCurrentPosition()!! + 5000)
        }
    }

    private fun releaseMediaPlayer() {
        try {
            if (mp3Player?.isPlaying() == true) {
                mp3Player?.stop()
            }
            mp3Player?.release()
            mp3Player = Mp3Player.getInstance()
        } catch (ex: Exception) {
            Log.e(tag, ex.message ?: "")
        }
    }

    private fun stopPlayerTimer() {
        playerTimer?.stop()
        playerTimer = null
    }

    private fun setSeekBarProgress() {
        stopPlayerTimer()

        playerTimer = PlayerTimer()
        playerTimer?.setCallback(object : PlayerTimer.Callback {
            override fun onTick(timeMillis: Long) {
                val position = mp3Player?.getCurrentPosition()!!
                val duration = mp3Player?.getDuration()!!

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
