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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_other_user_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewedUserId = arguments?.getString("username") ?: return
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

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
            })

        diaryRecyclerView.adapter = diaryAdapter
        archiveRecyclerView.adapter = playlistAdapter

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        followButton.setOnClickListener { toggleFollow() }
        followersText.setOnClickListener { showUserList("followers") }
        followingText.setOnClickListener { showUserList("following") }

        diaryTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.VISIBLE
            archiveRecyclerView.visibility = View.GONE
            diaryTabButton.isEnabled = false
            archiveTabButton.isEnabled = true
        }

        archiveTabButton.setOnClickListener {
            diaryRecyclerView.visibility = View.GONE
            archiveRecyclerView.visibility = View.VISIBLE
            diaryTabButton.isEnabled = true
            archiveTabButton.isEnabled = false
        }

        loadUserInfo()
        loadDiaryList()
        loadPlaylists()
        checkFollowState()
        fetchViewedUserIdAndCount()

        diaryTabButton.performClick()
    }

    private fun loadUserInfo() {
        db.collection("users")
            .whereEqualTo("username", viewedUserId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents[0]
                    usernameText.text = doc.getString("username") ?: ""
                    val imageUrl = doc.getString("profileImageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).circleCrop().into(profileImage)
                    }
                }
            }
    }

    private fun loadDiaryList() {
        db.collection("users")
            .whereEqualTo("username", viewedUserId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val userDocId = result.documents[0].id
                    db.collection("users").document(userDocId)
                        .collection("diaries")
                        .get()
                        .addOnSuccessListener { diaryResult ->
                            diaryList.clear()
                            for (doc in diaryResult) {
                                val diary = doc.toObject(DiaryItem::class.java)
                                if (diary.isPublic == true) diaryList.add(diary)
                            }
                            postCountText.text = "게시물 ${diaryList.size}"
                            diaryAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun loadPlaylists() {
        db.collection("users")
            .whereEqualTo("username", viewedUserId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val userDocId = result.documents[0].id
                    db.collection("users").document(userDocId)
                        .collection("playlists")
                        .get()
                        .addOnSuccessListener { playlistResult ->
                            playlistList.clear()
                            for (doc in playlistResult) {
                                val playlist = doc.toObject(Playlist::class.java)
                                playlistList.add(playlist)
                            }
                            playlistAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun checkFollowState() {
        db.collection("users")
            .whereEqualTo("username", viewedUserId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val targetUid = result.documents[0].id
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
        db.collection("users")
            .whereEqualTo("username", viewedUserId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val targetUid = result.documents[0].id
                    val followingRef = db.collection("users").document(currentUserId)
                        .collection("following").document(targetUid)
                    val followerRef = db.collection("users").document(targetUid)
                        .collection("followers").document(currentUserId)

                    if (followButton.text == "팔로우") {
                        db.collection("users").document(currentUserId).get()
                            .addOnSuccessListener { currentUserDoc ->
                                val currentUsername = currentUserDoc.getString("username") ?: ""
                                val currentEmail = currentUserDoc.getString("email") ?: ""
                                val currentProfileImageUrl = currentUserDoc.getString("profileImageUrl")
                                db.collection("users").document(targetUid).get()
                                    .addOnSuccessListener { targetUserDoc ->
                                        val targetUsername = targetUserDoc.getString("username") ?: ""
                                        val targetEmail = targetUserDoc.getString("email") ?: ""
                                        val targetProfileImageUrl = targetUserDoc.getString("profileImageUrl")

                                        followingRef.set(
                                            mapOf(
                                                "username" to targetUsername,
                                                "email" to targetEmail,
                                                "profileImageUrl" to targetProfileImageUrl
                                            )
                                        )

                                        followerRef.set(
                                            mapOf(
                                                "username" to currentUsername,
                                                "email" to currentEmail,
                                                "profileImageUrl" to currentProfileImageUrl
                                            )
                                        )

                                        followButton.text = "팔로잉"
                                        updateFollowCounts(targetUid)
                                    }
                            }
                    } else {
                        followingRef.delete()
                        followerRef.delete()
                        followButton.text = "팔로우"
                        updateFollowCounts(targetUid)
                    }
                }
            }
    }

    private fun updateFollowCounts(userId: String) {
        val userRef = db.collection("users").document(userId)
        userRef.collection("followers").get()
            .addOnSuccessListener { followersSnapshot ->
                followersText.text = "팔로워 ${followersSnapshot.size()}"
            }
        userRef.collection("following").get()
            .addOnSuccessListener { followingSnapshot ->
                followingText.text = "팔로잉 ${followingSnapshot.size()}"
            }
    }

    private fun showUserList(type: String) {
        val username = viewedUserId ?: return  // null이면 그냥 리턴

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val targetUid = result.documents[0].id
                    val fragment: Fragment = when (type) {
                        "followers" -> FollowersListFragment().apply {
                            arguments = Bundle().apply {
                                putString("userId", targetUid)
                            }
                        }
                        "following" -> FollowingListFragment().apply {
                            arguments = Bundle().apply {
                                putString("userId", targetUid)
                            }
                        }
                        else -> return@addOnSuccessListener
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
            .addOnFailureListener {
                // 실패했을 때 로그나 에러 처리도 추가 가능
            }
    }



    private fun fetchViewedUserIdAndCount() {
        db.collection("users")
            .whereEqualTo("username", viewedUserId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val targetUid = result.documents[0].id
                    updateFollowCounts(targetUid)
                }
            }
    }
}
