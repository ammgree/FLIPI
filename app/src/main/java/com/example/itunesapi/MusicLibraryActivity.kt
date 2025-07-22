package com.example.itunesapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MusicLibraryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlbumAdapter
    private val albumList = mutableListOf<Album>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_library)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AlbumAdapter(albumList) { album ->
            // 노래 선택 시 선택한 노래 정보(예: 제목, URL)를 FocusTimerFragment로 전달
            val intent = Intent()
            intent.putExtra("selectedSongTitle", album.title)
            intent.putExtra("selectedSongUrl", album.songUrl)
            setResult(Activity.RESULT_OK, intent)
            finish() // 액티비티 닫고 돌아가기
        }
        recyclerView.adapter = adapter

        // 여기에 iTunes API 호출해서 albumList 채우고 adapter.notifyDataSetChanged() 호출
        // 또는 이미 구현된 SearchFragment 코드를 참고해서 데이터를 채우세요
    }
}
