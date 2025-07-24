package com.example.itunesapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.itunesapi.Playlist

class PlaylistAdapter(
    private val playlist: List<Playlist>,
    private val onItemClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistArt : ImageView = itemView.findViewById(R.id.playlistArt)
        private val playlistName : TextView = itemView.findViewById(R.id.playlistName)
        private val item : LinearLayout = itemView.findViewById(R.id.playlist)

        fun bind(item : Playlist, onItemClick: (Playlist) -> Unit) {
            playlistName.text = item.title // 이거 아닌데 내가 정해야됨..

            Glide.with(itemView.context)
                .load(item.picture)
                .into(playlistArt)

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)

        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        // position에 해당하는 데이터를 bind() 함수로 넘김
        // 실제 데이터가 뷰에 표시됨
        holder.bind(playlist[position], onItemClick)
    }

    // 전체 몇 개의 데이터를 보여줄 건지 알려줌
    override fun getItemCount(): Int = playlist.size
}