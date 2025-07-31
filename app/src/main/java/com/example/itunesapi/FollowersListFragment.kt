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
    private val followersList = mutableListOf<UserItem>() //팔로워 정보를 담을 리스트

    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var currentUsername: String? = null  // 현재 로그인한 유저의 username

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_followers_list, container, false)

        // 뒤로가기 버튼 클릭 시 이전 화면으로 이동
        val backButton = view.findViewById<View>(R.id.btnBack)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // RecyclerView 초기 설정
        recyclerView = view.findViewById(R.id.followRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 인자로 전달받은 userId 가져오기 (팔로워 목록 대상 유저)
        userId = arguments?.getString("userId")

        // 현재 로그인한 유저의 username 불러오기
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    currentUsername = document.getString("username")

                    //FollowUserAdapter 설정 (자기 프로필/타인 프로필로 이동 기능 포함)
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

                    // username을 정상적으로 불러온 뒤에 팔로워 리스트 로드
                    loadFollowers()
                }
        }

        return view
    }

    // 해당 유저의 팔로워 정보를 Firestore에서 불러와 리스트에 추가
    private fun loadFollowers() {
        val targetUserId = userId ?: return

        db.collection("users").document(targetUserId).collection("followers")
            .get()
            .addOnSuccessListener { documents ->
                followersList.clear() // 기존 리스트 초기화

                // follower 문서의 ID 리스트 추출 (각 ID는 팔로워 유저의 uid)
                val userIds = documents.map { it.id }

                // 각 팔로워 유저의 프로필 정보 불러오기
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
