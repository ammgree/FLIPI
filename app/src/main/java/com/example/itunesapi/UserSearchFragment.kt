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

    private lateinit var searchInput: EditText              // 사용자 검색 입력창
    private lateinit var searchButton: ImageButton         // 검색 버튼
    private lateinit var resultRecyclerView: RecyclerView  // 검색 결과 표시용 RecyclerView
    private lateinit var adapter: FollowUserAdapter        // RecyclerView에 연결할 어댑터

    private val userList = mutableListOf<UserItem>()       // 검색된 사용자 목록 저장
    private var currentUsername: String? = null            // 현재 로그인한 사용자 이름 저장용

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 요소 연결
        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchBtn)
        resultRecyclerView = view.findViewById(R.id.resultRecyclerView)

        // 뒤로가기 버튼 클릭 시 이전 화면으로 돌아감
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // RecyclerView 세로 정렬 설정
        resultRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 현재 로그인한 사용자의 UID로부터 username 가져오기
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener { document ->
                    currentUsername = document.getString("username")

                    // 어댑터 초기화 (현재 사용자 이름 포함)
                    adapter = FollowUserAdapter(
                        userList,
                        currentUsername,
                        onNavigateToProfile = {
                            // 내 프로필로 이동
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, ProfileFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onNavigateToOtherUser = { username ->
                            // 다른 유저의 프로필로 이동
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, OtherUserProfileFragment.newInstance(username))
                                .addToBackStack(null)
                                .commit()
                        }
                    )

                    // 어댑터 연결
                    resultRecyclerView.adapter = adapter
                }
        }

        // 검색 버튼 클릭 시 사용자 검색 수행
        searchButton.setOnClickListener {
            val queryText = searchInput.text.toString().trim()
            if (queryText.isNotEmpty()) {
                searchUsers(queryText)
            }
        }
    }

    // Firestore에서 username 기준으로 사용자 검색
    private fun searchUsers(keyword: String) {
        FirebaseFirestore.getInstance().collection("users")
            .whereGreaterThanOrEqualTo("username", keyword)
            .whereLessThanOrEqualTo("username", keyword + '\uf8ff') // prefix 검색을 위한 범위 설정
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (doc in result) {
                    val user = doc.toObject(UserItem::class.java)
                    userList.add(user)
                }
                adapter.notifyDataSetChanged() // 검색 결과 갱신
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "검색 실패", Toast.LENGTH_SHORT).show()
            }
    }
}
