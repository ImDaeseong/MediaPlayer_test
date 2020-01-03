package com.daeseong.mediaplayer_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class Main3Activity extends AppCompatActivity {

    private static final String TAG = Main3Activity.class.getSimpleName();

    private ImageButton btnPlay, btnPause, btnPrevious, btnNextgo, btnSearch;
    private TextView txtStartTime, txtEndTime;
    private SeekBar TimeBar, volumeBar;
    private MediaPlayer mediaPlayer = null;
    private AudioManager audioManager = null;
    private PlayerTimer playerTimer;

    protected MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            Log.d(TAG, "onCompletion");
            stopplayerTimer();
        }
    };

    protected MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            Log.d(TAG, "onError");
            return false;
        }
    };

    protected MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {

            Log.d(TAG, "onPrepared");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitTitleBar();

        setContentView(R.layout.activity_main3);

        InitControl();

        initVolume();

        initPlaytime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            stopplayerTimer();

        }catch (Exception ex){
            Log.d(TAG, ex.getMessage().toString());
        }
    }

    private void InitTitleBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar_bg));
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void  initVolume(){

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxvolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curvolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeBar = (SeekBar)findViewById(R.id.volumeBar);
        volumeBar.setMax(maxvolume);
        volumeBar.setProgress(curvolume);

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void  initPlaytime(){

        TimeBar = (SeekBar)findViewById(R.id.TimeBar);
        TimeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initDisplaytime(){

        int nEndTime = mediaPlayer.getDuration() / 1000;
        int nEndMinutes = (nEndTime / 60) % 60;
        int nEndSeconds = nEndTime % 60;

        int nCurrentTime = mediaPlayer.getCurrentPosition() / 1000;
        int nCurrentMinutes = (nCurrentTime / 60) % 60;
        int nCurrentSeconds = nCurrentTime % 60;

        txtStartTime.setText(String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds));
        txtEndTime.setText(String.format("%02d:%02d", nEndMinutes, nEndSeconds));
    }

    private void InitControl(){

        txtStartTime = findViewById(R.id.startTime);
        txtEndTime = findViewById(R.id.endTime);

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    mediaPlayer = MediaPlayer.create(Main3Activity.this, R.raw.a);
                    mediaPlayer.setOnPreparedListener(onPreparedListener);
                    mediaPlayer.setOnCompletionListener(onCompletionListener);
                    mediaPlayer.setOnErrorListener(onErrorListener);

                    setSeekBarProgress();

                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);

                }catch (Exception ex){
                    Log.d(TAG, ex.getMessage().toString());
                }
            }
        });

        //연주
        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    btnPlay.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.VISIBLE);
                }
            }
        });

        //일시정지
        btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();

                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                }
            }
        });

        //5초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                }
            }
        });

        //5초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo);
        btnNextgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                }
            }
        });
    }

    private void stopplayerTimer(){
        if(playerTimer != null){
            playerTimer.stop();
            playerTimer.removeMessages(0);
        }
    }

    private void setSeekBarProgress(){

        stopplayerTimer();

        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {

                int position = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();

                if (duration <= 0) return;

                TimeBar.setMax(duration);
                TimeBar.setProgress(position);

                int nEndTime = duration / 1000;
                int nEndMinutes = (nEndTime / 60) % 60;
                int nEndSeconds = nEndTime % 60;

                int nCurrentTime = position / 1000;
                int nCurrentMinutes = (nCurrentTime / 60) % 60;
                int nCurrentSeconds = nCurrentTime % 60;

                txtStartTime.setText(String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds));
                txtEndTime.setText(String.format("%02d:%02d", nEndMinutes, nEndSeconds));
            }
        });
        playerTimer.start();
    }
}

