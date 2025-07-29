package com.example.itunesapi

import android.graphics.Color
import android.os.Parcelable
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Album(
    val title: String,
    val artist: String,
    val album: String,
    var imageUrl: String,
    val songUrl: String
) : Parcelable, Serializable

class AlbumAdapter(
    private val albumList: List<Album>,
    private val onItemClick: (Album) -> Unit,
    private val onItemLongClick: (Album) -> Unit = {} // 기본값 설정
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    var selectedAlbum: Album? = null

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumArt: ImageView = itemView.findViewById(R.id.albumArt)
        private val albumTitle: TextView = itemView.findViewById(R.id.albumTitle)
        private val albumArtist: TextView = itemView.findViewById(R.id.albumArtist)
        private val albumName: TextView = itemView.findViewById(R.id.albumName)
        private val itemLayout: LinearLayout = itemView.findViewById(R.id.item)

        fun bind(
            album: Album,
            onClick: (Album) -> Unit,
            onItemLongClick: (Album) -> Unit,
            selectedAlbum: Album?
        ) {
            albumTitle.text = album.title
            albumArtist.text = album.artist
            albumName.text = album.album

            Glide.with(itemView.context)
                .load(album.imageUrl)
                .into(albumArt)

            itemLayout.setBackgroundColor(
                if (album == selectedAlbum) Color.DKGRAY else Color.BLACK
            )

            itemView.setOnClickListener { onClick(album) }

            itemView.setOnLongClickListener {
                onItemLongClick(album)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albumList[position], onItemClick, onItemLongClick, selectedAlbum)
    }

    override fun getItemCount(): Int = albumList.size

    fun selectAlbum(album: Album) {
        if (selectedAlbum != album) {
            selectedAlbum = album
            notifyDataSetChanged()
        }
    }
}
