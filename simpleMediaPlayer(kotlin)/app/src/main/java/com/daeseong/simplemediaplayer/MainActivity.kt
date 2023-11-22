package com.daeseong.simplemediaplayer

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.daeseong.simplemediaplayer.Mp3Player.OnMediaPlayerListener
import java.util.*

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private lateinit var btnPre: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNextgo: ImageButton
    private lateinit var btnSearch: ImageButton
    private lateinit var txtStartTime: TextView
    private lateinit var txtEndTime: TextView
    private lateinit var txtDesc: TextView
    private lateinit var seekBar: SeekBar

    private var musicList = ArrayList<MusicInfo>()
    private var currentPlayIndex = -1
    private var timerMarquee: Timer? = null

    private lateinit var permissionsLauncher: ActivityResultLauncher<String>

    private lateinit var playerTimer: PlayerTimer
    private lateinit var mp3Player: Mp3Player
    private lateinit var taskMarquee: MarqueeTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTitleBar()

        setContentView(R.layout.activity_main)

        initPermissionsLauncher()

        initalizePlayer()

        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)
        txtDesc = findViewById(R.id.tvDesc)

        btnSearch = findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener {

            checkPermissions()

            //음악 폴더 선택
            musicList.clear()
            val item = getMusicList()
            musicList = item.getData()

            if (musicList.isEmpty()) return@setOnClickListener

            currentPlayIndex = 0

            mp3Player.reset()

            //Marquee
            txtDesc.text = musicList[currentPlayIndex].musicName

            val uri = Uri.parse(musicList[currentPlayIndex].musicPath)
            playPlayer(uri.toString())
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        //3초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious.setOnClickListener {
            PreplayPlayer()
        }

        //3초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo.setOnClickListener {
            NextplayPlayer()
        }

        //이전곡
        btnPre = findViewById(R.id.btnPre)
        btnPre.setOnClickListener {
            if (musicList.isEmpty()) return@setOnClickListener

            currentPlayIndex--

            if (currentPlayIndex < 0)
                currentPlayIndex = musicList.size - 1

            mp3Player.stop()

            //Marquee
            txtDesc.text = musicList[currentPlayIndex].musicName

            val uri = Uri.parse(musicList[currentPlayIndex].musicPath)
            playPlayer(uri.toString())
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        //다음곡
        btnNext = findViewById(R.id.btnNext)
        btnNext.setOnClickListener {
            if (musicList.isEmpty()) return@setOnClickListener

            currentPlayIndex++

            if (currentPlayIndex > (musicList.size - 1))
                currentPlayIndex = 0

            mp3Player.stop()

            //Marquee
            txtDesc.text = musicList[currentPlayIndex].musicName

            val uri = Uri.parse(musicList[currentPlayIndex].musicPath)
            playPlayer(uri.toString())
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        //연주
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener {
            mp3Player.start()
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        //일시정지
        btnPause = findViewById(R.id.btnPause)
        btnPause.setOnClickListener {
            if (mp3Player.isPlaying()) {
                mp3Player.pause()
                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
            } else {
                mp3Player.start()
                btnPlay.visibility = View.INVISIBLE
                btnPause.visibility = View.VISIBLE
            }
        }

        //진행바
        seekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!mp3Player.isPlaying()) {
                    mp3Player.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        checkPermissions()
    }

    private fun initTitleBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.rgb(255, 255, 255)
        }

        try {
            //안드로이드 8.0 오레오 버전에서만 오류 발생
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        } catch (ex: Exception) {
            Log.e(tag, ex.message.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        musicList.clear()
        closeMarqueeTimer()
        stopplayerTimer()
        releasePlayer()
    }

    private fun checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    permissionsLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    Log.e(tag, "READ_MEDIA_AUDIO 권한 소유")
                }

            } else {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    Log.e(tag, "READ_EXTERNAL_STORAGE 권한 소유")
                }
            }
        }
    }

    private fun initPermissionsLauncher() {
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

            if (result) {
                Log.e(tag, "권한 소유")
            } else {
                Log.e(tag, "권한 미소유")
            }
        }
    }

    private fun initalizePlayer() {
        mp3Player = Mp3Player.getInstance()
    }

    private fun releasePlayer() {
        mp3Player.release()
    }

    private fun playPlayer(sUrl: String) {
        mp3Player.play(sUrl, object : OnMediaPlayerListener {
            override fun onCompletion(bComplete: Boolean) {
                Log.d(tag, "onCompletion")
                stopplayerTimer()
            }

            override fun onPrepared(mDuration: Int) {
                Log.d(tag, "onPrepared")
            }
        })
        setSeekBarProgress()
    }

    private fun PreplayPlayer() {
        mp3Player.seekTo(mp3Player.getCurrentPosition() - 5000)
    }

    private fun NextplayPlayer() {
        mp3Player.seekTo(mp3Player.getCurrentPosition() + 5000)
    }

    private fun getCurrentPosition(): Int {
        return mp3Player.getCurrentPosition()
    }

    private fun getDuration(): Int {
        return mp3Player.getDuration()
    }

    private fun stringForTime(timeMs: Int): String {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs / 1000

        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private fun stopplayerTimer() {

        if (::playerTimer.isInitialized) {
            playerTimer.stop()
        }
    }

    private fun setSeekBarProgress() {

        //Marquee
        startMarqueeTimer()

        stopplayerTimer()

        playerTimer = PlayerTimer()
        playerTimer.setCallback(object : PlayerTimer.Callback {
            override fun onTick(timeMillis: Long) {

                if (mp3Player.isPlaying()) {

                    val position = getCurrentPosition()
                    val duration = getDuration().toLong()

                    if (duration <= 0) return

                    seekBar.max = (duration / 1000).toInt()
                    seekBar.progress = (position / 1000).toInt()

                    txtStartTime.text = stringForTime(getCurrentPosition())
                    txtEndTime.text = stringForTime(getDuration())
                }
            }
        })
        playerTimer.start()
    }

    private fun closeMarqueeTimer() {
        timerMarquee?.cancel()
        timerMarquee = null
    }

    private fun startMarqueeTimer() {
        closeMarqueeTimer()
        taskMarquee = MarqueeTask(txtDesc)
        timerMarquee = Timer()
        timerMarquee?.schedule(taskMarquee, 0, 10000)
    }
}
