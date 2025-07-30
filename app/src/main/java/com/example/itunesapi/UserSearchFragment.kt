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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserSearchFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var resultRecyclerView: RecyclerView
    private lateinit var adapter: FollowUserAdapter

    private val userList = mutableListOf<UserItem>()
    private var currentUsername: String? = null  // 현재 사용자 이름 저장용

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

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        resultRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 로그인한 유저의 username 미리 가져온 후 adapter 초기화
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener { document ->
                    currentUsername = document.getString("username")

                    // 어댑터 설정 (username 불러온 뒤!)
                    adapter = FollowUserAdapter(
                        userList,
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

                    resultRecyclerView.adapter = adapter
                }
        }

        // 검색 버튼 클릭 시
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
