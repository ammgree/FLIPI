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

    private lateinit var backButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var diaryTabButton: Button
    private lateinit var archiveTabButton: Button
    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var archiveRecyclerView: RecyclerView
    private lateinit var followingText : TextView
    private lateinit var followersText : TextView
    private lateinit var postCountText : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔗 UI 연결
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

        diaryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        archiveRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        // 🔸 다이어리 탭용 어댑터 및 데이터
        val diaryList = mutableListOf<DiaryItem>()
        val diaryAdapter = DiaryAdapter(
            diaryList,
            onItemClick = { diaryItem ->
                val fragment = DiaryDetailFragment(diaryItem)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = { diaryItem ->
                Toast.makeText(requireContext(), "길게 누름: ${diaryItem.title}", Toast.LENGTH_SHORT).show()
            },
            isProfile = true
        )
        diaryRecyclerView.adapter = diaryAdapter

        // 🔹 Firestore에서 사용자 정보
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Unknown"
                    val profileImageUrl = document.getString("profileImageUrl")

                    usernameText.text = username
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .circleCrop()
                            .into(profileImage)
                    }
                }

            // 🔹 Firestore에서 일기 목록 불러오기
            db.collection("users").document(uid).collection("diaries")
                .get()
                .addOnSuccessListener { result ->
                    diaryList.clear()
                    for (document in result) {
                        val diary = document.toObject(DiaryItem::class.java)
                        diaryList.add(diary)
                    }

                    postCountText.text = "게시물 ${diaryList.size}"
                    diaryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "일기를 불러오지 못했어요", Toast.LENGTH_SHORT).show()
                }

            // 🔹 팔로워 수
            db.collection("users").document(uid).collection("followers")
                .get()
                .addOnSuccessListener { result ->
                    followersText.text = "팔로워 ${result.size()}"
                }

            // 🔹 팔로잉 수
            db.collection("users").document(uid).collection("following")
                .get()
                .addOnSuccessListener { result ->
                    followingText.text = "팔로잉 ${result.size()}"
                }
        }

        // 🔙 뒤로가기: 홈
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        // 🔍 검색 화면으로 이동
        searchButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🔄 팔로워/팔로잉 목록으로 이동
        followersText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FollowersListFragment())
                .addToBackStack(null)
                .commit()
        }

        followingText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FollowingListFragment())
                .addToBackStack(null)
                .commit()
        }

        // 하단 바 숨기기
        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.GONE

        // ✅ 일기 탭 클릭 시
        diaryTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.VISIBLE
            archiveRecyclerView.visibility = View.GONE
        }

        // ✅ 보관함 탭 클릭 시 (플레이리스트 목록)
        archiveTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.GONE
            archiveRecyclerView.visibility = View.VISIBLE

            val archiveList = mutableListOf<Playlist>()
            val archiveAdapter = PlaylistAdapter(
                archiveList,
                onItemClick = { playlist ->
                    val bundle = Bundle().apply {
                        putSerializable("playlist", playlist)
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ViewPlaylistFragment().apply {
                            arguments = bundle
                        })
                        .addToBackStack(null)
                        .commit()
                },
                onItemLongClick = { playlist ->
                    Toast.makeText(requireContext(), "길게 누름: ${playlist.title}", Toast.LENGTH_SHORT).show()
                }
            )

            archiveRecyclerView.adapter = archiveAdapter

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("playlists")
                .get()
                .addOnSuccessListener { result ->
                    archiveList.clear()
                    for (document in result) {
                        val title = document.getString("title") ?: ""
                        val picture = document.getString("picture") ?: ""
                        val playlist = Playlist(title, picture)
                        archiveList.add(playlist)
                    }
                    archiveAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "보관함 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
