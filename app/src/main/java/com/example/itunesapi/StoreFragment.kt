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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StoreFragment : Fragment() {

    private lateinit var storeRecyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)
        val mainActivity = requireActivity() as MainActivity

        // 뷰 바인딩
        val addButton = view.findViewById<ImageButton>(R.id.addPlaylist)
        val youtubeButton = view.findViewById<ImageButton>(R.id.youtubeButton)
        storeRecyclerView = view.findViewById(R.id.storeView)

        storeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 설정
        adapter = PlaylistAdapter(mainActivity.playLists) { selectedPlaylist ->
            Toast.makeText(requireContext(), "${selectedPlaylist.title} 클릭됨", Toast.LENGTH_SHORT).show()
        }
        storeRecyclerView.adapter = adapter

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

                mainActivity.playLists.add(0, newPlaylist) // 최신순 맨 위로
                adapter.notifyItemInserted(0)
                storeRecyclerView.scrollToPosition(0)

                val user = FirebaseAuth.getInstance().currentUser
                val uid = user?.uid
                if (uid == null) {
                    Toast.makeText(requireContext(), "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val explaylist = hashMapOf(
                    "title" to title,
                    "picture" to imageUrl,
                    "songs" to emptyList<Map<String, Any>>()
                )

                val db = FirebaseFirestore.getInstance()
               db.collection("users").document(uid)
                   .collection("playlists")
                   .add(explaylist)
                   .addOnSuccessListener {
                       Toast.makeText(requireContext(), "저장 성공!", Toast.LENGTH_SHORT).show()
                   }

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


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("playlists")
            .get()
            .addOnSuccessListener { documents ->
                val playlistList = mutableListOf<Playlist>()

                for (doc in documents) {
                    val title = doc.getString("title") ?: ""
                    val picture = doc.getString("picture")
                    val songsData = doc.get("songs") as? List<Map<String,Any>> ?: emptyList()

                    val songs = songsData.map {
                        Album(
                            title = it["title"] as String,
                            artist = it["artist"] as String,
                            album = it["album"] as String,
                            imageUrl = it["imageUrl"] as String,
                            songUrl = it["songUrl"] as String
                        )
                    }.toMutableList()
                    playlistList.add(Playlist(title,picture,songs))
                }
                adapter = PlaylistAdapter(playlistList) { selectedPlaylist ->
                    Toast.makeText(requireContext(), "${selectedPlaylist.title} 클릭됨", Toast.LENGTH_SHORT).show()
                }

                storeRecyclerView.adapter = adapter
            }
    }
}
