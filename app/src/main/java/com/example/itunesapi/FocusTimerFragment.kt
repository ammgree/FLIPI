package com.example.itunesapi

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class FocusTimerFragment : Fragment() {

    private lateinit var viewModel: TimerViewModel
    private var topic: String? = null
    private var secondsPassed = 0
    private var isRunning = false
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private lateinit var tvTimer: TextView
    private lateinit var tvTopicTitle: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    // 음악 관련 변수
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongUrl: String? = null
    private var currentSongTitle: String? = null
    private var isPlaying = false

    private lateinit var tvCurrentSong: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var btnPrevSong: Button
    private lateinit var btnNextSong: Button
    private lateinit var btnMusic: Button

    companion object {
        private const val MUSIC_LIBRARY_REQUEST_CODE = 1001

        fun newInstance(topic: String): FocusTimerFragment {
            val fragment = FocusTimerFragment()
            val args = Bundle()
            args.putString("topic", topic)
            fragment.arguments = args
            return fragment
        }
    }
    private val musicLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val selectedSongTitle = data?.getStringExtra("selectedSongTitle") ?: "선택된 노래 없음"
            tvCurrentSong.text = selectedSongTitle
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topic = arguments?.getString("topic")
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        handler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_focus_timer, container, false)

        tvTimer = view.findViewById(R.id.tvTimer)
        tvTopicTitle = view.findViewById(R.id.tvTopicTitle)
        btnStart = view.findViewById(R.id.btnStart)
        btnStop = view.findViewById(R.id.btnStop)

        // 음악 UI 초기화
        tvCurrentSong = view.findViewById(R.id.tvCurrentMusicTitle)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        btnPrevSong = view.findViewById(R.id.btnPrev)
        btnNextSong = view.findViewById(R.id.btnNext)
        btnMusic = view.findViewById(R.id.btnMusic)

        tvTopicTitle.text = topic ?: "주제 없음"
        tvTimer.text = formatTime(secondsPassed)
        updateMusicUI()
        updatePlayPauseButton()

        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    secondsPassed++
                    tvTimer.text = formatTime(secondsPassed)
                    handler.postDelayed(this, 1000)
                }
            }
        }

        btnStart.setOnClickListener {
            if (!isRunning) {
                isRunning = true
                handler.post(runnable)
            }
        }

        btnStop.setOnClickListener {
            isRunning = false
            handler.removeCallbacks(runnable)
        }

        btnPlayPause.setOnClickListener {
            if (mediaPlayer == null) return@setOnClickListener
            if (isPlaying) {
                mediaPlayer?.pause()
            } else {
                mediaPlayer?.start()
            }
            isPlaying = !isPlaying
            updatePlayPauseButton()
        }

        btnPrevSong.setOnClickListener {
            // 이전곡 기능 구현 예정
        }

        btnNextSong.setOnClickListener {
            // 다음곡 기능 구현 예정
        }

        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null

        // 타이머 분 단위로 저장
        topic?.let {
            val minutes = secondsPassed / 60
            viewModel.updateTime(it, minutes)
        }
    }

    private fun prepareMediaPlayer(url: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare()
        }
        isPlaying = false
        updatePlayPauseButton()
    }

    private fun updateMusicUI() {
        tvCurrentSong.text = currentSongTitle ?: "선택된 노래 없음"
    }

    private fun updatePlayPauseButton() {
        btnPlayPause.text = if (isPlaying) "정지" else "재생"
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}




