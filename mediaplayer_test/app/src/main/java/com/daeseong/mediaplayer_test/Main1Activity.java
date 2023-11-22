package com.daeseong.mediaplayer_test;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import androidx.appcompat.app.AppCompatActivity;

public class Main1Activity extends AppCompatActivity {

    private static final String TAG = Main1Activity.class.getSimpleName();

    private ImageButton btnPlay, btnPause, btnPrevious, btnNextgo, btnSearch;
    private TextView txtStartTime, txtEndTime;
    private SeekBar timeBar, volumeBar;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private PlayerTimer playerTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTitleBar();

        setContentView(R.layout.activity_main1);

        initControl();

        initVolume();

        initPlaytime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        stopPlayerTimer();
    }

    private void initTitleBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.rgb(255, 255, 255));
        }

        try {
            //안드로이드 8.0 오레오 버전에서만 오류 발생
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage().toString());
        }
    }

    private void initVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeBar = findViewById(R.id.volumeBar);
        volumeBar.setMax(maxVolume);
        volumeBar.setProgress(currentVolume);

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

    private void initPlaytime() {
        timeBar = findViewById(R.id.TimeBar);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying() && fromUser) {
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

    private void initControl() {
        txtStartTime = findViewById(R.id.startTime);
        txtEndTime = findViewById(R.id.endTime);

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMediaPlayer();
            }
        });

        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMediaPlayer();
            }
        });

        btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseMediaPlayer();
            }
        });

        btnPrevious = findViewById(R.id.btnPrevious);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekMediaPlayer(-5000);
            }
        });

        btnNextgo = findViewById(R.id.btnNextgo);
        btnNextgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekMediaPlayer(5000);
            }
        });
    }

    private void initMediaPlayer() {
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(Main1Activity.this, R.raw.a);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlayerTimer();
            }
        });
        setSeekBarProgress();
        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.INVISIBLE);
    }

    private void playMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            btnPlay.setVisibility(View.INVISIBLE);
            btnPause.setVisibility(View.VISIBLE);
        }
    }

    private void pauseMediaPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlay.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.INVISIBLE);
        }
    }

    private void seekMediaPlayer(int milliseconds) {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition() + milliseconds;
            mediaPlayer.seekTo(Math.max(0, currentPosition));
        }
    }

    private void setSeekBarProgress() {
        stopPlayerTimer();
        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {
                updateSeekBar();
            }
        });
        playerTimer.start();
    }

    private void updateSeekBar() {
        int position = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();

        if (duration <= 0) return;

        timeBar.setMax(duration);
        timeBar.setProgress(position);

        int nEndTime = duration / 1000;
        int nEndMinutes = (nEndTime / 60) % 60;
        int nEndSeconds = nEndTime % 60;

        int nCurrentTime = position / 1000;
        int nCurrentMinutes = (nCurrentTime / 60) % 60;
        int nCurrentSeconds = nCurrentTime % 60;

        txtStartTime.setText(String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds));
        txtEndTime.setText(String.format("%02d:%02d", nEndMinutes, nEndSeconds));
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void stopPlayerTimer() {
        if (playerTimer != null) {
            playerTimer.stop();
            playerTimer = null;
        }
    }
}
