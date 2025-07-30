package com.example.itunesapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FollowersListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FollowUserAdapter
    private val followersList = mutableListOf<UserItem>()

    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var currentUsername: String? = null  // 🔹 현재 로그인한 유저 이름

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_followers_list, container, false)

        recyclerView = view.findViewById(R.id.followRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        userId = arguments?.getString("userId")

        // 🔸 로그인한 유저의 username을 가져온 뒤 Adapter 설정
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    currentUsername = document.getString("username")

                    // 🔸 어댑터 설정
                    adapter = FollowUserAdapter(
                        followersList,
                        currentUsername,
                        onNavigateToProfile = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, ProfileFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onNavigateToOtherUser = { username ->
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, OtherUserProfileFragment.newInstance(username))
                                .addToBackStack(null)
                                .commit()
                        }
                    )

                    recyclerView.adapter = adapter

                    // 🔸 username을 가져온 뒤에 팔로워 불러오기
                    loadFollowers()
                }
        }

        return view
    }

    private fun loadFollowers() {
        val targetUserId = userId ?: return

        db.collection("users").document(targetUserId).collection("followers")
            .get()
            .addOnSuccessListener { documents ->
                followersList.clear()

                val userIds = documents.map { it.id }

                for (userId in userIds) {
                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val user = userDoc.toObject(UserItem::class.java)
                            if (user != null) {
                                followersList.add(user)
                                adapter.notifyItemInserted(followersList.size - 1)
                            }
                        }
                }
            }
    }
}
