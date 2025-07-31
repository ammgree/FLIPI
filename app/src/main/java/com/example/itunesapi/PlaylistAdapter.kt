package com.example.itunesapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// RecyclerView 어댑터: 재생목록(Playlist) 항목을 리스트 형태로 화면에 출력하는 어댑터
class PlaylistAdapter(
    private val playlists: List<Playlist>,                         // 표시할 재생목록 리스트
    private val onItemClick: (Playlist) -> Unit,                   // 아이템 클릭 이벤트 콜백
    private val onItemLongClick : (Playlist) -> Unit               // 아이템 롱클릭 이벤트 콜백
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    // ViewHolder 클래스: 각 아이템 뷰를 관리
    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.playlistName)      // 재생목록 제목 뷰
        val playlistArt: ImageView = itemView.findViewById(R.id.playlistArt)       // 재생목록 이미지 뷰

        // 데이터 바인딩 함수
        fun bind(playlist: Playlist, onItemClick: (Playlist) -> Unit) {
            playlistName.text = playlist.title   // 재생목록 제목 표시
            Glide.with(itemView.context)         // Glide로 이미지 로딩
                .load(playlist.picture)
                .into(playlistArt)

            // 클릭 이벤트 연결
            itemView.setOnClickListener {
                onItemClick(playlist)
            }

            // 롱클릭 이벤트 연결
            itemView.setOnLongClickListener {
                onItemLongClick(playlist)
                true
            }
        }
    }

    // 아이템 뷰 생성 (ViewHolder 생성)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    // 아이템 데이터 바인딩
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position], onItemClick)
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int = playlists.size
}
