package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    // UI 컴포넌트 선언
    private lateinit var backButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var diaryTabButton: Button
    private lateinit var archiveTabButton: Button
    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var archiveRecyclerView: RecyclerView
    private lateinit var followingText: TextView
    private lateinit var followersText: TextView
    private lateinit var postCountText: TextView

    // Firebase 참조
    private val db = FirebaseFirestore.getInstance()
    private val uid: String? get() = arguments?.getString("userId") ?: FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // 프래그먼트 레이아웃 inflate
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 레이아웃에서 뷰 바인딩
        backButton = view.findViewById(R.id.backButton)
        searchButton = view.findViewById(R.id.searchButton)
        profileImage = view.findViewById(R.id.profileImage)
        usernameText = view.findViewById(R.id.usernameText)
        diaryTabButton = view.findViewById(R.id.diaryTabButton)
        archiveTabButton = view.findViewById(R.id.archiveTabButton)
        diaryRecyclerView = view.findViewById(R.id.diaryRecyclerView)
        archiveRecyclerView = view.findViewById(R.id.archiveRecyclerView)
        followersText = view.findViewById(R.id.followersText)
        followingText = view.findViewById(R.id.followingText)
        postCountText = view.findViewById(R.id.postCountText)

        // RecyclerView 레이아웃 설정
        diaryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        archiveRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 사용자 정보, 일기, 팔로우 데이터 불러오기
        loadUserInfo()
        loadDiaries()
        loadFollowCounts()

        // 이전 화면에서 전달받은 사용자 정보
        val username = arguments?.getString("username")
        val mood = arguments?.getString("mood")

        // 뒤로가기 버튼 → 홈으로 이동하며 번들 전달
        backButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("username", username)
                putString("mood", mood)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment().apply { arguments = bundle })
                .commit()
        }

        // 검색 버튼 → 사용자 검색 화면으로 이동
        searchButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 팔로워 텍스트 클릭 시 팔로워 목록 프래그먼트로 이동
        followersText.setOnClickListener {
            uid?.let {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FollowersListFragment().apply {
                        arguments = Bundle().apply { putString("userId", it) }
                    })
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 팔로잉 텍스트 클릭 시 팔로잉 목록 프래그먼트로 이동
        followingText.setOnClickListener {
            uid?.let {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FollowingListFragment().apply {
                        arguments = Bundle().apply { putString("userId", it) }
                    })
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 탭 버튼 클릭에 따른 화면 전환
        diaryTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.VISIBLE
            archiveRecyclerView.visibility = View.GONE
        }

        archiveTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.GONE
            archiveRecyclerView.visibility = View.VISIBLE
            loadPlaylists() // 보관함 로드
        }

        // selectedTab 전달 여부에 따라 탭 상태 결정
        when (arguments?.getString("selectedTab")) {
            "archive" -> archiveTabButton.performClick()
            else -> diaryTabButton.performClick()
        }

        // 하단 내비게이션 바 숨기기
        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.GONE
    }

    // 사용자 프로필 정보 (닉네임, 이미지) 불러오기
    private fun loadUserInfo() {
        uid?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { doc ->
                    usernameText.text = doc.getString("username") ?: "Unknown"
                    val profileImageUrl = doc.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profileImageUrl).circleCrop().into(profileImage)
                    }
                }
        }
    }

    // 일기 리스트 로드 및 RecyclerView에 표시
    private fun loadDiaries() {
        val diaryList = mutableListOf<DiaryItem>()
        val adapter = DiaryAdapter(
            diaryList,
            onItemClick = { item ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DiaryDetailFragment(item))
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = { item ->
                Toast.makeText(requireContext(), "길게 누름: ${item.title}", Toast.LENGTH_SHORT).show()
            },
            isProfile = true // 프로필 화면에서 사용하는 플래그
        )
        diaryRecyclerView.adapter = adapter

        uid?.let {
            db.collection("users").document(it).collection("diaries")
                .get()
                .addOnSuccessListener { result ->
                    diaryList.clear()
                    for (doc in result) {
                        diaryList.add(doc.toObject(DiaryItem::class.java))
                    }
                    postCountText.text = "게시물 ${diaryList.size}"
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "일기를 불러오지 못했어요", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 팔로워/팔로잉 수 로드
    private fun loadFollowCounts() {
        uid?.let {
            db.collection("users").document(it).collection("followers")
                .get()
                .addOnSuccessListener { followersText.text = "팔로워 ${it.size()}" }

            db.collection("users").document(it).collection("following")
                .get()
                .addOnSuccessListener { followingText.text = "팔로잉 ${it.size()}" }
        }
    }

    // 보관함(플레이리스트) 로드
    private fun loadPlaylists() {
        val archiveList = mutableListOf<Playlist>()
        val adapter = PlaylistAdapter(
            archiveList,
            onItemClick = { playlist ->
                val bundle = Bundle().apply {
                    putSerializable("playlist", playlist)
                    putString("origin", "archive")
                    putString("viewedUserId", uid)
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ViewPlaylistFragment().apply { arguments = bundle })
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = { playlist ->
                Toast.makeText(requireContext(), "길게 누름: ${playlist.title}", Toast.LENGTH_SHORT).show()
            }
        )
        archiveRecyclerView.adapter = adapter

        uid?.let {
            db.collection("users").document(it).collection("playlists")
                .get()
                .addOnSuccessListener { result ->
                    archiveList.clear()
                    for (doc in result) {
                        val title = doc.getString("title") ?: ""
                        val picture = doc.getString("picture") ?: ""
                        val songsData = doc.get("songs") as? List<Map<String, Any>> ?: emptyList()
                        val songs = songsData.map { song ->
                            Album(
                                title = song["title"] as? String ?: "",
                                artist = song["artist"] as? String ?: "",
                                album = song["album"] as? String ?: "",
                                imageUrl = song["imageUrl"] as? String ?: "",
                                songUrl = song["songUrl"] as? String ?: ""
                            )
                        }.toMutableList()
                        archiveList.add(Playlist(title, picture, songs))
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "보관함 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
