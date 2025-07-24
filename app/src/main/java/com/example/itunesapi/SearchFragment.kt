package com.example.itunesapi

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlbumAdapter
    // 검색해서 나올 노래 리스트
    private val albumList = mutableListOf<Album>()
    // 내 플레이리스트
    val playList = mutableListOf<Album>()
    private var mediaPlayer : MediaPlayer? = null
    lateinit var addButton : ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val button = view.findViewById<Button>(R.id.button)
        val editText = view.findViewById<EditText>(R.id.searchEditText)
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        addButton = view.findViewById<ImageButton>(R.id.addbutton)

        // 이 albumList는 검색결과로 나오는 노래들
        adapter = AlbumAdapter(albumList) { album ->
            adapter.selectAlbum(album)
            addButton.visibility = View.VISIBLE

            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(adapter.selectedAlbum?.songUrl)
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

                    activity?.runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }

                } catch (e:Exception){
                    Log.e("iTunesAPI", "오류 발생: $e")
                }
            }.start()
        }

        addButton.setOnClickListener {
            val song = adapter.selectedAlbum
            if(song != null)
                playList.add(song)
        }
    }


    private fun makeMap(urls:String) : Map<String, Album>{
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