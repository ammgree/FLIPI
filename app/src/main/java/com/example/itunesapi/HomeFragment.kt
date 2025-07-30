package com.example.itunesapi

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


// 데이터 모델 클래스: 스토리 아이템을 나타냄
import android.os.Parcelable
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Parcelize
data class StoryItem(
    val title: String = "",
    val artist: String = "",
    val albumArtUrl: String = ""
) : Parcelable


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var storyRecyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val storyList = mutableListOf<StoryItem>()
    private lateinit var storyAdapter: StoryAdapter

    // 1. fragment_home.xml을 inflate해서 view 반환
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    // 2. UI가 완전히 그려진 후 뷰 작업 수행
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //$mood $usernme 님을 위한 노래 textveiw & recyclerView
        val mood = arguments?.getString("mood") ?: ""
        val username = arguments?.getString("username") ?: ""
        val recyclerView = view.findViewById<RecyclerView>(R.id.rcmdSongRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val rcmdMent = view.findViewById<TextView>(R.id.rcmdMent)
        rcmdMent.text = "$mood $username 님을 위한 \n 오늘의 노래추천 🎵"

        Thread {
            db.collection("songs")
                .whereEqualTo("mood", mood)
                .get()
                .addOnSuccessListener { documents ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val searchKeywords = documents
                            .mapNotNull { doc ->
                                val title = doc.getString("title")
                                val artist = doc.getString("artist")
                                if (!title.isNullOrBlank() && !artist.isNullOrBlank()) {
                                    "$title $artist"
                                } else null
                            }
                            .shuffled()
                            .take(5)
                        val albumList = mutableListOf<Album>()
                        searchKeywords.forEach { keyword ->
                            val term = URLEncoder.encode(keyword, "UTF-8")
                            val url = "https://itunes.apple.com/search?media=musicTrack&entity=song&country=kr&term=$term"
                            val songMap = makeMap(url)
                            songMap.values.firstOrNull()?.let { albumList.add(it) }
                        }
                        withContext(Dispatchers.Main) {
                            val adapter = AlbumAdapter(
                                albumList,
                                onItemClick = { album ->
                                    MusicPlayerManager.play(album.songUrl)
                                }
                            )
                            recyclerView.adapter = adapter


                        }
                    }
                }
        }.start()

        // 1. 리사이클러뷰 설정: 스토리 목록 보여줌
        storyRecyclerView = view.findViewById(R.id.storyRecyclerView)
        storyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        storyAdapter = StoryAdapter(storyList) { storyItem ->
            val detailFragment = StoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("story", storyItem)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        storyRecyclerView.adapter = storyAdapter

        db.collection("stories")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                storyList.clear()
                for (doc in snapshot.documents) {
                    val item = doc.toObject(StoryItem::class.java)
                    if (item != null) storyList.add(item)
                }
                storyAdapter.notifyDataSetChanged()
            }

        // 2. + 스토리 추가 버튼 클릭 시, StoryAddFragment 다이얼로그 띄움
        val addStoryButton = view.findViewById<Button>(R.id.addStoryButton)
        addStoryButton.setOnClickListener {
            val dialog = StoryAddFragment()
            dialog.show(parentFragmentManager, "AddStoryDialog")
        }

        // 3. 파이어스토어에서 프로필 이미지 불러오기
        val profileImageView = view.findViewById<ImageView>(R.id.profileImageView)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        //val db = FirebaseFirestore.getInstance()

        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                val imageUrl = document.getString("profileImageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(profileImageView)
                }
            }
        }

        // 4. 프로필 이미지 클릭 → 프로필 프래그먼트로 이동
        profileImageView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // 5. 하단 네비게이션바 보이도록 설정
        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.VISIBLE
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

