package com.daeseong.simplemediaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageButton btnPre, btnPlay, btnPause, btnNext, btnPrevious, btnNextgo, btnSearch;
    private TextView txtStartTime, txtEndTime, txtDesc;
    private SeekBar seekBar;
    private PlayerTimer playerTimer;
    private Mp3Player mp3Player;

    private ArrayList<MusicInfo> musicList  = new ArrayList<MusicInfo>();
    private int CurrentPlayIndex = -1;

    private MarqueeTask taskMarquee;
    private Timer timerMarquee = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitTitleBar();

        setContentView(R.layout.activity_main);

        initalizePlayer();

        txtStartTime = findViewById(R.id.startTime);
        txtEndTime = findViewById(R.id.endTime);
        txtDesc = findViewById(R.id.tvDesc);

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissions();

                //음악 폴더 선택
                musicList.clear();
                getMusicList item = new getMusicList();
                musicList = item.getData();

                if (musicList.size() == 0) return;

                CurrentPlayIndex = 0;

                mp3Player.reset();

                //Marquee
                txtDesc.setText(musicList.get(CurrentPlayIndex).getMusicName());

                Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                playPlayer(uri.toString());
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //3초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreplayPlayer();
            }
        });

        //3초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo);
        btnNextgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NextplayPlayer();
            }
        });

        //이전곡
        btnPre = findViewById(R.id.btnPre);
        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(musicList.size() == 0) return;

                CurrentPlayIndex--;

                if (CurrentPlayIndex < 0)
                    CurrentPlayIndex = musicList.size() - 1;

                mp3Player.stop();

                //Marquee
                txtDesc.setText(musicList.get(CurrentPlayIndex).getMusicName());

                Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                playPlayer(uri.toString());
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //다음곡
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(musicList.size() == 0) return;

                CurrentPlayIndex++;

                if (CurrentPlayIndex > (musicList.size() - 1))
                    CurrentPlayIndex = 0;

                mp3Player.stop();

                //Marquee
                txtDesc.setText(musicList.get(CurrentPlayIndex).getMusicName());

                Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                playPlayer(uri.toString());
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //연주
        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mp3Player.start();
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //일시정지
        btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mp3Player.isPlaying()) {
                    mp3Player.pause();
                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                }else {
                    mp3Player.start();
                    btnPlay.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.VISIBLE);
                }
            }
        });

        //진행바
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(!mp3Player.isPlaying()) {
                    mp3Player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        checkPermissions();
    }

    private void InitTitleBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar_bg));
        }
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,   new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        musicList.clear();
        closeMarqueeTimer();
        stopplayerTimer();
        releasePlayer();
    }

    private void initalizePlayer(){
        mp3Player = mp3Player.getInstance();
    }

    private void releasePlayer(){
        mp3Player.release();
    }

    private void playPlayer(String sUrl){

        mp3Player.play(sUrl, new Mp3Player.OnMediaPlayerListener() {
            @Override
            public void onCompletion(boolean bComplete) {

                Log.d(TAG, "onCompletion");
                stopplayerTimer();
            }

            @Override
            public void onPrepared(int mDuration) {

                Log.d(TAG, "onPrepared");
            }
        });

        setSeekBarProgress();
    }

    private void PreplayPlayer(){
        mp3Player.seekTo(mp3Player.getCurrentPosition() - 5000);
    }

    private void NextplayPlayer(){
        mp3Player.seekTo(mp3Player.getCurrentPosition() + 5000);
    }

    private int getCurrentPosition(){
        return mp3Player.getCurrentPosition();
    }

    private int getDuration(){
        return mp3Player.getDuration();
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds =  timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void stopplayerTimer(){
        if(playerTimer != null){
            playerTimer.stop();
            playerTimer.removeMessages(0);
        }
    }

    private void setSeekBarProgress(){

        //Marquee
        startMarqueeTimer();

        stopplayerTimer();

        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {

                if(mp3Player.isPlaying()) {

                    long position = getCurrentPosition();
                    long duration = getDuration();

                    if (duration <= 0) return;

                    seekBar.setMax((int) duration / 1000);
                    seekBar.setProgress((int) position / 1000);

                    txtStartTime.setText(stringForTime((int) getCurrentPosition()));
                    txtEndTime.setText(stringForTime((int) getDuration()));
                }
            }
        });
        playerTimer.start();
    }

    private void closeMarqueeTimer(){
        if (timerMarquee != null) {
            timerMarquee.cancel();
            timerMarquee = null;
        }
    }

    private void startMarqueeTimer(){
        closeMarqueeTimer();
        taskMarquee = new MarqueeTask(txtDesc);
        timerMarquee = new Timer();
        timerMarquee.schedule(taskMarquee, 0, 10000);
    }

}
