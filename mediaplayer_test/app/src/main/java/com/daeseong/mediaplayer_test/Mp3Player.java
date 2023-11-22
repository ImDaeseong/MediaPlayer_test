package com.daeseong.mediaplayer_test;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class Mp3Player {

    private static final String TAG = Mp3Player.class.getName();

    private static Mp3Player instance;
    private MediaPlayer mediaPlayer;
    private OnMediaPlayerListener onMediaPlayerListener;

    private Mp3Player() {
        mediaPlayer = new MediaPlayer();
    }

    public static Mp3Player getInstance() {
        if (instance == null) {
            instance = new Mp3Player();
        }
        return instance;
    }

    public void release() {
        try {
            removeListener();

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void play(String sPath, final OnMediaPlayerListener onMediaPlayerListener) {
        try {
            this.onMediaPlayerListener = onMediaPlayerListener;

            mediaPlayer.reset();
            mediaPlayer.setDataSource(sPath);
            mediaPlayer.prepare();

            setMediaPlayerListeners();
        } catch (Exception ex) {
            handleMediaPlayerError(ex);
        }
    }

    public void play(Context context, final OnMediaPlayerListener onMediaPlayerListener) {
        try {
            this.onMediaPlayerListener = onMediaPlayerListener;

            mediaPlayer = MediaPlayer.create(context, R.raw.a);

            setMediaPlayerListeners();
        } catch (Exception ex) {
            handleMediaPlayerError(ex);
        }
    }

    private void setMediaPlayerListeners() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (onMediaPlayerListener != null) {
                    onMediaPlayerListener.onCompletion(true);
                }
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (onMediaPlayerListener != null) {
                    onMediaPlayerListener.onPrepared(mp.getDuration());
                }
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                handleMediaPlayerError(new Exception("MediaPlayer error"));
                return false;
            }
        });
    }

    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) return mediaPlayer.getCurrentPosition();
        else return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) return mediaPlayer.getDuration();
        else return 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    private void removeListener() {
        onMediaPlayerListener = null;
    }

    private void handleMediaPlayerError(Exception ex) {
        if (onMediaPlayerListener != null) {
            onMediaPlayerListener.onCompletion(false);
        }
        Log.e(TAG, ex.getMessage());
    }

    public interface OnMediaPlayerListener {
        void onCompletion(boolean bComplete);
        void onPrepared(int mDuration);
    }
}
