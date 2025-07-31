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

class OtherUserProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var followButton: Button
    private lateinit var followersText: TextView
    private lateinit var followingText: TextView
    private lateinit var postCountText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var archiveTabButton: Button
    private lateinit var diaryTabButton: Button

    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var archiveRecyclerView: RecyclerView

    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var playlistAdapter: PlaylistAdapter

    private val diaryList = mutableListOf<DiaryItem>()
    private val playlistList = mutableListOf<Playlist>()

    private lateinit var viewedUserId: String
    private lateinit var currentUserId: String

    private val db = FirebaseFirestore.getInstance()

    companion object {
        fun newInstance(username: String): OtherUserProfileFragment {
            val fragment = OtherUserProfileFragment()
            val bundle = Bundle()
            bundle.putString("username", username)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_other_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewedUserId = arguments?.getString("username") ?: return
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // UI 바인딩
        profileImage = view.findViewById(R.id.profileImage)
        usernameText = view.findViewById(R.id.usernameText)
        followButton = view.findViewById(R.id.followButton)
        followersText = view.findViewById(R.id.followersText)
        followingText = view.findViewById(R.id.followingText)
        postCountText = view.findViewById(R.id.postCountText)
        backButton = view.findViewById(R.id.backButton)
        archiveTabButton = view.findViewById(R.id.archiveTabButton)
        diaryTabButton = view.findViewById(R.id.diaryTabButton)

        diaryRecyclerView = view.findViewById(R.id.diaryRecyclerView)
        archiveRecyclerView = view.findViewById(R.id.archiveRecyclerView)

        diaryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        archiveRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 설정
        diaryAdapter = DiaryAdapter(diaryList, onItemClick = { diaryItem ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryDetailFragment(diaryItem))
                .addToBackStack(null)
                .commit()
        }, onItemLongClick = {}, isProfile = true)

        playlistAdapter = PlaylistAdapter(playlistList,
            onItemClick = { playlist ->
                val bundle = Bundle().apply {
                    putSerializable("playlist", playlist)
                    putString("origin", "archive")
                    putString("viewedUserId", viewedUserId)
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ViewPlaylistFragment().apply { arguments = bundle })
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = {
                Toast.makeText(requireContext(), "길게 누름: ${it.title}", Toast.LENGTH_SHORT).show()
            })

        diaryRecyclerView.adapter = diaryAdapter
        archiveRecyclerView.adapter = playlistAdapter

        // 버튼 이벤트
        backButton.setOnClickListener { parentFragmentManager.popBackStack() }
        followButton.setOnClickListener { toggleFollow() }
        followersText.setOnClickListener { showUserList("followers") }
        followingText.setOnClickListener { showUserList("following") }

        diaryTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.VISIBLE
            archiveRecyclerView.visibility = View.GONE
        }

        archiveTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.GONE
            archiveRecyclerView.visibility = View.VISIBLE
            loadPlaylists()
        }

        // 데이터 로드
        loadUserInfo()
        loadDiaryList()
        checkFollowState()
        fetchViewedUserFollowCounts()

        // 탭 자동 선택
        val selectedTab = arguments?.getString("selectedTab")
        if (selectedTab == "archive") {
            archiveTabButton.performClick()
        } else {
            diaryTabButton.performClick()
        }
    }

    private fun loadUserInfo() {
        db.collection("users").whereEqualTo("username", viewedUserId).get()
            .addOnSuccessListener { result ->
                result.firstOrNull()?.let { doc ->
                    usernameText.text = doc.getString("username") ?: ""
                    doc.getString("profileImageUrl")?.let { imageUrl ->
                        Glide.with(this).load(imageUrl).circleCrop().into(profileImage)
                    }
                }
            }
    }

    private fun loadDiaryList() {
        db.collection("users").whereEqualTo("username", viewedUserId).get()
            .addOnSuccessListener { result ->
                result.firstOrNull()?.let { userDoc ->
                    val userDocId = userDoc.id
                    db.collection("users").document(userDocId)
                        .collection("diaries").get()
                        .addOnSuccessListener { diaryResult ->
                            diaryList.clear()
                            diaryList.addAll(
                                diaryResult.mapNotNull { it.toObject(DiaryItem::class.java) }
                                    .filter { it.isPublic == true }
                            )
                            postCountText.text = "게시물 ${diaryList.size}"
                            diaryAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun loadPlaylists() {
        db.collection("users").whereEqualTo("username", viewedUserId).get()
            .addOnSuccessListener { result ->
                result.firstOrNull()?.let { userDoc ->
                    val userDocId = userDoc.id
                    db.collection("users").document(userDocId)
                        .collection("playlists").get()
                        .addOnSuccessListener { playlistResult ->
                            playlistList.clear()
                            playlistList.addAll(playlistResult.mapNotNull { it.toObject(Playlist::class.java) })
                            playlistAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun checkFollowState() {
        db.collection("users").whereEqualTo("username", viewedUserId).get()
            .addOnSuccessListener { result ->
                result.firstOrNull()?.let { targetUser ->
                    val targetUid = targetUser.id
                    db.collection("users").document(currentUserId)
                        .collection("following").document(targetUid)
                        .get()
                        .addOnSuccessListener {
                            followButton.text = if (it.exists()) "팔로잉" else "팔로우"
                        }
                }
            }
    }

    private fun toggleFollow() {
        db.collection("users").whereEqualTo("username", viewedUserId).get()
            .addOnSuccessListener { result ->
                result.firstOrNull()?.let { targetUser ->
                    val targetUid = targetUser.id
                    val followingRef = db.collection("users").document(currentUserId)
                        .collection("following").document(targetUid)
                    val followerRef = db.collection("users").document(targetUid)
                        .collection("followers").document(currentUserId)

                    if (followButton.text == "팔로우") {
                        db.collection("users").document(currentUserId).get()
                            .addOnSuccessListener { currentDoc ->
                                val currentUsername = currentDoc.getString("username") ?: ""
                                val currentEmail = currentDoc.getString("email") ?: ""
                                val currentProfileImageUrl = currentDoc.getString("profileImageUrl")

                                followerRef.set(
                                    mapOf(
                                        "username" to currentUsername,
                                        "email" to currentEmail,
                                        "profileImageUrl" to currentProfileImageUrl
                                    )
                                )

                                val targetUsername = targetUser.getString("username") ?: ""
                                val targetEmail = targetUser.getString("email") ?: ""
                                val targetProfileImageUrl = targetUser.getString("profileImageUrl")

                                followingRef.set(
                                    mapOf(
                                        "username" to targetUsername,
                                        "email" to targetEmail,
                                        "profileImageUrl" to targetProfileImageUrl
                                    )
                                )

                                followButton.text = "팔로잉"
                                fetchViewedUserFollowCounts()
                            }
                    } else {
                        followingRef.delete()
                        followerRef.delete()
                        followButton.text = "팔로우"
                        fetchViewedUserFollowCounts()
                    }
                }
            }
    }

    private fun fetchViewedUserFollowCounts() {
        db.collection("users").whereEqualTo("username", viewedUserId).get()
            .addOnSuccessListener { result ->
                result.firstOrNull()?.let { userDoc ->
                    val userId = userDoc.id
                    val userRef = db.collection("users").document(userId)
                    userRef.collection("followers").get()
                        .addOnSuccessListener { snapshot ->
                            followersText.text = "팔로워 ${snapshot.size()}"
                        }
                    userRef.collection("following").get()
                        .addOnSuccessListener { snapshot ->
                            followingText.text = "팔로잉 ${snapshot.size()}"
                        }
                }
            }
    }

    private fun showUserList(type: String) {
        db.collection("users").whereEqualTo("username", viewedUserId).get()
            .addOnSuccessListener { result ->
                result.firstOrNull()?.let { targetUser ->
                    val targetUid = targetUser.id
                    val fragment = when (type) {
                        "followers" -> FollowersListFragment().apply {
                            arguments = Bundle().apply { putString("userId", targetUid) }
                        }
                        "following" -> FollowingListFragment().apply {
                            arguments = Bundle().apply { putString("userId", targetUid) }
                        }
                        else -> return@addOnSuccessListener
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
    }
}
