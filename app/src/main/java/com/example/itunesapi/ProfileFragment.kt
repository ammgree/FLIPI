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

class ProfileFragment : Fragment() {

    private lateinit var backButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var diaryTabButton: Button
    private lateinit var archiveTabButton: Button
    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var followingText : TextView
    private lateinit var followersText : TextView
    private lateinit var postCountText : TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔗 UI 연결
        backButton = view.findViewById(R.id.backButton)
        searchButton = view.findViewById(R.id.searchButton)
        profileImage = view.findViewById(R.id.profileImage)
        usernameText = view.findViewById(R.id.usernameText)
        diaryTabButton = view.findViewById(R.id.diaryTabButton)
        archiveTabButton = view.findViewById(R.id.archiveTabButton)
        diaryRecyclerView = view.findViewById(R.id.diaryRecyclerView)
        diaryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        followersText = view.findViewById(R.id.followersText)
        followingText = view.findViewById(R.id.followingText)
        postCountText = view.findViewById(R.id.postCountText)



        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        //diaryAdapter 설정

        val diaryList = mutableListOf<DiaryItem>()



        val diaryAdapter = DiaryAdapter(
            diaryList,
            onItemClick = { diaryItem ->
                // 클릭 시 처리: 예를 들어 일기 상세 보기로 이동
                Toast.makeText(requireContext(), "클릭됨: ${diaryItem.title}", Toast.LENGTH_SHORT).show()

                // 일기 상세 보기 프래그먼트로 이동
                val fragment = DiaryDetailFragment(diaryItem)
                parentFragmentManager.beginTransaction()
                     .replace(R.id.fragment_container, fragment)
                     .addToBackStack(null)
                     .commit()
            },
            onItemLongClick = { diaryItem ->
                // 길게 눌렀을 때 처리: 예를 들어 삭제 다이얼로그 띄우기
                Toast.makeText(requireContext(), "길게 누름: ${diaryItem.title}", Toast.LENGTH_SHORT).show()
            },
            isProfile = true
        )
        diaryRecyclerView.adapter = diaryAdapter

        // Firestore에서 프로필 정보 불러오기
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Unknown"
                    val profileImageUrl = document.getString("profileImageUrl")

                    usernameText.text = username
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .circleCrop()
                            .into(profileImage)
                    }
                }


            // Firestore에서 일기 목록 불러오기
            db.collection("users").document(uid).collection("diaries")
                .get()
                .addOnSuccessListener { result ->
                    diaryList.clear()
                    for (document in result) {
                        val diary = document.toObject(DiaryItem::class.java)
                        diaryList.add(diary)
                    }

                    // 일기 개수 반영
                    postCountText.text = "게시물 ${diaryList.size}"

                    diaryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "일기를 불러오지 못했어요", Toast.LENGTH_SHORT).show()
                }

        }

        // 뒤로가기 버튼: 홈으로
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        // 검색 버튼: 검색 화면으로
        searchButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        followersText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FollowersListFragment())
                .addToBackStack(null)
                .commit()
        }

        followingText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FollowingListFragment())
                .addToBackStack(null)
                .commit()
        }


        // 하단바 숨기기
        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.GONE

        // 일기 탭 기본 선택 상태
        diaryTabButton.setOnClickListener {
            // 이미 표시 중이니까 생략 또는 효과 넣어줘도 됨
        }

        archiveTabButton.setOnClickListener {
            Toast.makeText(requireContext(), "보관함 기능은 아직 준비 중입니다.", Toast.LENGTH_SHORT).show()
        }
    }

}
