package com.daeseong.mediaplayer_test

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class Mp3Player private constructor() {

    private val tag = Mp3Player::class.java.name

    private var mediaPlayer: MediaPlayer? = null
    private var onMediaPlayerListener: OnMediaPlayerListener? = null

    fun release() {
        try {
            removeListener()

            mediaPlayer?.let {
                it.stop()
                it.reset()
                it.release()
            }
            mediaPlayer = null
        } catch (ex: Exception) {
            Log.e(tag, ex.message ?: "")
        }
    }

    fun play(sPath: String, onMediaPlayerListener: OnMediaPlayerListener) {
        try {
            this.onMediaPlayerListener = onMediaPlayerListener

            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(sPath)
            mediaPlayer?.prepare()

            setMediaPlayerListeners()
        } catch (ex: Exception) {
            handleMediaPlayerError(ex)
        }
    }

    fun play(context: Context, onMediaPlayerListener: OnMediaPlayerListener) {
        try {
            this.onMediaPlayerListener = onMediaPlayerListener

            mediaPlayer = MediaPlayer.create(context, R.raw.a)

            setMediaPlayerListeners()
        } catch (ex: Exception) {
            handleMediaPlayerError(ex)
        }
    }

    private fun setMediaPlayerListeners() {
        mediaPlayer?.setOnCompletionListener {
            onMediaPlayerListener?.onCompletion(true)
        }

        mediaPlayer?.setOnPreparedListener {
            onMediaPlayerListener?.onPrepared(it.duration)
        }

        mediaPlayer?.setOnErrorListener { _, _, _ ->
            handleMediaPlayerError(Exception("MediaPlayer error"))
            false
        }
    }

    fun start() {
        mediaPlayer?.start()
    }

    fun stop() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    private fun removeListener() {
        onMediaPlayerListener = null
    }

    private fun handleMediaPlayerError(ex: Exception) {
        onMediaPlayerListener?.onCompletion(false)
        Log.e(tag, ex.message ?: "")
    }

    interface OnMediaPlayerListener {
        fun onCompletion(bComplete: Boolean)
        fun onPrepared(mDuration: Int)
    }

    companion object {
        private var instance: Mp3Player? = null

        @JvmStatic
        fun getInstance(): Mp3Player {
            if (instance == null) {
                instance = Mp3Player()
            }
            return instance!!
        }
    }
}