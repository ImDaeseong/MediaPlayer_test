package com.daeseong.mediaplayer_test


import android.content.Context
import android.media.MediaPlayer
import android.util.Log


class Mp3Player {

    companion object {

        private val tag = Mp3Player::class.java.name

        private var instance: Mp3Player? = null

        private var mediaPlayer: MediaPlayer? = null

        fun getInstance(): Mp3Player {
            if (instance == null) {

                instance = Mp3Player()
                mediaPlayer = MediaPlayer()

                Log.e(tag, "getInstance")
            }
            return instance as Mp3Player
        }
    }

    private var onMediaPlayerListener: OnMediaPlayerListener? = null

    fun release() {

        try {

            removeListener()
            if (mediaPlayer != null) {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
        } catch (ex: Exception) {
            Log.d(tag, ex.message.toString())
        }
    }

    fun play(sPath: String?, onMediaPlayerListener: OnMediaPlayerListener?) {

        try {

            if (mediaPlayer != null) {

                this.onMediaPlayerListener = onMediaPlayerListener
                mediaPlayer!!.setDataSource(sPath)
                mediaPlayer!!.prepare()
                mediaPlayer!!.setOnCompletionListener {
                    onMediaPlayerListener?.onCompletion(true)
                }
                mediaPlayer!!.setOnPreparedListener { mp ->
                    onMediaPlayerListener?.onPrepared(mp.duration)
                    mp.start()
                }
                mediaPlayer!!.setOnErrorListener { mp, what, extra ->
                    onMediaPlayerListener?.onCompletion(false)
                    false
                }
            }
        } catch (ex: Exception) {
            onMediaPlayerListener?.onCompletion(false)
        }
    }

    fun play(context: Context?, onMediaPlayerListener: OnMediaPlayerListener?) {
        try {

            if (mediaPlayer != null) {

                this.onMediaPlayerListener = onMediaPlayerListener

                mediaPlayer = MediaPlayer.create(context, R.raw.a)
                mediaPlayer!!.setOnCompletionListener {
                    onMediaPlayerListener?.onCompletion(true)
                }
                mediaPlayer!!.setOnPreparedListener { mp ->
                    onMediaPlayerListener?.onPrepared(mp.duration)
                }
                mediaPlayer!!.setOnErrorListener { mp, what, extra ->
                    onMediaPlayerListener?.onCompletion(false)
                    false
                }
            }
        } catch (ex: java.lang.Exception) {
            onMediaPlayerListener?.onCompletion(false)
            Log.d(tag, ex.message.toString())
        }
    }

    fun start() {

        if (mediaPlayer != null) {
            mediaPlayer!!.start()
        }
    }

    fun stop() {

        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }
    }

    fun reset() {

        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
        }
    }

    fun pause() {

        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
        }
    }

    fun getCurrentPosition(): Int {
        return if (mediaPlayer != null) mediaPlayer!!.currentPosition else 0
    }

    fun getDuration(): Int {
        return if (mediaPlayer != null) mediaPlayer!!.duration else 0
    }

    fun isPlaying(): Boolean {
        return if (mediaPlayer != null) mediaPlayer!!.isPlaying else false
    }

    fun seekTo(position: Int) {

        if (mediaPlayer != null) {
            mediaPlayer!!.seekTo(position)
        }
    }

    private fun removeListener() {

        try {

            onMediaPlayerListener = null
        } catch (ex: Exception) {
            Log.e(tag, ex.message.toString())
        }
    }

    interface OnMediaPlayerListener {
        fun onCompletion(bComplete: Boolean)
        fun onPrepared(mDuration: Int)
    }
}