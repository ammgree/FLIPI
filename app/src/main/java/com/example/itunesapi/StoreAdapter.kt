package com.example.itunesapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.itunesapi.Playlist

class StoreAdapter(
    private val playlists: List<Playlist>,
    private val onItemClick: (Playlist) -> Unit
) : RecyclerView.Adapter<StoreAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.playlistTitle)
        val imageView: ImageView = itemView.findViewById(R.id.playlistImage)

        fun bind(playlist: Playlist) {
            titleTextView.text = playlist.title
            Glide.with(itemView.context).load(playlist.picture).into(imageView)

            itemView.setOnClickListener {
                onItemClick(playlist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size
}
