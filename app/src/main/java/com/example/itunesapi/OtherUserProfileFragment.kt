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
    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var followersText: TextView
    private lateinit var followingText: TextView
    private lateinit var postCountText: TextView

    private lateinit var diaryAdapter: DiaryAdapter
    private val diaryList = mutableListOf<DiaryItem>()

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
        diaryRecyclerView = view.findViewById(R.id.diaryRecyclerView)
        diaryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        diaryAdapter = DiaryAdapter(diaryList, onItemClick = {}, onItemLongClick = {}, isProfile = true)
        diaryRecyclerView.adapter = diaryAdapter

        loadUserInfo()
        loadDiaryList()
        checkFollowState()

        followButton.setOnClickListener {
            toggleFollow()
        }


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
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(profileImage)
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

                    db.collection("users")
                        .document(userDocId)
                        .collection("diaries")
                        .get()
                        .addOnSuccessListener { diaryResult ->
                            diaryList.clear()
                            for (document in diaryResult) {
                                val diary = document.toObject(DiaryItem::class.java)
                                // 👉 공개된 일기만 보여주기
                                if (diary.isPublic == true) {
                                    diaryList.add(diary)
                                }
                            }
                            postCountText.text = "게시물 ${diaryList.size}"
                            diaryAdapter.notifyDataSetChanged()
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
                        // 🔥 먼저 내 정보 가져오기
                        db.collection("users").document(currentUserId).get()
                            .addOnSuccessListener { currentUserDoc ->
                                val currentUsername = currentUserDoc.getString("username") ?: ""
                                val currentEmail = currentUserDoc.getString("email") ?: ""
                                val currentProfileImageUrl = currentUserDoc.getString("profileImageUrl")

                                // 🔥 상대 정보도 가져오기
                                db.collection("users").document(targetUid).get()
                                    .addOnSuccessListener { targetUserDoc ->
                                        val targetUsername = targetUserDoc.getString("username") ?: ""
                                        val targetEmail = targetUserDoc.getString("email") ?: ""
                                        val targetProfileImageUrl = targetUserDoc.getString("profileImageUrl")

                                        val followingData = mapOf(
                                            "username" to targetUsername,
                                            "email" to targetEmail,
                                            "profileImageUrl" to targetProfileImageUrl
                                        )
                                        followingRef.set(followingData)

                                        val followerData = mapOf(
                                            "username" to currentUsername,
                                            "email" to currentEmail,
                                            "profileImageUrl" to currentProfileImageUrl
                                        )
                                        followerRef.set(followerData)

                                        followButton.text = "팔로잉"
                                        updateFollowCounts(viewedUserId)
                                    }
                            }
                    } else {
                        followingRef.delete()
                        followerRef.delete()
                        followButton.text = "팔로우"
                        updateFollowCounts(viewedUserId)
                    }
                }
            }
    }


    private fun updateFollowCounts(userId: String) {
        val userRef = db.collection("users").document(userId)

        // followers 수
        userRef.collection("followers").get()
            .addOnSuccessListener { followersSnapshot ->
                val count = followersSnapshot.size()
                followersText.text = "팔로워 $count"
            }

        // following 수
        userRef.collection("following").get()
            .addOnSuccessListener { followingSnapshot ->
                val count = followingSnapshot.size()
                followingText.text = "팔로잉 $count"
            }
    }

}
