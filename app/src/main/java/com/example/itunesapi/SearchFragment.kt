package com.example.itunesapi

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlbumAdapter
    private val albumList = mutableListOf<Album>()
    private var mediaPlayer: MediaPlayer? = null
    lateinit var addButton: ImageButton
    private var fromDiaryAdd: Boolean = false
    private var searchAttribute = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fromDiaryAdd = arguments?.getBoolean("fromDiaryAdd", false) == true
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val button = view.findViewById<ImageButton>(R.id.searchButton)
        val editText = view.findViewById<EditText>(R.id.searchEditText)
        val tabAll = view.findViewById<TextView>(R.id.tabAll)
        val tabSong = view.findViewById<TextView>(R.id.tabSong)
        val tabArtist = view.findViewById<TextView>(R.id.tabArtist)
        val tabAlbum = view.findViewById<TextView>(R.id.tabAlbum)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        addButton = view.findViewById(R.id.addbutton)

        adapter = AlbumAdapter(
            albumList,
            onItemClick = { album ->
                adapter.selectAlbum(album)
                addButton.visibility = View.VISIBLE
                adapter.selectedAlbum?.songUrl?.let { MusicPlayerManager.play(it) }

                if (fromDiaryAdd) {
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
        )



        recyclerView.adapter = adapter

        // 탭 초기화
        highlightSelectedTab(tabAll)

        tabAll.setOnClickListener {
            searchAttribute = ""
            highlightSelectedTab(tabAll)
        }

        tabSong.setOnClickListener {
            searchAttribute = "attribute=songTerm"
            highlightSelectedTab(tabSong)
        }

        tabArtist.setOnClickListener {
            searchAttribute = "attribute=artistTerm"
            highlightSelectedTab(tabArtist)
        }

        tabAlbum.setOnClickListener {
            searchAttribute = "attribute=albumTerm"
            highlightSelectedTab(tabAlbum)
        }

        // 검색 버튼 클릭 시
        button.setOnClickListener {
            Thread {
                try {
                    val rawInput = editText.text.toString()
                    val term = URLEncoder.encode(rawInput, "UTF-8")
                    val baseUrl =
                        "https://itunes.apple.com/search?media=music&entity=song&country=kr"
                    val isEnglish = rawInput.matches(Regex("^[a-zA-Z0-9\\s]+$"))


                    val urlSong = if (searchAttribute.isNotEmpty()) {
                        "$baseUrl&term=$term&$searchAttribute"
                    } else {
                        "$baseUrl&term=$term"
                    }




                    Log.d("iTunesAPI", "요청 URL: $urlSong")

                    val songMap = makeMap(urlSong)
                    val allResults = songMap.values.toMutableList()

                    val sortedResults = when {
                        searchAttribute.contains("artistTerm", ignoreCase = true) -> {
                            val lowerInput = rawInput.lowercase()

                            val exact = allResults.filter { it.artist.equals(rawInput, true) }
                            val contains = allResults.filter {
                                !exact.contains(it) && it.artist.lowercase().contains(lowerInput)
                            }
                            val others = allResults - exact - contains

                            // 우선순위: 정확히 일치 > 포함 > 나머지
                            exact + contains + others.sortedBy { it.artist }
                        }

                        searchAttribute.contains("songTerm", ignoreCase = true) -> {
                            val exact = allResults.filter { it.title.equals(rawInput, true) }
                            val word = allResults.filter {
                                !exact.contains(it) && it.title.lowercase().split(Regex("[\\s\\-]"))
                                    .contains(rawInput.lowercase())
                            }
                            val partial = allResults.filter {
                                !exact.contains(it) && !word.contains(it) && it.title.lowercase()
                                    .contains(rawInput.lowercase())
                            }
                            val others = allResults - exact - word - partial
                            exact + word + partial + others.sortedBy { it.artist }
                        }

                        searchAttribute.contains("albumTerm", ignoreCase = true) -> {
                            val exact = allResults.filter { it.album.equals(rawInput, true) }
                            val word = allResults.filter {
                                !exact.contains(it) && it.album.lowercase().split(Regex("[\\s\\-]"))
                                    .contains(rawInput.lowercase())
                            }
                            val partial = allResults.filter {
                                !exact.contains(it) && !word.contains(it) && it.album.lowercase()
                                    .contains(rawInput.lowercase())
                            }
                            val others = allResults - exact - word - partial
                            exact + word + partial + others.sortedBy { it.title }
                        }

                        else -> {
                            // 전체 검색일 때도 title 기준으로 정렬 적용
                            val exact = allResults.filter { it.title.equals(rawInput, true) }
                            val word = allResults.filter {
                                !exact.contains(it) && it.title.lowercase().split(Regex("[\\s\\-\\[\\]]"))
                                    .contains(rawInput.lowercase())
                            }
                            val partial = allResults.filter {
                                !exact.contains(it) && !word.contains(it) && it.title.lowercase()
                                    .contains(rawInput.lowercase())
                            }
                            val others = allResults - exact - word - partial
                            exact + word + partial + others.sortedBy { it.artist }
                        }


                    }


                    // 여기서부터 UI 스레드에서 처리
                    activity?.runOnUiThread {
                        albumList.clear()
                        albumList.addAll(sortedResults)
                        adapter.notifyDataSetChanged()

                        if (albumList.isEmpty()) {
                            Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }


                } catch (e: Exception) {
                    Log.e("iTunesAPI", "오류 발생: $e")
                }
            }.start()
        }


        // 노래 추가 버튼 팝업
        addButton.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.add_song, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_add -> {
                        val mainActivity = requireActivity() as MainActivity
                        val playlistTitles = mainActivity.playLists.map { it.title }.toTypedArray()

                        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        builder.setTitle("플레이리스트 선택")
                        builder.setItems(playlistTitles) { _, which ->
                            val selectedPlaylist = mainActivity.playLists[which]
                            adapter.selectedAlbum?.let { selectedPlaylist.songs.add(it) }
                            Toast.makeText(
                                requireContext(),
                                "'${selectedPlaylist.title}'에 추가되었습니다!",
                                Toast.LENGTH_SHORT
                            ).show()
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

    private fun highlightSelectedTab(selected: TextView) {
        val tabs = listOf<TextView>(
            requireView().findViewById(R.id.tabAll),
            requireView().findViewById(R.id.tabSong),
            requireView().findViewById(R.id.tabArtist),
            requireView().findViewById(R.id.tabAlbum)
        )
        tabs.forEach { it.setBackgroundColor(android.graphics.Color.TRANSPARENT) }
        selected.setBackgroundColor(android.graphics.Color.DKGRAY)
    }

    private fun makeMap(urls: String): Map<String, Album> {
        val madeMap = mutableMapOf<String, Album>()

        try {
            val url = URL(urls)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val stream = conn.inputStream
                val result = stream.bufferedReader().use { it.readText() }
                stream.close()

                val jsonResponse = JSONObject(result)
                val jsonArray = jsonResponse.getJSONArray("results")

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val id = item.optString("trackId")  // collectionId → trackId


                    val title = item.optString("trackName")
                    val artist = item.optString("artistName")
                    val album = item.optString("collectionName")
                    val albumArt = item.optString("artworkUrl100")
                    val songUrl = item.optString("previewUrl")

                    madeMap[id] = Album(title, artist, album, albumArt, songUrl)
                }
            } else {
                Log.e("iTunesAPI", "응답 코드 오류: ${conn.responseCode}")
            }

        } catch (e: Exception) {
            Log.e("iTunesAPI", "오류 발생: $e")
        }

        return madeMap
    }
}