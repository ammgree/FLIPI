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
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    var imageUrl: String = "",
    val songUrl: String = ""
) : Parcelable, Serializable

class AlbumAdapter(
    private val albumList: List<Album>,
    private val onItemClick: (Album) -> Unit,
    private val onItemLongClick: (Album) -> Unit = {} // 기본값 설정
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    var selectedAlbum: Album? = null

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 뷰를 연결하기
        val albumArt: ImageView = itemView.findViewById(R.id.albumArt)
        val albumTitle: TextView = itemView.findViewById(R.id.albumTitle)
        val albumArtist: TextView = itemView.findViewById(R.id.albumArtist)
        val albumName: TextView = itemView.findViewById(R.id.albumName)
        val item : LinearLayout = itemView.findViewById(R.id.item)


        // 뷰에 앨범 데이터를 넣고 클릭 이벤트 설정
        fun bind(album: Album, onClick: (Album) -> Unit, onItemLongClick: (Album) -> Unit, selectedAlbum: Album?) {
            albumTitle.text = album.title
            albumArtist.text = album.artist
            albumName.text = album.album

            Glide.with(itemView.context)
                .load(album.imageUrl)
                .into(albumArt)

            if (album == selectedAlbum)
                item.setBackgroundColor(Color.DKGRAY)
            else
                item.setBackgroundColor(Color.BLACK)
            // 아이템 클릭하면 onClick 실행
            itemView.setOnClickListener {
                onClick(album)
            }
            itemView.setOnLongClickListener {
                onItemLongClick(album)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        // item_album.xml을 꺼내서 뷰로 만듦
        // AlbumViewHolder에 넣어서 반환
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        // position에 해당하는 데이터를 bind() 함수로 넘김
        // 실제 데이터가 뷰에 표시됨
        holder.bind(albumList[position], onItemClick, onItemLongClick, selectedAlbum)
    }

    // 전체 몇 개의 데이터를 보여줘야하는지 recyclerView가 알 수 있음
    override fun getItemCount(): Int = albumList.size

    fun selectAlbum(album:Album){
        if(selectedAlbum != album)
            selectedAlbum = album
        notifyDataSetChanged()
    }
}
