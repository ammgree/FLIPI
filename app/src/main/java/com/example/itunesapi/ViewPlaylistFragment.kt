package com.example.itunesapi

import android.app.AlertDialog
import android.os.Bundle
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
import java.io.Serializable

class ViewPlaylistFragment : Fragment() {

    private lateinit var adapter: AlbumAdapter
    private var origin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        origin = arguments?.getString("origin")
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
            val origin = arguments?.getString("origin")

            if (origin == "archive") {
                // 프로필 프래그먼트를 새로 만들되, 보관함 탭을 선택한 상태로 전달
                val profileFragment = ProfileFragment().apply {
                    arguments = Bundle().apply {
                        putString("selectedTab", "archive")
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .commit()
            } else {
                requireActivity().supportFragmentManager.popBackStack()
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