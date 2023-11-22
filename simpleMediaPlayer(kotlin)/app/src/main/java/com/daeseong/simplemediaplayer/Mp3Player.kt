package com.daeseong.simplemediaplayer

import android.media.MediaPlayer
import android.util.Log

class Mp3Player private constructor() {

    private val tag = Mp3Player::class.java.name

    private var mediaPlayer: MediaPlayer? = null
    private var onMediaPlayerListener: OnMediaPlayerListener? = null

    init {
        mediaPlayer = MediaPlayer()
    }

    companion object {
        private var instance: Mp3Player? = null

        fun getInstance(): Mp3Player {
            if (instance == null) {
                instance = Mp3Player()
            }
            return instance as Mp3Player
        }
    }

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
            Log.e(tag, ex.message.toString())
        }
    }

    fun play(sPath: String, onMediaPlayerListener: OnMediaPlayerListener?) {
        try {
            mediaPlayer?.let {
                this.onMediaPlayerListener = onMediaPlayerListener

                it.setDataSource(sPath)
                it.prepare()

                it.setOnCompletionListener {
                    onMediaPlayerListener?.onCompletion(true)
                }

                it.setOnPreparedListener { mp ->
                    onMediaPlayerListener?.onPrepared(mp.duration)
                    mp.start()
                }

                it.setOnErrorListener { mp, what, extra ->
                    onMediaPlayerListener?.onCompletion(false)
                    false
                }
            }
        } catch (ex: Exception) {
            onMediaPlayerListener?.onCompletion(false)
        }
    }

    fun start() {
        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.let {
            it.stop()
            it.reset()
        }
    }

    fun reset() {
        mediaPlayer?.reset()
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    private fun removeListener() {
        onMediaPlayerListener = null
    }

    interface OnMediaPlayerListener {
        fun onCompletion(bComplete: Boolean)
        fun onPrepared(mDuration: Int)
    }
}
