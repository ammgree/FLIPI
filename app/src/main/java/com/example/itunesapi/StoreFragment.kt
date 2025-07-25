package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StoreFragment : Fragment() {

    private lateinit var storeRecyclerView: RecyclerView
    private lateinit var storeAdapter: PlaylistAdapter
    private var storeList: MutableList<Playlist> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)

        // 뷰 바인딩
        val addButton = view.findViewById<ImageButton>(R.id.addPlaylist)
        val youtubeButton = view.findViewById<ImageButton>(R.id.youtubeButton)
        val playlistText = view.findViewById<TextView>(R.id.searchTitleText)
        storeRecyclerView = view.findViewById(R.id.storeView)

        storeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 초기 데이터
        storeList.add(Playlist("예시 플리", "https://example.com/image1.jpg"))

        // 어댑터 설정
        storeAdapter = PlaylistAdapter(storeList) { playlist ->
            Toast.makeText(requireContext(), "${playlist.title} 클릭됨", Toast.LENGTH_SHORT).show()
            // TODO: 플레이리스트 상세 페이지로 이동
        }
        storeRecyclerView.adapter = storeAdapter

        // "+" 버튼 클릭 시 빈 플레이리스트 추가
        addButton.setOnClickListener {
            // 다이얼로그로 제목 입력 받기
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("새 플레이리스트 생성")

            val input = android.widget.EditText(requireContext())
            input.hint = "플레이리스트 제목 입력"
            builder.setView(input)

            builder.setPositiveButton("추가") { dialog, _ ->
                val title = input.text.toString().ifBlank { "이름 없는 플레이리스트" }
                val imageUrl = "https://picsum.photos/300/200?random=${System.currentTimeMillis()}" // 랜덤 이미지
                val newPlaylist = Playlist(title, imageUrl)

                storeList.add(0, newPlaylist) // 최신순 맨 위로
                storeAdapter.notifyItemInserted(0)
                storeRecyclerView.scrollToPosition(0)

                dialog.dismiss()
            }

            builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }

            builder.show()
        }

        // 유튜브 버튼 눌렀을 때 동작 (검색 화면으로 이동할 수 있음)
        youtubeButton.setOnClickListener {
            Toast.makeText(requireContext(), "YouTube 검색창으로 이동", Toast.LENGTH_SHORT).show()
            // TODO: 유튜브 검색 화면으로 이동
        }

        storeAdapter = PlaylistAdapter(storeList) { selectedPlaylist ->
            val action = StoreFragmentDirections.actionStoreToEditPlaylist(
                title = selectedPlaylist.title,
                picture = selectedPlaylist.picture  // or selectedPlaylist.imageUrl, 네이밍에 맞게
            )
            findNavController().navigate(action)
        }


        return view
    }
}
