package com.example.itunesapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ViewPlaylistFragment : Fragment() {

    private lateinit var adapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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

        adapter = AlbumAdapter(albumList = playlist!!.songs){ album ->
            adapter.selectAlbum(album)
            adapter.selectedAlbum?.songUrl?.let { MusicPlayerManager.play(it) }
        }
        showPlaylistView.adapter = adapter

        goBackbtn.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}