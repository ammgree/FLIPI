package com.example.itunesapi

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itunesapi.model.YoutubeVideoInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.net.URLEncoder

class StoreFragment : Fragment() {

    private lateinit var storeRecyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter
    private val client = OkHttpClient()
    private var origin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        origin = arguments?.getString("origin")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)
        val mainActivity = requireActivity() as MainActivity

        // 뷰 바인딩
        val addButton = view.findViewById<ImageButton>(R.id.addPlaylist)
        val youtubeButton = view.findViewById<ImageButton>(R.id.mediaLinkButton)
        storeRecyclerView = view.findViewById(R.id.storeView)

        storeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PlaylistAdapter(mainActivity.playLists,
            onItemClick = { selectedPlaylist ->
                Log.d("origin","origin: ${origin}")
                if(origin == "FocusTimer"){
                    val playlistFragment = ViewPlaylistFragment().apply {
                        arguments = Bundle().apply {
                            putString("origin", "FocusTimer")
                            putSerializable("playlist", selectedPlaylist)
                            putString("subject", arguments?.getString("subject"))
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, playlistFragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    val bundle = Bundle().apply {
                        putSerializable("playlist", selectedPlaylist)
                        putString("origin", "store")
                    }
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ViewPlaylistFragment().apply {
                            arguments = bundle
                        })
                        .addToBackStack(null)
                        .commit()
                }
            },
            onItemLongClick = { playlist ->
                AlertDialog.Builder(requireContext())
                    .setTitle("플레이리스트 삭제")
                    .setMessage("「${playlist.title}」을 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        val user = FirebaseAuth.getInstance().currentUser
                        val uid = user?.uid ?: return@setPositiveButton
                        deletePlaylist(uid, playlist.title) {
                            mainActivity.playLists.remove(playlist)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        )

        storeRecyclerView.adapter = adapter

        addButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("새 플레이리스트 생성")
            val input = EditText(requireContext()).apply { hint = "플레이리스트 제목 입력" }
            builder.setView(input)

            builder.setPositiveButton("추가") { dialog, _ ->
                val title = input.text.toString().ifBlank { "이름 없는 플레이리스트" }
                val imageUrl = "https://picsum.photos/300/200?random=${System.currentTimeMillis()}"
                val newPlaylist = Playlist(title, imageUrl)

                mainActivity.playLists.add(0, newPlaylist)
                adapter.notifyItemInserted(0)
                storeRecyclerView.scrollToPosition(0)

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    Toast.makeText(requireContext(), "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val playlistMap = hashMapOf(
                    "title" to title,
                    "picture" to imageUrl,
                    "songs" to emptyList<Map<String, Any>>()
                )

                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("playlists")
                    .add(playlistMap)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "저장 성공!", Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            }

            builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }

            builder.show()
        }

        // 유튜브 버튼 눌렀을 때 동작 (검색 화면으로 이동할 수 있음)
        youtubeButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("유튜브 플레이리스트 URL 입력")

            val input = EditText(requireContext())
            input.hint = "예: https://www.youtube.com/playlist?list=..."
            builder.setView(input)

            builder.setPositiveButton("확인") { dialog, _ ->
                val url = input.text.toString()
                val playlistId = extractPlaylistId(url)

                Log.d("playlist_debug", "추출된 ID: $playlistId")

                if (playlistId != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val videos = fetchYoutubePlaylistItems(playlistId)
                        if (videos.isEmpty()) {
                            Toast.makeText(requireContext(), "불러올 영상이 없습니다", Toast.LENGTH_SHORT).show()
                        } else {
                            val albums = videos.mapNotNull {
                                searchItunesSong(it.title)
                            }
                            if (albums.isNotEmpty()) {
                                // 여기가 변경된 부분!
                                showPlaylistTitleDialog(albums)
                            } else {
                                Toast.makeText(requireContext(), "iTunes 검색 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "올바르지 않은 URL입니다", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }

            builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
            builder.show()
        }


        return view
    }

    // 유튜브 플레이리스트 URL에서 ID 추출
    private fun extractPlaylistId(url: String): String? {
        val uri = Uri.parse(url)
        return uri.getQueryParameter("list")
    }

    // 유튜브 플레이리스트 영상 정보 가져오기 (최대 30개)
    suspend fun fetchYoutubePlaylistItems(playlistId: String): List<YoutubeVideoInfo> {

        KeyManager.init(requireContext())
        val apiKey = KeyManager.get("YOUTUBE_API_KEY")


        val url =
            "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=$playlistId&maxResults=30&key=$apiKey"
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            val result = mutableListOf<YoutubeVideoInfo>()
            try {
                val response = client.newCall(request).execute()
                Log.d("playlist_debug", "응답 코드: ${response.code}")
                val bodyString = response.body?.string()
                Log.d("playlist_debug", "응답 본문: $bodyString")

                if (response.isSuccessful && bodyString != null) {
                    val json = JSONObject(bodyString)
                    val items = json.getJSONArray("items")

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i).getJSONObject("snippet")
                        val title = item.getString("title")
                        val videoId = item.getJSONObject("resourceId").getString("videoId")
                        val channelTitle = item.getString("channelTitle")
                        val thumbnailUrl = item.getJSONObject("thumbnails")
                            .getJSONObject("default").getString("url")
                        result.add(YoutubeVideoInfo(title, videoId, channelTitle, thumbnailUrl))
                    }
                }
            } catch (e: Exception) {
                Log.e("playlist_debug", "예외 발생: ${e.message}", e)
            }

            result
        }
    }

    fun parseTitleAndArtist(rawTitle: String): Pair<String, String> {
        // 1. 불필요한 키워드 제거 (괄호는 남겨둠)
        val cleaned = rawTitle
            .replace(Regex("Official MV|M/V|Part\\.\\d+", RegexOption.IGNORE_CASE), "")
            .trim()

        // 2. 괄호 기호만 제거하고 안의 내용은 보존
        val noParens = cleaned.replace(Regex("[()]"), " ")  // 괄호만 제거
            .replace(Regex("\\s+"), " ") // 중복 공백 정리
            .trim()

        // 3. 큰따옴표 기반 곡명 추출
        val quoteRegex = Regex("[\"“”](.*?)[\"“”]")
        val quoteMatch = quoteRegex.find(noParens)
        if (quoteMatch != null) {
            val title = quoteMatch.groupValues[1].trim()
            val artist = noParens.substring(0, quoteMatch.range.first).trim()
            return artist to title
        }

        // 4. 작은 따옴표 기반 곡명 추출
        val apostropheRegex = Regex("['‘](.*?)['’]")
        val apostropheMatch = apostropheRegex.find(noParens)
        if (apostropheMatch != null) {
            val title = apostropheMatch.groupValues[1].trim()
            val artist = noParens.substring(0, apostropheMatch.range.first).trim()
            return artist to title
        }

        // 5. fallback: 제목만 전체 사용, 아티스트는 빈 값
        return "" to noParens
    }



    // iTunes에서 노래 검색 후 Album 객체 반환
    private suspend fun searchItunesSong(rawTitle: String): Album? {
        val (artist, title) = parseTitleAndArtist(rawTitle)
        val query = URLEncoder.encode("$title $artist", "UTF-8")
        val url = "https://itunes.apple.com/search?term=$query&media=music&limit=10"
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null
                val bodyString = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyString)
                val results = json.getJSONArray("results")
                if (results.length() == 0) return@withContext null

                val resultList = mutableListOf<Album>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    resultList.add(
                        Album(
                            title = item.getString("trackName"),
                            artist = item.getString("artistName"),
                            album = item.optString("collectionName", "Unknown Album"),
                            imageUrl = item.getString("artworkUrl100"),
                            songUrl = item.getString("previewUrl")
                        )
                    )
                }

                // 정렬: 정확 일치 > 단어 포함 > 부분 포함 > 나머지
                val exact = resultList.filter { it.title.equals(title, true) }
                val word = resultList.filter {
                    !exact.contains(it) && it.title.lowercase().split(Regex("[\\s\\-\\[\\]]"))
                        .contains(title.lowercase())
                }
                val partial = resultList.filter {
                    !exact.contains(it) && !word.contains(it) && it.title.lowercase()
                        .contains(title.lowercase())
                }
                val others = resultList - exact - word - partial
                val sorted = exact + word + partial + others.sortedBy { it.artist }

                sorted.firstOrNull()  // 가장 유사한 첫 번째 결과 반환

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }



    // 유튜브 영상 리스트를 iTunes 앨범 리스트로 매칭
    private suspend fun matchYoutubeToItunes(youtubeList: List<YoutubeVideoInfo>): List<Album> {
        val albumList = mutableListOf<Album>()
        for (video in youtubeList) {
            val album = searchItunesSong(video.title)
            if (album != null) {
                albumList.add(album)
                Log.d("itunes_match", "🎶 매칭 성공: ${album.title} - ${album.artist}")
            } else {
                Log.w("itunes_match", "❌ 매칭 실패: ${video.title} - ${video.channelTitle}")
            }
        }
        return albumList
    }

    // 사용자에게 플레이리스트 이름을 입력받는 다이얼로그
    private fun showPlaylistTitleDialog(albumList: List<Album>) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("플레이리스트 이름 입력")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("저장") { dialog, _ ->
            val playlistTitle = input.text.toString().trim()
            if (playlistTitle.isNotEmpty()) {
                savePlaylistToFirestore(playlistTitle, albumList)
            } else {
                Toast.makeText(requireContext(), "플레이리스트 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }

        builder.show()
    }




    fun savePlaylistToFirestore(title: String, newSongs: List<Album>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userDoc = FirebaseFirestore.getInstance().collection("users").document(uid)
        val playlistsRef = userDoc.collection("playlists")

        playlistsRef.whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { documents ->
                // 중복 제거
                val distinctNewSongs = newSongs.distinctBy { it.title to it.artist }

                if (!documents.isEmpty) {
                    // 기존 데이터와 합치기
                    val doc = documents.documents[0]
                    val existingSongs = (doc["songs"] as? List<Map<String, Any>>)?.map {
                        Album(
                            it["title"] as String,
                            it["artist"] as String,
                            it["album"] as String,
                            it["imageUrl"] as String,
                            it["songUrl"] as String
                        )
                    } ?: emptyList()

                    val uniqueNewSongs = distinctNewSongs.filter { newSong ->
                        existingSongs.none { it.title == newSong.title && it.artist == newSong.artist }
                    }

                    val updatedSongs = existingSongs + uniqueNewSongs

                    // 표지: iTunes 기준 첫 번째 노래 이미지
                    val coverImageUrl = updatedSongs.firstOrNull()?.imageUrl ?: ""

                    playlistsRef.document(doc.id)
                        .update("songs", updatedSongs.map { it.toMap() })
                        .addOnSuccessListener {
                            Toast.makeText(context, "플레이리스트에 노래가 추가되었습니다!", Toast.LENGTH_SHORT).show()

                            val mainActivity = requireActivity() as MainActivity
                            val newPlaylist = Playlist(title, coverImageUrl, updatedSongs.toMutableList())
                            mainActivity.playLists.removeAll { it.title == title }
                            mainActivity.playLists.add(0, newPlaylist)
                            adapter.notifyDataSetChanged()
                            storeRecyclerView.scrollToPosition(0)
                        }

                } else {
                    //표지: iTunes 기준 첫 번째 노래 이미지
                    val coverImageUrl = distinctNewSongs.firstOrNull()?.imageUrl ?: ""

                    val playlistMap = hashMapOf(
                        "title" to title,
                        "songs" to distinctNewSongs.map { it.toMap() },
                        "picture" to coverImageUrl // ← Firestore에도 저장
                    )

                    playlistsRef.add(playlistMap).addOnSuccessListener {
                        Toast.makeText(context, "새 플레이리스트가 저장되었습니다!", Toast.LENGTH_SHORT).show()

                        val mainActivity = requireActivity() as MainActivity
                        val newPlaylist = Playlist(title, coverImageUrl, distinctNewSongs.toMutableList())
                        mainActivity.playLists.add(0, newPlaylist)
                        adapter.notifyItemInserted(0)
                        storeRecyclerView.scrollToPosition(0)
                    }
                }
            }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val mainActivity = requireActivity() as MainActivity
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        mainActivity.playLists.clear()

        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("playlists")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val title = doc.getString("title") ?: ""
                    val picture = doc.getString("picture") ?: ""
                    val songsData = doc.get("songs") as? List<Map<String, Any>> ?: emptyList()

                    val songs = songsData.map {
                        Album(
                            title = it["title"] as String,
                            artist = it["artist"] as String,
                            album = it["album"] as String,
                            imageUrl = it["imageUrl"] as String,
                            songUrl = it["songUrl"] as String
                        )
                    }.toMutableList()

                    mainActivity.playLists.add(Playlist(title, picture, songs))
                }
                adapter.notifyDataSetChanged()
            }
    }

    fun deletePlaylist(userId: String, playlistTitle: String, onComplete: () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("playlists")
            .whereEqualTo("title", playlistTitle)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    db.collection("users").document(userId)
                        .collection("playlists")
                        .document(doc.id)
                        .delete()
                }
                Toast.makeText(requireContext(), "플레이리스트가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onSongSelected(songUrl: String, songTitle: String) {
        // 결과 전달
        parentFragmentManager.setFragmentResult(
            "songSelected",
            Bundle().apply {
                putString("musicUrl", songUrl)
                putString("musicTitle", songTitle)
            }
        )

        if (origin == "FocusTimer") {
            // FocusTimerFragment에서 왔을 때만 뒤로 가기
            parentFragmentManager.popBackStack()
        } else {
            // 그 외에는 아무것도 하지 않음
            Log.d("StoreFragment", "Not from FocusTimer, so no popBackStack")
        }
    }
}
