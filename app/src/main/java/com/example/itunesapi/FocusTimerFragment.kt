package com.example.itunesapi

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.itunesapi.databinding.FragmentFocusTimerBinding
import java.text.SimpleDateFormat
import java.util.*

class FocusTimerFragment : Fragment() {

    private var _binding: FragmentFocusTimerBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null
    private var timer: CountDownTimer? = null
    private var elapsedSeconds = 0
    private var isRunning = false

    private var playlist: List<Album> = emptyList()
    private var currentIndex = 0

    private lateinit var subjectName: String
    private lateinit var musicUrl: String

    private lateinit var timerViewModel: TimerViewModel

    companion object {
        private const val ARG_TOPIC = "subject"
        private const val ARG_MUSIC = "musicUrl"

        fun newInstance(subject: String, musicUrl: String): FocusTimerFragment {
            val fragment = FocusTimerFragment()
            val args = Bundle().apply {
                putString(ARG_TOPIC, subject)
                putString(ARG_MUSIC, musicUrl)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timerViewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        arguments?.let {
            subjectName = it.getString(ARG_TOPIC) ?: "기본"
            musicUrl = it.getString(ARG_MUSIC) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusTimerBinding.inflate(inflater, container, false)

        // 주제 이름 화면에 표시 (예: 텍스트뷰)
        binding.tvTopicTitle.text = subjectName  // tvSubjectName은 xml에 정의된 TextView id

        // 초기 타이머, 음악은 실행하지 않고, 버튼 클릭으로 시작하도록 세팅
        binding.btnStart.setOnClickListener {
            if (!isRunning) {
                startTimer()
                playMusic()
                isRunning = true
                binding.btnStart.isEnabled = false  // 시작 버튼 비활성화 (중복 실행 방지)
            }
        }

        binding.btnStop.setOnClickListener {
            if (isRunning) {
                stopTimer()
                stopMusic()
                isRunning = false
                binding.btnStart.isEnabled = true  // 다시 시작 가능하도록

                saveTime(subjectName, elapsedSeconds)
                timerViewModel.updateTime(subjectName, elapsedSeconds)
                Toast.makeText(requireContext(), "공부 기록 저장됨!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvTimer.text = "00:00"

        return binding.root
    }

    private fun setupViews() {
        binding.btnMusic.setOnClickListener {
            stopMusic()
            val storeFragment = StoreFragment()
            storeFragment.arguments = Bundle().apply {
                putString("origin", "FocusTimer")
                putString("subject", subjectName)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, storeFragment)
                .addToBackStack("StoreFragment")
                .commit()
        }

        binding.btnPrev.setOnClickListener {
            stopMusic()
            if (playlist.isNotEmpty()) {
                currentIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
                updateAndPlayCurrentSong()
            }
        }

        binding.btnNext.setOnClickListener {
            stopMusic()
            if (playlist.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % playlist.size
                updateAndPlayCurrentSong()
            }
        }
    }

    private fun updateAndPlayCurrentSong() {
        if (playlist.isNotEmpty()) {
            val song = playlist[currentIndex]
            selectAndPlaySong(song)
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(3600000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedSeconds++
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                stopMusic()
                saveTime(subjectName, elapsedSeconds)
                Toast.makeText(requireContext(), "1시간 완료!", Toast.LENGTH_SHORT).show()
                isRunning = false
                binding.btnStart.isEnabled = true
            }
        }
        timer?.start()
    }

    private fun playMusic() {
        if (musicUrl.isBlank()) {
            Toast.makeText(requireContext(),"음악 URL이 비어있습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(musicUrl)
            prepareAsync()
            setOnPreparedListener { it.start() }
            setOnErrorListener { _, _, _ ->
                Toast.makeText(requireContext(), "음악 재생 실패", Toast.LENGTH_SHORT).show()
                true
            }
        }
    }

    private fun stopMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            it.release()
        }
        mediaPlayer = null
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun saveTime(topic: String, seconds: Int) {
        val prefs = requireContext().getSharedPreferences("FocusTimerPrefs", 0)
        val editor = prefs.edit()

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val key = "$topic-$date"
        val previousTime = prefs.getInt(key, 0)
        val newTime = previousTime + seconds
        editor.putInt(key, newTime).apply()

        Toast.makeText(requireContext(), "[$key] $newTime 저장됨", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMusic()
        stopTimer()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            "songSelected",
            viewLifecycleOwner
        ) { _, bundle ->
            val selectedSong = bundle.getParcelable<Album>("album")
            val selectedPlaylist = bundle.getSerializable("playlist") as? Playlist

            if (selectedSong != null && selectedPlaylist != null) {
                playlist = selectedPlaylist.songs
                currentIndex = playlist.indexOfFirst { it == selectedSong }.coerceAtLeast(0)
                selectAndPlaySong(selectedSong)
            }
        }
        setupViews()
    }
    private fun selectAndPlaySong(song : Album) {
        musicUrl = song.songUrl
        subjectName = song.title

        binding.currentMusicBox.visibility = View.VISIBLE
        binding.tvCurrentMusicTitle.text = "재생 중: ${song.title} - ${song.artist}"

        Glide.with(this)
            .load(song.imageUrl)
            .placeholder(R.drawable.music_note)
            .into(binding.albumArt)

        stopMusic()
        playMusic()
    }
}
