
package com.example.itunesapi
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    fun play(url: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { it.start() }
            setOnCompletionListener {
                releasePlayer()
                Handler(Looper.getMainLooper()).postDelayed({
                    onCompletionListener?.invoke()
                }, 100 )
            }
        }
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
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}