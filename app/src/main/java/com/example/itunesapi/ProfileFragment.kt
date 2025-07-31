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
    private lateinit var followingText: TextView
    private lateinit var followersText: TextView
    private lateinit var postCountText: TextView

    private val db = FirebaseFirestore.getInstance()
    private val uid: String? get() = arguments?.getString("userId") ?: FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 연결
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

        loadUserInfo()
        loadDiaries()
        loadFollowCounts()

        val username = arguments?.getString("username")
        val mood = arguments?.getString("mood")

        backButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("username", username)
                putString("mood", mood)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment().apply { arguments = bundle })
                .commit()
        }

        searchButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserSearchFragment())
                .addToBackStack(null)
                .commit()
        }

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

        // 탭 전환
        diaryTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.VISIBLE
            archiveRecyclerView.visibility = View.GONE
        }

        archiveTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.GONE
            archiveRecyclerView.visibility = View.VISIBLE
            loadPlaylists()
        }

        // 선택된 탭 유지
        when (arguments?.getString("selectedTab")) {
            "archive" -> archiveTabButton.performClick()
            else -> diaryTabButton.performClick()
        }

        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.GONE
    }

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

    private fun loadDiaries() {
        val diaryList = mutableListOf<DiaryItem>()
        val adapter = DiaryAdapter(diaryList,
            onItemClick = { item ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DiaryDetailFragment(item))
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = { item ->
                Toast.makeText(requireContext(), "길게 누름: ${item.title}", Toast.LENGTH_SHORT).show()
            },
            isProfile = true
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
