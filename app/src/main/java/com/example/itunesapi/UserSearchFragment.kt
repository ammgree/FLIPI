package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class UserSearchFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var resultRecyclerView: RecyclerView
    private lateinit var adapter: FollowUserAdapter

    private val userList = mutableListOf<UserItem>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchBtn)
        resultRecyclerView = view.findViewById(R.id.resultRecyclerView)


        adapter = FollowUserAdapter(userList) { username ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OtherUserProfileFragment.newInstance(username))
                .addToBackStack(null)
                .commit()
        }



        resultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        resultRecyclerView.adapter = adapter

        searchButton.setOnClickListener {
            val queryText = searchInput.text.toString().trim()
            if (queryText.isNotEmpty()) {
                searchUsers(queryText)
            }
        }
    }

    private fun searchUsers(keyword: String) {
        FirebaseFirestore.getInstance().collection("users")
            .whereGreaterThanOrEqualTo("username", keyword)
            .whereLessThanOrEqualTo("username", keyword + '\uf8ff')
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (doc in result) {
                    val user = doc.toObject(UserItem::class.java)
                    userList.add(user)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "검색 실패", Toast.LENGTH_SHORT).show()
            }
    }
}
