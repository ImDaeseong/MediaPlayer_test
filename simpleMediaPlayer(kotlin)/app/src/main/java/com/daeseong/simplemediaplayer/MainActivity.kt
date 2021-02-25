package com.daeseong.simplemediaplayer

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daeseong.simplemediaplayer.Mp3Player.OnMediaPlayerListener
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private var btnPre: ImageButton? = null
    private var btnPlay:ImageButton? = null
    private var btnPause:ImageButton? = null
    private var btnNext:ImageButton? = null
    private var btnPrevious:ImageButton? = null
    private var btnNextgo:ImageButton? = null
    private var btnSearch:ImageButton? = null
    private var txtStartTime: TextView? = null
    private var txtEndTime:TextView? = null
    private var txtDesc:TextView? = null
    private var seekBar: SeekBar? = null
    private var playerTimer: PlayerTimer? = null
    private var mp3Player: Mp3Player? = null

    private var musicList = ArrayList<MusicInfo>()
    private var CurrentPlayIndex = -1

    private var taskMarquee: MarqueeTask? = null
    private var timerMarquee: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InitTitleBar()

        setContentView(R.layout.activity_main)

        initalizePlayer()

        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)
        txtDesc = findViewById(R.id.tvDesc)

        btnSearch = findViewById(R.id.btnSearch)
        btnSearch!!.setOnClickListener(View.OnClickListener {

            checkPermissions()

            //음악 폴더 선택
            musicList.clear()
            val item = getMusicList()
            if (item.getData()) {
                musicList = item.musicList
            }

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex = 0
            mp3Player!!.reset()

            //Marquee
            txtDesc!!.text = musicList[CurrentPlayIndex].musicName

            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri.toString())
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //3초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious!!.setOnClickListener {
            PreplayPlayer()
        }

        //3초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo!!.setOnClickListener {
            NextplayPlayer()
        }

        //이전곡
        btnPre = findViewById(R.id.btnPre)
        btnPre!!.setOnClickListener(View.OnClickListener {

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex--

            if (CurrentPlayIndex < 0) CurrentPlayIndex = musicList.size - 1
            mp3Player!!.stop()

            //Marquee
            txtDesc!!.text = musicList[CurrentPlayIndex].musicName

            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri.toString())
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //다음곡
        btnNext = findViewById(R.id.btnNext)
        btnNext!!.setOnClickListener(View.OnClickListener {

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex++

            if (CurrentPlayIndex > musicList.size - 1) CurrentPlayIndex = 0

            mp3Player!!.stop()

            //Marquee
            txtDesc!!.text = musicList[CurrentPlayIndex].musicName

            val uri: Uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri.toString())
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //연주
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay!!.setOnClickListener {

            mp3Player!!.start()
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        }

        //일시정지
        btnPause = findViewById(R.id.btnPause)
        btnPause!!.setOnClickListener(View.OnClickListener {

            if (mp3Player!!.isPlaying()) {
                mp3Player!!.pause()
                btnPlay!!.visibility = View.VISIBLE
                btnPause!!.visibility = View.INVISIBLE
            } else {
                mp3Player!!.start()
                btnPlay!!.visibility = View.INVISIBLE
                btnPause!!.visibility = View.VISIBLE
            }
        })

        //진행바
        seekBar = findViewById<SeekBar>(R.id.seekBar)
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!mp3Player!!.isPlaying()) {
                    mp3Player!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        checkPermissions()
    }

    private fun InitTitleBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_bg)
        }
    }

    private fun checkPermissions() {

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        musicList.clear()
        closeMarqueeTimer()
        stopplayerTimer()
        releasePlayer()
    }

    private fun initalizePlayer() {
        mp3Player = Mp3Player.getInstance()
    }

    private fun releasePlayer() {
        mp3Player!!.release()
    }

    private fun playPlayer(sUrl: String) {

        mp3Player!!.play(sUrl, object : OnMediaPlayerListener {

            override fun onCompletion(bComplete: Boolean) {
                Log.e(tag, "onCompletion")
                stopplayerTimer()
            }

            override fun onPrepared(mDuration: Int) {
                Log.e(tag, "onPrepared")
            }
        })
        setSeekBarProgress()
    }

    private fun PreplayPlayer() {
        mp3Player!!.seekTo(mp3Player!!.getCurrentPosition() - 5000)
    }

    private fun NextplayPlayer() {
        mp3Player!!.seekTo(mp3Player!!.getCurrentPosition() + 5000)
    }

    private fun getCurrentPosition(): Int {
        return mp3Player!!.getCurrentPosition()
    }

    private fun getDuration(): Int {
        return mp3Player!!.getDuration()
    }

    private fun stringForTime(timeMs: Int): String? {
        val mFormatter: Formatter
        val mFormatBuilder: StringBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())

        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)

        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private fun stopplayerTimer() {

        if (playerTimer != null) {
            playerTimer!!.stop()
            playerTimer!!.removeMessages(0)
        }
    }

    private fun setSeekBarProgress() {

        //Marquee
        startMarqueeTimer()
        stopplayerTimer()
        playerTimer = PlayerTimer()
        playerTimer!!.setCallback(object : PlayerTimer.Callback {

            override fun onTick(timeMillis: Long) {

                if (mp3Player!!.isPlaying()) {

                    val position: Int = getCurrentPosition()
                    val duration: Int = getDuration()

                    if (duration <= 0) return

                    seekBar!!.max = duration / 1000
                    seekBar!!.progress = position / 1000

                    txtStartTime!!.text = stringForTime(getCurrentPosition())
                    txtEndTime!!.text = stringForTime(getDuration())
                }
            }
        })
        playerTimer!!.start()
    }

    private fun closeMarqueeTimer() {

        if (timerMarquee != null) {
            timerMarquee!!.cancel()
            timerMarquee = null
        }
    }

    private fun startMarqueeTimer() {

        closeMarqueeTimer()
        taskMarquee = MarqueeTask(txtDesc!!)
        timerMarquee = Timer()
        timerMarquee!!.schedule(taskMarquee, 0, 10000)
    }
}
