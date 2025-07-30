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
        super.onViewCreated(view, savedInstanceState)
        val goBackbtn = view.findViewById<ImageButton>(R.id.goBackbtn)
        val PlaylistName = view.findViewById<TextView>(R.id.PlaylistName)
        showPlaylistView = view.findViewById(R.id.showPlaylistView)
        showPlaylistView.layoutManager = LinearLayoutManager(requireContext())

        val playlist = arguments?.getSerializable("playlist") as? Playlist
        playlist?.let {
            PlaylistName.text = it.title;
        }

        adapter = AlbumAdapter(albumList = playlist!!.songs, { album ->
            adapter.selectAlbum(album)

            val albumList = ArrayList(playlist.songs)
            val currentIndex = albumList.indexOf(album)

// Album 리스트를 SongItem 리스트로 변환
            val songItemList = albumList.map { album ->
                SongItem(
                    url = album.songUrl,
                    title = album.title,
                    artist = album.artist,
                    albumArtUrl = album.imageUrl
                )
            }.toCollection(ArrayList())

            val result = Bundle().apply {
                putString("musicTitle", album.title)
                putString("musicUrl", album.songUrl)
                putString("musicArtist", album.artist)
                putString("albumArtUrl", album.imageUrl)
                putSerializable("albumList", albumList)
                putInt("currentIndex", currentIndex)
            }

            parentFragmentManager.setFragmentResult("songSelected", result)

            if (origin == "FocusTimer") {
                // 타이머에서 왔으면 타이머 화면으로 돌아가기 or 새로 띄우기
                val popped = parentFragmentManager.popBackStackImmediate("FocusTimer", 0)
                if (!popped) {
                    val fragment = FocusTimerFragment.newInstance(
                        album.title,
                        album.songUrl,
                        songItemList,
                        currentIndex
                    )
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack("FocusTimer")
                        .commit()
                }
            } else {
                // 타이머에서 온 게 아니면 현재 화면 내에서 노래 재생만 처리
                Toast.makeText(requireContext(), "${album.title} 재생", Toast.LENGTH_SHORT).show()
                // 음악 재생 관련 메서드 호출 가능 (예: playMusic(album))
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