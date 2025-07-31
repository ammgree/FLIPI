package com.example.itunesapi

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

// 음악 재생을 전역적으로 관리하는 객체
object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null // 미디어 플레이어 인스턴스
    private var onStartListener: (() -> Unit)? = null // 재생 시작 시 콜백
    private var onCompletionListener: (() -> Unit)? = null // 재생 완료 시 콜백
    lateinit var song: Album // 현재 재생 중인 노래

    private val playPauseListeners = mutableListOf<(Boolean) -> Unit>() // 재생/일시정지 상태 변경 리스너 목록

    // 재생/일시정지 상태 변경 리스너 추가
    fun addOnPlayPauseChangeListener(listener: ((Boolean) -> Unit)?) {
        playPauseListeners.add(listener!!)
    }

    // 재생/일시정지 상태 변경 리스너 제거
    fun removeOnPlayPauseChangeListener(listener: (Boolean) -> Unit) {
        playPauseListeners.remove(listener)
    }

    // 등록된 리스너에 재생 상태 알림
    private fun notifyPlayPauseChanged(isPlaying: Boolean) {
        playPauseListeners.forEach { it(isPlaying) }
    }

    // 노래 재생 시작
    fun play(album: Album) {
        stop() // 기존 재생 중지
        song = album
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(album.songUrl) // 노래 URL 설정
            prepareAsync() // 비동기 준비
            setOnPreparedListener {
                it.start() // 준비 완료 시 재생 시작
                onStartListener?.invoke() // 재생 시작 콜백 호출
            }
            setOnCompletionListener {
                releasePlayer() // 완료 후 해제
                Handler(Looper.getMainLooper()).postDelayed({
                    onCompletionListener?.invoke() // 재생 완료 콜백 호출
                }, 100)
            }
        }
        notifyPlayPauseChanged(true) // 재생 상태 알림
    }

    // 플레이어 해제
    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // 재생 중지
    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        notifyPlayPauseChanged(false)
    }

    // 일시정지
    fun pause() {
        mediaPlayer?.pause()
        notifyPlayPauseChanged(false)
    }

    // 일시정지한 재생 다시 시작
    fun resume() {
        mediaPlayer?.start()
        notifyPlayPauseChanged(true)
    }

    // 재생 시작 콜백 설정
    fun setOnStartListener(listener: () -> Unit) {
        onStartListener = listener
    }

    // 재생 완료 콜백 설정
    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    // 현재 재생 중인지 여부 반환
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}
