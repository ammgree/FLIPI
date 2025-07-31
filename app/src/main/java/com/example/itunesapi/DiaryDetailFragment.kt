package com.example.itunesapi

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment

class DiaryDetailFragment(private val diaryItem: DiaryItem) : Fragment() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_detail, container, false)

        val titleText = view.findViewById<TextView>(R.id.textTitle)
        val contentText = view.findViewById<TextView>(R.id.textContent)
        val dateText = view.findViewById<TextView>(R.id.textDate)
        val visibilityText = view.findViewById<TextView>(R.id.textVisibility)
        val backButton = view.findViewById<ImageButton>(R.id.btnBack)

        // 음악 텍스트 관련 뷰 찾기
        val musicTitleText = view.findViewById<TextView>(R.id.textMusicTitle)
        val musicArtistText = view.findViewById<TextView>(R.id.textMusicArtist)
        val musicInfoLayout = view.findViewById<View>(R.id.layoutMusicInfo)  // LinearLayout 전체

        // UI 설정
        titleText.text = diaryItem.title
        contentText.text = diaryItem.content
        dateText.text = diaryItem.date
        visibilityText.text = if (diaryItem.isPublic) "공개" else "비공개"

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }




        if (!diaryItem.musicTitle.isNullOrEmpty() && !diaryItem.musicArtist.isNullOrEmpty()) {
            musicTitleText.text = diaryItem.musicTitle
            musicArtistText.text = diaryItem.musicArtist
            musicInfoLayout.visibility = View.VISIBLE
        }


        // 음악 재생
        diaryItem.musicUrl?.let { url ->
            if (url.isNotEmpty()) {
                stopCurrentPlayback()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(url)
                    prepareAsync()
                    setOnPreparedListener {
                        it.start()
                    }
                    setOnCompletionListener {
                        it.release()
                    }
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopCurrentPlayback()
    }

    private fun stopCurrentPlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }
}
