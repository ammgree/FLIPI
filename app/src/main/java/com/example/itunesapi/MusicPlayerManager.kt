
package com.example.itunesapi
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var onStartListener: (()->Unit)? = null
    private var onCompletionListener: (() -> Unit)? = null
    lateinit var song: Album

    private val playPauseListeners = mutableListOf<(Boolean)->Unit>()

    fun addOnPlayPauseChangeListener(listener: ((Boolean) -> Unit)?) {
        playPauseListeners.add(listener!!)
    }
    fun removeOnPlayPauseChangeListener(listener: (Boolean) -> Unit) {
        playPauseListeners.remove(listener)
    }
    private fun notifyPlayPauseChanged(isPlaying: Boolean) {
        playPauseListeners.forEach { it(isPlaying) }
    }

    fun play(album: Album) {
        stop()
        song = album
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(album.songUrl)
            prepareAsync()
            setOnPreparedListener {
                it.start()
                onStartListener?.invoke()
            }
            setOnCompletionListener {
                releasePlayer()
                Handler(Looper.getMainLooper()).postDelayed({
                    onCompletionListener?.invoke()
                }, 100 )
            }
        }
        notifyPlayPauseChanged(true)
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        notifyPlayPauseChanged(false)
    }

    fun pause() {
        mediaPlayer?.pause()
        notifyPlayPauseChanged(false)
    }

    fun resume() {
        mediaPlayer?.start()
        notifyPlayPauseChanged(true)
    }

    fun setOnStartListener(listener: () -> Unit) {
        onStartListener = listener
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}