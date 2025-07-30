package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class ViewSongFragment : Fragment() {

    private lateinit var songTitleTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var albumImageView: ImageView
    private lateinit var itplaylist: Playlist
    private lateinit var itsonglist : List<Album>
    private lateinit var goBackbtn : ImageButton
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        songTitleTextView = view.findViewById(R.id.songTitleTextView)
        artistTextView = view.findViewById(R.id.artistTextView)
        albumImageView = view.findViewById(R.id.albumImageView)
        goBackbtn = view.findViewById(R.id.goBackbtn)


        val playlist = arguments?.getSerializable("playlist") as? Playlist
        val album =arguments?.getSerializable("selectedAlbum") as? Album

        playlist?.let {
            itplaylist = it
            itsonglist = it.songs
            currentIndex = itsonglist.indexOfFirst { it.title == album?.title }

            if (currentIndex == -1) currentIndex = 0

            updateSongUI(itsonglist[currentIndex])
        }

        goBackbtn.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun updateSongUI(album: Album) {
        songTitleTextView.text = album.title
        artistTextView.text = album.artist
        Glide.with(this)
            .load(album.imageUrl)
            .into(albumImageView)
        MusicPlayerManager.play(album.songUrl)
    }
}