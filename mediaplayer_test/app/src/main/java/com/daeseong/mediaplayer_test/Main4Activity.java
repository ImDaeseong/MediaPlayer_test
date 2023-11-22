package com.daeseong.mediaplayer_test;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class Main4Activity extends AppCompatActivity {

    private static final String TAG = Main4Activity.class.getSimpleName();

    private ImageButton btnPlay, btnPause, btnPrevious, btnNextgo, btnSearch;
    private TextView txtStartTime, txtEndTime;
    private SeekBar TimeBar, volumeBar;
    private AudioManager audioManager;
    private PlayerTimer playerTimer;
    private Mp3Player mp3Player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTitleBar();

        setContentView(R.layout.activity_main4);

        InitControl();

        initVolume();

        initPlaytime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mp3Player.release();
            stopPlayerTimer();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage().toString());
        }
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
        int maxvolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curvolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeBar = findViewById(R.id.volumeBar);
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

    private void initPlaytime() {
        TimeBar = findViewById(R.id.TimeBar);
        TimeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!mp3Player.isPlaying()) {
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
    }

    private void initDisplaytime() {
        int nEndTime = mp3Player.getDuration() / 1000;
        int nEndMinutes = (nEndTime / 60) % 60;
        int nEndSeconds = nEndTime % 60;

        int nCurrentTime = mp3Player.getCurrentPosition() / 1000;
        int nCurrentMinutes = (nCurrentTime / 60) % 60;
        int nCurrentSeconds = nCurrentTime % 60;

        txtStartTime.setText(String.format("%02d:%02d", nCurrentMinutes, nCurrentSeconds));
        txtEndTime.setText(String.format("%02d:%02d", nEndMinutes, nEndSeconds));
    }

    private void InitControl() {
        mp3Player = Mp3Player.getInstance();

        txtStartTime = findViewById(R.id.startTime);
        txtEndTime = findViewById(R.id.endTime);

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mp3Player.release();
                    mp3Player.play(Main4Activity.this, new Mp3Player.OnMediaPlayerListener() {
                        @Override
                        public void onCompletion(boolean bComplete) {
                            Log.e(TAG, "onCompletion");
                            stopPlayerTimer();
                        }

                        @Override
                        public void onPrepared(int mDuration) {
                            Log.e(TAG, "onPrepared");
                            setSeekBarProgress();
                        }
                    });

                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage().toString());
                }
            }
        });

        // 연주
        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp3Player.start();
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        // 일시정지
        btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mp3Player.isPlaying()) {
                    mp3Player.pause();
                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                }
            }
        });

        // 5초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp3Player.seekTo(mp3Player.getCurrentPosition() - 5000);
            }
        });

        // 5초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo);
        btnNextgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp3Player.seekTo(mp3Player.getCurrentPosition() + 5000);
            }
        });
    }

    private void releaseMediaPlayer() {
        try {
            if (mp3Player != null) {
                mp3Player.stop();
                mp3Player.release();
                mp3Player = null;
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void stopPlayerTimer() {
        if (playerTimer != null) {
            playerTimer.stop();
            playerTimer = null;
        }
    }

    private void setSeekBarProgress() {
        stopPlayerTimer();

        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {
                int position = mp3Player.getCurrentPosition();
                int duration = mp3Player.getDuration();

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
