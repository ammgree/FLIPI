package com.example.itunesapi

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlbumAdapter
    private val albumList = mutableListOf<Album>()
    private var mediaPlayer : MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val button = findViewById<Button>(R.id.button)
        val editText = findViewById<EditText>(R.id.searchEditText)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)


        adapter = AlbumAdapter(albumList) {
                album ->
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(album.songUrl)
                prepare()
                start()
            }
        }
        recyclerView.adapter = adapter

        button.setOnClickListener {
            Thread {
                try {
                    //입력한 노래 제목
                    val rawInput = editText.text.toString()
                    val term = URLEncoder.encode(rawInput, "UTF-8")

                    val urlSong = "https://itunes.apple.com/search?media=musicTrack&entity=song&country=kr&term=$term"
                    val urlAlbum = "https://itunes.apple.com/search?media=musicTrack&entity=album&country=kr&term=$term"

                    albumList.clear()

                    val songMap = makeMap(urlSong)
                    val albumMap = makeMap(urlAlbum)

                    for ((id, songData) in songMap) {
                        if (albumMap.containsKey(id)) {
                            albumList.add(songData)
                        }
                    }

                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }

                } catch (e:Exception){
                    Log.e("iTunesAPI", "오류 발생: $e")
                }
            }.start()
        }
    }
    fun makeMap(urls:String) : Map<String, Album>{
        //URL 객체로 만들기
        val url = URL(urls)

        //GET 요청하기
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        //5초동안만 데이터 받기
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        val stream = conn.inputStream
        val result = stream.bufferedReader().use { it.readText() }
        stream.close()

        val jsonResponse = JSONObject(result)
        val jsonArray = jsonResponse.getJSONArray("results")

        val madeMap = mutableMapOf<String, Album>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            var id = item.optString("collectionId")

            val title = item.optString("trackName")
            val artist = item.optString("artistName")
            val album = item.optString("collectionName")
            val albumArt = item.optString("artworkUrl100")
            val songUrl = item.optString("previewUrl")

            madeMap[id] = Album(title, artist, album, albumArt, songUrl)
        }
        return madeMap
    }
}