package com.example.itunesapi

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewPlaylistFragment : Fragment() {

    private lateinit var adapter: AlbumAdapter
    private var userId: String? = null
    private lateinit var origin: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        origin = arguments?.getString("origin") ?: ""
        userId = arguments?.getString("userId")
        Log.d("origin", "origin: $origin")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val goBackbtn = view.findViewById<ImageButton>(R.id.goBackbtn)
        val PlaylistName = view.findViewById<TextView>(R.id.PlaylistName)
        val showPlaylistView = view.findViewById<RecyclerView>(R.id.showPlaylistView)


        val playlist = arguments?.getSerializable("playlist") as? Playlist
        playlist?.let {
            PlaylistName.text = it.title;
        }
        showPlaylistView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AlbumAdapter(albumList = playlist!!.songs, { album ->
            if (origin == "FocusTimer") {
                // FocusTimerFragment에 정보 전달
                val subject = arguments?.getString("subject") ?: "기본"
                val resultBundle = Bundle().apply {
                    putParcelable("album", album)
                    putSerializable("playlist", playlist)
                }
                parentFragmentManager.setFragmentResult("songSelected", resultBundle)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container,
                        FocusTimerFragment.newInstance(subject, album.songUrl))
                    .addToBackStack(null)
                    .commit()
            } else {
                // ViewSongFragment에 정보 전달
                adapter.selectAlbum(album)
                adapter.selectedAlbum?.let { album ->
                    MusicPlayerManager.play(album)
                }
                val bundle = Bundle().apply {
                    putSerializable("playlist", playlist)
                    putSerializable("selectedAlbum", adapter.selectedAlbum)
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container,ViewSongFragment().apply {
                        arguments = bundle
                    })
                    .addToBackStack(null)
                    .commit()
            }
        }, { album ->
            AlertDialog.Builder(requireContext())
                .setTitle("노래 삭제")
                .setMessage("「${album.title}」을 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    val user = FirebaseAuth.getInstance().currentUser
                    val uid = user?.uid ?: return@setPositiveButton

                    deleteAlbum(uid, playlist.title, album) {
                        playlist.songs.remove(album)
                        adapter.notifyDataSetChanged()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        })
        showPlaylistView.adapter = adapter

        goBackbtn.setOnClickListener {
            val viewedUserId = arguments?.getString("viewedUserId")
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            when (origin) {
                "store" -> {
                    // 스토어에서 들어온 경우 → StoreFragment로 돌아가기
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, StoreFragment())
                        .commit()
                }
                "FocusTimer" -> {
                    // 포커스타이머에서 들어온 경우 → 이전 프래그먼트로 단순 백스택 pop
                    parentFragmentManager.popBackStack()
                }
                else -> {
                    // 프로필에서 들어온 경우 (내 프로필 or 다른 사람 프로필)
                    if (viewedUserId != null && viewedUserId != currentUserId) {
                        val fragment = OtherUserProfileFragment.newInstance(viewedUserId)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit()
                    } else {
                        val fragment = ProfileFragment().apply {
                            arguments = Bundle().apply {
                                putString("selectedTab", "archive")
                                putString("userId", currentUserId)
                            }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit()
                    }
                }
            }
        }



    }
    fun deleteAlbum(userId: String, playlistTitle: String, album: Album, onComplete: () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("playlists")
            .whereEqualTo("title", playlistTitle)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val docRef = db.collection("users")
                        .document(userId)
                        .collection("playlists")
                        .document(doc.id)
                    val playlistData = doc.data
                    val currentSongs = playlistData["songs"] as? List<HashMap<String, Any>> ?: continue

                    val updatedSongs = currentSongs.filter { song ->
                        val title = song["title"] as? String
                        val artist = song["artist"] as? String
                        title != album.title || artist != album.artist
                    }


                    docRef.update("songs", updatedSongs)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "노래가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }

            }
            .addOnFailureListener {
            Toast.makeText(requireContext(), "조회 실패: ${it.message}", Toast.LENGTH_SHORT).show()
        }

    }
}