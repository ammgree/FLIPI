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

        diaryAdapter = DiaryAdapter(diaryList, onItemClick = {}, onItemLongClick = {}, isProfile = true)
        diaryRecyclerView.adapter = diaryAdapter

        loadUserInfo()
        loadDiaryList()
        checkFollowState()

        followButton.setOnClickListener {
            toggleFollow()
        }
    }

    private fun loadUserInfo() {
        db.collection("users").document(viewedUserId).get()
            .addOnSuccessListener { doc ->
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

    private fun loadDiaryList() {
        db.collection("users").document(viewedUserId).collection("diaries")
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
    }

    private fun checkFollowState() {
        db.collection("users").document(currentUserId)
            .collection("following").document(viewedUserId)
            .get()
            .addOnSuccessListener {
                followButton.text = if (it.exists()) "팔로잉" else "팔로우"
            }
    }

    private fun toggleFollow() {
        val followingRef = db.collection("users").document(currentUserId)
            .collection("following").document(viewedUserId)
        val followerRef = db.collection("users").document(viewedUserId)
            .collection("followers").document(currentUserId)

        if (followButton.text == "팔로우") {
            // 팔로우하기
            followingRef.set(mapOf("username" to viewedUserId))
            followerRef.set(mapOf("username" to currentUserId))
            followButton.text = "팔로잉"
        } else {
            // 팔로우 해제
            followingRef.delete()
            followerRef.delete()
            followButton.text = "팔로우"
        }
    }
}
