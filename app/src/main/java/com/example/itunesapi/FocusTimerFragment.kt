package com.example.itunesapi

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.itunesapi.databinding.FragmentFocusTimerBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FocusTimerFragment : Fragment() {

    private var _binding: FragmentFocusTimerBinding? = null
    private val binding get() = _binding!!

    // 스톱워치 관련 변수
    private var isRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private var timerTask: Timer? = null

    // 음악 재생 관련
    private var mediaPlayer: MediaPlayer? = null
    private var playlist: ArrayList<SongItem> = arrayListOf()  // 재생목록
    private var currentIndex = 0

    private var subjectName: String = ""
    private var musicUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectName = it.getString("subject") ?: ""
            musicUrl = it.getString("musicUrl") ?: ""
            playlist = it.getParcelableArrayList<SongItem>("playlist") ?: arrayListOf()
            currentIndex = it.getInt("currentIndex", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusTimerBinding.inflate(inflater, container, false)

        // 타이틀 표시
        binding.tvTopicTitle.text = subjectName

        // 타이머 초기화 텍스트
        binding.tvTimer.text = "00:00"

        // 버튼 클릭 리스너 설정
        binding.btnStart.setOnClickListener {
            if (!isRunning) startStopwatch()
        }

        binding.btnStop.setOnClickListener {
            if (isRunning) stopStopwatch()
        }

        binding.btnMusic.setOnClickListener {
            // StoreFragment로 이동해서 음악 선택하게 함
            val storeFragment = StoreFragment()
            storeFragment.arguments = Bundle().apply {
                putString("origin", "FocusTimer")
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, storeFragment)
                .addToBackStack("StoreFragment")
                .commit()
        }

        binding.btnPlayPause.setOnClickListener {
            toggleMusic()
        }

        binding.btnNext.setOnClickListener {
            playNext()
        }

        binding.btnPrev.setOnClickListener {
            playPrevious()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fragment로부터 플레이리스트 및 인덱스 받기 (StoreFragment에서 songSelected 결과)
        parentFragmentManager.setFragmentResultListener("songSelected", viewLifecycleOwner) { _, bundle ->
            val musicList = bundle.getParcelableArrayList<SongItem>("playlist")
            val selectedIndex = bundle.getInt("selectedIndex", 0)

            if (musicList != null && musicList.isNotEmpty()) {
                playlist = musicList
                currentIndex = selectedIndex
                updateAndPlayCurrentSong()
            } else {
                // 단일 곡만 선택했을 경우 처리
                val musicUrl = bundle.getString("musicUrl") ?: ""
                val musicTitle = bundle.getString("musicTitle") ?: ""
                val musicArtist = bundle.getString("musicArtist") ?: ""
                val albumArtUrl = bundle.getString("albumArtUrl") ?: ""

                this.musicUrl = musicUrl
                this.subjectName = musicTitle
                binding.tvTopicTitle.text = subjectName

                binding.currentMusicBox.visibility = View.VISIBLE
                binding.tvCurrentMusicTitle.text = "재생 중: $musicTitle - $musicArtist"

                Glide.with(this)
                    .load(albumArtUrl)
                    .placeholder(R.drawable.music_note)
                    .into(binding.albumArt)

                stopMusic()
                playMusic(musicUrl)
            }
        }
    }

    // 스톱워치 시작
    private fun startStopwatch() {
        isRunning = true
        startTime = SystemClock.elapsedRealtime() - elapsedTime

        timerTask = timer(period = 1000) {
            val now = SystemClock.elapsedRealtime()
            elapsedTime = now - startTime
            val minutes = (elapsedTime / 1000 / 60).toInt()
            val seconds = ((elapsedTime / 1000) % 60).toInt()

            activity?.runOnUiThread {
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }
        }
    }

    // 스톱워치 정지 및 Firebase 저장
    private fun stopStopwatch() {
        isRunning = false
        timerTask?.cancel()
        timerTask = null

        val totalSeconds = (elapsedTime / 1000).toInt()
        saveStudyTimeToFirebase(subjectName, totalSeconds)

        Toast.makeText(requireContext(), "스톱워치 종료: ${binding.tvTimer.text}", Toast.LENGTH_SHORT).show()
    }

    // 현재 재생중인 곡 UI 업데이트 및 음악 재생
    private fun updateAndPlayCurrentSong() {
        if (playlist.isEmpty()) {
            binding.tvCurrentMusicTitle.text = "재생목록 없음"
            binding.albumArt.setImageResource(R.drawable.music_note)
            return
        }

        val song = playlist[currentIndex]
        musicUrl = song.url
        subjectName = song.title
        binding.tvTopicTitle.text = subjectName

        binding.currentMusicBox.visibility = View.VISIBLE
        binding.tvCurrentMusicTitle.text = "재생 중: ${song.title} - ${song.artist}"

        Glide.with(this)
            .load(song.albumArtUrl)
            .placeholder(R.drawable.music_note)
            .into(binding.albumArt)

        stopMusic()
        playMusic(musicUrl)
    }

    // 음악 재생
    private fun playMusic(url: String) {
        if (url.isBlank()) return

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { it.start() }
            setOnErrorListener { _, _, _ ->
                Toast.makeText(requireContext(), "음악 재생 실패", Toast.LENGTH_SHORT).show()
                true
            }
            setOnCompletionListener {
                // 자동 다음곡 재생
                playNext()
            }
        }
        binding.btnPlayPause.text = "일시정지"
    }

    // 음악 일시정지 / 재생 토글
    private fun toggleMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                binding.btnPlayPause.text = "재생"
            } else {
                it.start()
                binding.btnPlayPause.text = "일시정지"
            }
        } ?: run {
            // 플레이어가 없으면 현재곡 재생 시도
            if (musicUrl.isNotBlank()) playMusic(musicUrl)
        }
    }

    // 다음 곡 재생
    private fun playNext() {
        if (playlist.isEmpty()) return
        currentIndex = (currentIndex + 1) % playlist.size
        updateAndPlayCurrentSong()
    }

    // 이전 곡 재생
    private fun playPrevious() {
        if (playlist.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
        updateAndPlayCurrentSong()
    }

    // 음악 정지 및 리소스 해제
    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        binding.btnPlayPause.text = "재생"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerTask?.cancel()
        mediaPlayer?.release()
        _binding = null
    }

    companion object {
        fun newInstance(
            subject: String,
            musicUrl: String,
            playlist: ArrayList<SongItem>,
            currentIndex: Int
        ): FocusTimerFragment {
            val fragment = FocusTimerFragment()
            val args = Bundle()
            args.putString("subject", subject)
            args.putString("musicUrl", musicUrl)
            args.putParcelableArrayList("playlist", playlist)
            args.putInt("currentIndex", currentIndex)
            fragment.arguments = args
            return fragment
        }
    }

    // 스톱워치 정지 시 호출되는 함수 안에서 저장하도록 하거나 별도로 호출 가능
    private fun saveStudyTimeToFirebase(topic: String, seconds: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val uid = user.uid

        // 저장할 날짜 문자열 (ex: 2025-07-30)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val studyData = hashMapOf(
            "date" to today,
            "topic" to topic,
            "studySeconds" to seconds
        )

        // users 컬렉션 > uid 문서 > study_times 서브컬렉션 > 오늘 날짜 문서에 저장
        db.collection("users")
            .document(uid)
            .collection("study_times")
            .document(today)
            .set(studyData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "공부 시간 저장 완료: ${seconds / 60}분", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
