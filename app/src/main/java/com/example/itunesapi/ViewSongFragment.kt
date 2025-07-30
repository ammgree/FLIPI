package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val Backbtn = view.findViewById<ImageButton>(R.id.Backbtn)
        songTitleTextView = view.findViewById(R.id.songTitleTextView)
        artistTextView = view.findViewById(R.id.artistTextView)
        albumImageView = view.findViewById(R.id.albumImageView)


        val playlist = arguments?.getSerializable("playlist") as? Playlist
        val album =arguments?.getSerializable("selectedAlbum") as? Album

        val previous = view.findViewById<ImageButton>(R.id.skip_previous)
        val playButton = view.findViewById<ImageButton>(R.id.play)
        val pauseButton = view.findViewById<ImageButton>(R.id.pause)
        val next = view.findViewById<ImageButton>(R.id.skip_next)

        playlist?.let {
            itplaylist = it
            itsonglist = it.songs
            currentIndex = itsonglist.indexOfFirst { it.title == album?.title }

            if (currentIndex == -1) currentIndex = 0

            updateSongUI(itsonglist[currentIndex])

            previous.setOnClickListener {
                if (currentIndex == 0)
                    currentIndex = itsonglist.size-1
                else
                    currentIndex--
                updateSongUI(itsonglist[currentIndex])
            }

            next.setOnClickListener {
                if(currentIndex == itsonglist.size-1)
                    currentIndex = 0
                else
                    currentIndex++
                updateSongUI(itsonglist[currentIndex])
            }

            playButton.setOnClickListener {
                MusicPlayerManager.resume()
                playButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
            }
            pauseButton.setOnClickListener {
                MusicPlayerManager.pause()
                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.GONE
            }
        }

        Backbtn.setOnClickListener{
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