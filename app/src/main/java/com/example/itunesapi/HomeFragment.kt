package com.example.itunesapi

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class StoryItem(
    val title: String,
    val artist: String,
    val albumArtUrl: String
)

class HomeFragment : Fragment() {

    private lateinit var storyRecyclerView: RecyclerView

    // 임시 데이터 (예시)
    private val storyList = mutableListOf(
        StoryItem("노래1", "아티스트1", "https://url-to-album-art1.jpg"),
        StoryItem("노래2", "아티스트2", "https://url-to-album-art2.jpg"),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 리사이클러뷰 초기화
        storyRecyclerView = view.findViewById(R.id.storyRecyclerView)
        storyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        storyRecyclerView.adapter = StoryAdapter(storyList) { storyItem ->
            val detailFragment = StoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("songTitle", storyItem.title)
                    putString("artistName", storyItem.artist)
                    putString("albumArtUrl", storyItem.albumArtUrl)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        // 2. 프로필 이미지 불러오기
        val profileImageView = view.findViewById<ImageView>(R.id.profileImageView)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

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

        profileImageView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.VISIBLE

    }

}
