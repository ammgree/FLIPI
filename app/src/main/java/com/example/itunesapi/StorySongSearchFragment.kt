package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StorySongSearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlbumAdapter
    private val albumList = mutableListOf<Album>() // 검색 결과 앨범 목록

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_story_song_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. RecyclerView 설정
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = AlbumAdapter(albumList) { album ->
            // 아이템 클릭 시 선택한 앨범 정보 전달
            val resultBundle = Bundle().apply {
                putString("songTitle", album.title)
                putString("artistName", album.artist)
                putString("albumArtUrl", album.imageUrl)
            }
            parentFragmentManager.setFragmentResult("storySongSelected", resultBundle)
            parentFragmentManager.popBackStack() // 프래그먼트 종료
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 2. 초기 검색어로 앨범 불러오기 (임시로 BTS 검색)
        searchAlbums("BTS")
    }

    private fun searchAlbums(query: String) {
        // TODO: iTunes API 연동 예정
        // 추후 여기에 Retrofit 등을 활용해 API 호출 후 albumList에 결과 추가하고
        // adapter.notifyDataSetChanged()를 호출할 예정
    }
}
