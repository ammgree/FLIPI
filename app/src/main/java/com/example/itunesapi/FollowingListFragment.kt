package com.example.itunesapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class FollowingListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FollowUserAdapter
    private val followingList = mutableListOf<UserItem>()
    private var currentUsername: String? = null


    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_following_list, container, false)

        recyclerView = view.findViewById(R.id.followRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        userId = arguments?.getString("userId")

        adapter = FollowUserAdapter(
            followingList,
            currentUsername,  // null이 아닌 값으로 정확히 설정되어 있어야 함!
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

        loadFollowing()

        return view
    }

    private fun loadFollowing() {
        val targetUserId = userId ?: return

        db.collection("users").document(targetUserId).collection("following")
            .get()
            .addOnSuccessListener { documents ->
                followingList.clear()

                val followingUserIds = documents.map { it.id }

                for (followedUserId in followingUserIds) {
                    db.collection("users").document(followedUserId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val user = userDoc.toObject(UserItem::class.java)
                            if (user != null) {
                                followingList.add(user)
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
    }
}
