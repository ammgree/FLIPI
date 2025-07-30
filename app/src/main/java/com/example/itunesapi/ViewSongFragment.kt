package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
    private lateinit var itsonglist: List<Album>
    private lateinit var goBackbtn: ImageButton
    private var currentIndex = 0
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton

    // ✅ 리스너 변수화 (중복 등록/제거 방지)
    private val playPauseListener: (Boolean) -> Unit = { isPlaying ->
        playButton.visibility = if (isPlaying) GONE else VISIBLE
        pauseButton.visibility = if (isPlaying) VISIBLE else GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        songTitleTextView = view.findViewById(R.id.songTitleTextView)
        artistTextView = view.findViewById(R.id.artistTextView)
        albumImageView = view.findViewById(R.id.albumImageView)
        goBackbtn = view.findViewById(R.id.goBackbtn)
        playButton = view.findViewById(R.id.play)
        pauseButton = view.findViewById(R.id.pause)
        val previous = view.findViewById<ImageButton>(R.id.skip_previous)
        val next = view.findViewById<ImageButton>(R.id.skip_next)

        val playlist = arguments?.getSerializable("playlist") as? Playlist
        val album = arguments?.getSerializable("selectedAlbum") as? Album

        // ✅ 리스너 등록
        MusicPlayerManager.addOnPlayPauseChangeListener(playPauseListener)

        playlist?.let {
            itplaylist = it
            itsonglist = it.songs
            currentIndex = itsonglist.indexOfFirst { it.title == album?.title }
            if (currentIndex == -1) currentIndex = 0

            updateSongUI(itsonglist[currentIndex])

            previous.setOnClickListener {
                currentIndex = if (currentIndex == 0) itsonglist.size - 1 else currentIndex - 1
                updateSongUI(itsonglist[currentIndex])
            }

            next.setOnClickListener {
                currentIndex = if (currentIndex == itsonglist.size - 1) 0 else currentIndex + 1
                updateSongUI(itsonglist[currentIndex])
            }

            playButton.setOnClickListener {
                MusicPlayerManager.resume()
            }

            pauseButton.setOnClickListener {
                MusicPlayerManager.pause()
            }

            // ✅ fragment가 activity에 attach되어 있는지 확인
            MusicPlayerManager.setOnCompletionListener {
                activity?.runOnUiThread {
                    currentIndex = if (currentIndex == itsonglist.size - 1) 0 else currentIndex + 1
                    updateSongUI(itsonglist[currentIndex])
                }
            }
        }

        goBackbtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun updateSongUI(album: Album) {
        songTitleTextView.text = album.title
        artistTextView.text = album.artist
        Glide.with(this)
            .load(album.imageUrl)
            .into(albumImageView)
        MusicPlayerManager.play(album)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ✅ 리스너 해제
        MusicPlayerManager.removeOnPlayPauseChangeListener(playPauseListener)
    }
}
