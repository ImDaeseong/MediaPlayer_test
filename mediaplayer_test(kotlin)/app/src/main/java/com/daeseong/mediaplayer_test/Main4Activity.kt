package com.daeseong.mediaplayer_test


import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.daeseong.mediaplayer_test.Mp3Player.OnMediaPlayerListener


class Main4Activity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private var btnPlay: ImageButton? = null
    private var btnPause:ImageButton? = null
    private var btnPrevious:ImageButton? = null
    private var btnNextgo:ImageButton? = null
    private var btnSearch:ImageButton? = null
    private var txtStartTime: TextView? = null
    private var txtEndTime:TextView? = null
    private var TimeBar: SeekBar? = null
    private var volumeBar:SeekBar? = null
    private var audioManager: AudioManager? = null
    private var playerTimer: PlayerTimer? = null
    private var mp3Player: Mp3Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InitTitleBar()

        setContentView(R.layout.activity_main4)

        InitControl()

        initVolume()

        initPlaytime()
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            mp3Player!!.release()
            stopplayerTimer()
        } catch (ex: Exception) {
            Log.d(tag, ex.message.toString())
        }
    }

    private fun InitTitleBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_bg)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun initVolume() {

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxvolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curvolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)

        volumeBar = findViewById<SeekBar>(R.id.volumeBar)
        volumeBar!!.max = maxvolume
        volumeBar!!.progress = curvolume
        volumeBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initPlaytime() {

        TimeBar = findViewById<SeekBar>(R.id.TimeBar)
        TimeBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!mp3Player!!.isPlaying()) {
                    mp3Player!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initDisplaytime() {

        val nEndTime = mp3Player!!.getDuration() / 1000
        val nEndMinutes = nEndTime / 60 % 60
        val nEndSeconds = nEndTime % 60
        val nCurrentTime = mp3Player!!.getCurrentPosition() / 1000
        val nCurrentMinutes = nCurrentTime / 60 % 60
        val nCurrentSeconds = nCurrentTime % 60

        txtStartTime!!.text = String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds)
        txtEndTime!!.text = String.format("%02d:%02d", nEndMinutes, nEndSeconds)
    }

    private fun InitControl() {

        mp3Player = Mp3Player.getInstance()
        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)
        btnSearch = findViewById(R.id.btnSearch)
        btnSearch!!.setOnClickListener {

            try {

                mp3Player!!.play(this, object : OnMediaPlayerListener {

                    override fun onCompletion(bComplete: Boolean) {
                        Log.d(tag, "onCompletion")
                        stopplayerTimer()
                    }

                    override fun onPrepared(mDuration: Int) {
                        Log.d(tag, "onPrepared")
                    }
                })

                setSeekBarProgress()

                btnPlay!!.visibility = View.VISIBLE
                btnPause!!.visibility = View.INVISIBLE

            } catch (ex: java.lang.Exception) {
                Log.d(tag, ex.message.toString())
            }
        }

        //연주
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay!!.setOnClickListener {

            mp3Player!!.start()
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        }

        //일시정지
        btnPause = findViewById(R.id.btnPause)
        btnPause!!.setOnClickListener {

            if (mp3Player!!.isPlaying()) {
                mp3Player!!.pause()
                btnPlay!!.visibility = View.VISIBLE
                btnPause!!.visibility = View.INVISIBLE
            }
        }

        //5초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious!!.setOnClickListener {
            mp3Player!!.seekTo(mp3Player!!.getCurrentPosition() - 5000)
        }

        //5초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo!!.setOnClickListener {
            mp3Player!!.seekTo(mp3Player!!.getCurrentPosition() + 5000)
        }
    }

    private fun stopplayerTimer() {
        if (playerTimer != null) {
            playerTimer!!.stop()
            playerTimer!!.removeMessages(0)
        }
    }

    private fun setSeekBarProgress() {

        stopplayerTimer()

        playerTimer = PlayerTimer()
        playerTimer!!.setCallback(object : PlayerTimer.Callback {

            override fun onTick(timeMillis: Long) {

                val position = mp3Player!!.getCurrentPosition()
                val duration = mp3Player!!.getDuration()

                if (duration <= 0) return

                TimeBar!!.max = duration
                TimeBar!!.progress = position
                val nEndTime = duration / 1000
                val nEndMinutes = nEndTime / 60 % 60
                val nEndSeconds = nEndTime % 60
                val nCurrentTime = position / 1000
                val nCurrentMinutes = nCurrentTime / 60 % 60
                val nCurrentSeconds = nCurrentTime % 60

                txtStartTime!!.text = String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds)
                txtEndTime!!.text = String.format("%02d:%02d", nEndMinutes, nEndSeconds)
            }
        })
        playerTimer!!.start()
    }

}
