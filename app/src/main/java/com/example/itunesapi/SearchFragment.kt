package com.example.itunesapi

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
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
    private var mediaPlayer : MediaPlayer? = null
    lateinit var addButton : ImageButton
    private var fromDiaryAdd: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fromDiaryAdd = arguments?.getBoolean("fromDiaryAdd", false) == true
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

            // <도연>싱글톤으로 음악 재생 자세한 내용은 MusicPlayerManager가셔서 보시면 됩니다
            // ✅ 싱글톤으로 음악 재생
            adapter.selectedAlbum?.songUrl?.let { MusicPlayerManager.play(it) }

            if (fromDiaryAdd) {
                // ✨ 일기 작성화면에서 왔을 때는 결과 전달 후 돌아가기
                val resultBundle = Bundle().apply {
                    putString("songTitle", album.title)
                    putString("songArtist", album.artist)
                    putString("songUrl", album.songUrl)
                    putString("albumImage", album.imageUrl)
                }
                parentFragmentManager.setFragmentResult("songSelected", resultBundle)
                parentFragmentManager.popBackStack()
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

        addButton.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.add_song, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_add-> {
                        val mainActivity = requireActivity() as MainActivity
                        mainActivity.playLists.forEachIndexed { index, playlist ->
                            Log.d("StoreFragment", "플레이리스트[$index]: ${playlist.title}, ${playlist.picture}")
                        }

                        val playlistTitles = mainActivity.playLists.map { it.title }.toTypedArray()

                        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        builder.setTitle("플레이리스트 선택")
                        builder.setItems(playlistTitles) { dialog, which ->
                            val selectedPlaylist = mainActivity.playLists[which]
                            adapter.selectedAlbum?.let { selectedPlaylist.songs.add(it) }
                            Toast.makeText(requireContext(), "'${selectedPlaylist.title}'에 추가되었습니다!", Toast.LENGTH_SHORT).show()
                        }
                        builder.show()
                        true
                    }
                    R.id.menu_add_new -> {
                        val songBundle = Bundle().apply {
                            putParcelable("album", adapter.selectedAlbum)
                        }

                        val fragment = EditPlaylistFragment().apply {
                            arguments = songBundle
                        }

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> false
                }
            }

            popup.show()
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