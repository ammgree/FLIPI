package com.example.itunesapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditPlaylistFragment : Fragment() {

    private lateinit var titleEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveButton: Button
    val mysongPlaylist = Playlist("", "")


    private lateinit var song : Album

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        song = arguments?.getParcelable("album")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.titleEditText)
        imageView = view.findViewById(R.id.imageView)
        saveButton = view.findViewById(R.id.saveButton)

        Glide.with(requireContext()).load(song.imageUrl).into(imageView)

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString()
            val resultBundle = Bundle().apply {
                putString("newPlaylistTitle", newTitle)
            }

            mysongPlaylist.title = newTitle
            mysongPlaylist.picture = song.imageUrl
            mysongPlaylist.songs.add(song)

            val mainActivity = requireActivity() as MainActivity
            mainActivity.playLists.add(0, mysongPlaylist)

            val user = FirebaseAuth.getInstance().currentUser
            val uid = user?.uid ?: return@setOnClickListener

            val db = FirebaseFirestore.getInstance()

            val playlistDTO = PlaylistDTO(
                title = mysongPlaylist.title,
                picture = mysongPlaylist.picture,
                songs = mysongPlaylist.songs.map { it.toMap() }
            )

            db.collection("users").document(uid)
                .collection("playlists")
                .add(playlistDTO)
                .addOnSuccessListener {
                    if (isAdded) {
                        parentFragmentManager.setFragmentResult("newPlaylist", resultBundle)
                        Toast.makeText(requireContext(), "저장 성공!", Toast.LENGTH_SHORT).show()

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, StoreFragment())
                            .commit()
                    }
                }
                .addOnFailureListener { e ->
                    if (isAdded) {
                        Toast.makeText(requireContext(), "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}
