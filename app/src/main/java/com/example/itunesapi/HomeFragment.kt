package com.example.itunesapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class StoryItem(
    val title: String,
    val artist: String,
    val albumArtUrl: String
)

class HomeFragment : Fragment() {

    private lateinit var storyRecyclerView: RecyclerView

    // 임시 데이터 (예시)
    private val storyList = mutableListOf(
        StoryItem("노래1", "아티스트1", "https://url-to-album-art1.jpg"),
        StoryItem("노래2", "아티스트2", "https://url-to-album-art2.jpg"),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storyRecyclerView = view.findViewById(R.id.storyRecyclerView)
        storyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        storyRecyclerView.adapter = StoryAdapter(storyList) { storyItem ->
            // 클릭된 StoryItem 데이터를 번들로 담아서 StoryDetailFragment로 이동
            val detailFragment = StoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("songTitle", storyItem.title)
                    putString("artistName", storyItem.artist)
                    putString("albumArtUrl", storyItem.albumArtUrl)
                }
            }

            // fragment_container는 액티비티에 있는 프래그먼트를 담는 FrameLayout id임
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null) // 뒤로가기 가능하도록
                .commit()
        }
    }
}
