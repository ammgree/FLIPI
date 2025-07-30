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

data class ViewPlaylist(
    val title: String,
    val songs: List<Album>
) : Serializable

class ViewPlaylistFragment : Fragment() {

    private lateinit var adapter: AlbumAdapter
    private lateinit var showPlaylistView: RecyclerView
    private lateinit var playlist: Playlist
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

            val result = Bundle().apply {
                putString("musicTitle", album.title)
                putString("musicUrl", album.songUrl)
                putString("musicArtist", album.artist)
                putString("albumArtUrl", album.imageUrl)
            }

            parentFragmentManager.setFragmentResult("songSelected", result)
            if (origin == "FocusTimer") {
                // FocusTimer가 스택에 있으면 pop하고 아니면 새로 띄우기
                val popped = parentFragmentManager.popBackStackImmediate("FocusTimer", 0)
                if (!popped) {
                    val fragment = FocusTimerFragment.newInstance(album.title, album.songUrl)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack("FocusTimer")
                        .commit()
                }
            } else {
                // origin이 FocusTimer가 아닐 경우, 뒤로가기나 화면 전환 하지 않음
                // 필요하면 여기서 다른 동작 처리
                // 예) Toast.makeText(requireContext(), "노래 선택 완료", Toast.LENGTH_SHORT).show()
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

        goBackbtn.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
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

                    val updatedSongs = currentSongs.filter { song->
                        song["title"] != album.title || song["artist"] != album.artist
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