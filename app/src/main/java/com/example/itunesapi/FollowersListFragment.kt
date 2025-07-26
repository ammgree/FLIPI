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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_followers_list, container, false)

        recyclerView = view.findViewById(R.id.followRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)


        adapter = FollowUserAdapter(followersList) { username ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OtherUserProfileFragment.newInstance(username))
                .addToBackStack(null)
                .commit()
        }



        recyclerView.adapter = adapter

        loadFollowers()

        return view
    }

    private fun loadFollowers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(currentUserId).collection("followers")
            .get()
            .addOnSuccessListener { documents ->
                followersList.clear()
                for (doc in documents) {
                    val user = doc.toObject(UserItem::class.java)
                    followersList.add(user)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
