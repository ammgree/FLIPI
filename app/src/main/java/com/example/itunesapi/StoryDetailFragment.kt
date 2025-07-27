package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

// StoryDetailFragment: 선택한 노래의 상세 정보를 보여주는 프래그먼트
class StoryDetailFragment : Fragment() {

    // 나중에 전달받을 수도 있는 추가 데이터(예: storyId나 사용자ID 등)를 위한 변수
    private var storyId: String? = null
    private var userId: String? = null

    private lateinit var songTitleTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var albumImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 전달받은 인자값 초기화
        arguments?.let {
            storyId = it.getString(ARG_STORY_ID)
            userId = it.getString(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트의 레이아웃 설정
        return inflater.inflate(R.layout.fragment_story_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 요소 연결
        songTitleTextView = view.findViewById(R.id.songTitleTextView)
        artistTextView = view.findViewById(R.id.artistTextView)
        albumImageView = view.findViewById(R.id.albumImageView)

        // 검색창에서 선택한 노래 정보를 수신
        parentFragmentManager.setFragmentResultListener("storySongSelected", viewLifecycleOwner) { _, bundle ->
            val title = bundle.getString("songTitle")
            val artist = bundle.getString("artistName")
            val imageUrl = bundle.getString("albumArtUrl")

            // UI에 표시
            songTitleTextView.text = title
            artistTextView.text = artist
            Glide.with(requireContext()).load(imageUrl).into(albumImageView)
        }
    }

    companion object {
        // 나중에 Story를 구분할 때 사용할 수 있는 키값
        private const val ARG_STORY_ID = "storyId"
        private const val ARG_USER_ID = "userId"

        // 스토리 ID와 사용자 ID를 넘기고 싶은 경우 사용할 생성자 함수
        @JvmStatic
        fun newInstance(storyId: String, userId: String) =
            StoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STORY_ID, storyId)
                    putString(ARG_USER_ID, userId)
                }
            }
    }
}
