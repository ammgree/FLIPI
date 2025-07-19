package com.example.itunesapi

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class AlbumAdapter(private val albumList: List<Album>,
    private val onItemClick: (Album) -> Unit) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //RecyclerView를 상속받아서 데이터를 이어줌
        val albumArt: ImageView = itemView.findViewById(R.id.albumArt)
        val albumTitle: TextView = itemView.findViewById(R.id.albumTitle)
        val albumArtist: TextView = itemView.findViewById(R.id.albumArtist)
        val albumName: TextView = itemView.findViewById(R.id.albumName)

        fun bind(album: Album, onClick: (Album) -> Unit) {
            albumTitle.text = album.title
            albumArtist.text = album.artist
            albumName.text = album.album
            Glide.with(itemView.context)
                .load(album.imageUrl)
                .into(albumArt)
            itemView.setOnClickListener {
                onClick(album)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albumList[position], onItemClick)
    }

    override fun getItemCount(): Int = albumList.size
}
