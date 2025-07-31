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
    private val followingList = mutableListOf<UserItem>() //  내가 팔로우한 사용자 정보 리스트
    private var currentUsername: String? = null // 현재 로그인한 유저의 이름 (null 상태로 시작)

    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null //  해당 프로필의 userId (팔로잉 리스트 주인)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_following_list, container, false)

        //  뒤로가기 버튼 클릭 시 이전 화면으로 이동
        val backButton = view.findViewById<View>(R.id.btnBack)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        //  RecyclerView 설정
        recyclerView = view.findViewById(R.id.followRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        //  전달받은 유저 ID 가져오기 (팔로잉 목록 주인)
        userId = arguments?.getString("userId")

        //  어댑터 초기화 (currentUsername은 추후 설정 필요)
        adapter = FollowUserAdapter(
            followingList,
            currentUsername,  //  null이 아닌 값이어야 정상 작동함!
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

        //  팔로잉 리스트 불러오기
        loadFollowing()

        return view
    }

    //  해당 유저가 팔로우 중인 사용자들 불러오기
    private fun loadFollowing() {
        val targetUserId = userId ?: return

        db.collection("users").document(targetUserId).collection("following")
            .get()
            .addOnSuccessListener { documents ->
                followingList.clear() // 기존 리스트 초기화

                val followingUserIds = documents.map { it.id } // 팔로잉한 유저 ID 리스트 추출

                //  각 팔로잉 유저의 상세 정보 가져오기
                for (followedUserId in followingUserIds) {
                    db.collection("users").document(followedUserId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val user = userDoc.toObject(UserItem::class.java)
                            if (user != null) {
                                followingList.add(user)
                                adapter.notifyDataSetChanged() // 리스트 갱신
                            }
                        }
                }
            }
    }
}
