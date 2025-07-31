package com.example.itunesapi

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class DiaryFragment : Fragment() {

    // 일기 목록을 표시할 RecyclerView와 어댑터 선언
    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_diary 레이아웃 inflate
        val view = inflater.inflate(R.layout.fragment_diary, container, false)

        // RecyclerView 초기화 및 LayoutManager 설정
        diaryRecyclerView = view.findViewById(R.id.diaryRecyclerView)
        diaryRecyclerView.layoutManager = LinearLayoutManager(context)

        // 일기 데이터 로드
        loadDiaries()

        return view
    }

    // Firestore에서 일기 데이터를 불러오는 함수
    private fun loadDiaries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("diaries")
            .get()
            .addOnSuccessListener { result ->
                val diaryList = mutableListOf<DiaryItem>()

                // 가져온 문서들을 DiaryItem으로 변환하여 리스트에 추가
                for (document in result) {
                    val diary = document.toObject<DiaryItem>()
                    if (diary.title != "추가하기") {  // '추가하기'는 제외
                        diaryList.add(diary)
                    }
                }

                // 어댑터 설정 및 아이템 클릭/롱클릭 리스너 구현
                diaryAdapter = DiaryAdapter(
                    diaryList,
                    onItemClick = { diaryItem ->
                        // 일기 클릭 시 상세보기 프래그먼트로 이동
                        val fragment = DiaryDetailFragment(diaryItem)
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    onItemLongClick = { diaryItem ->
                        // 일기 롱클릭 시 삭제 다이얼로그 표시
                        AlertDialog.Builder(requireContext())
                            .setTitle("일기 삭제")
                            .setMessage("「${diaryItem.title}」을(를) 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { _, _ ->
                                // Firestore에서 해당 일기 문서를 찾아 삭제
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .collection("diaries")
                                    .whereEqualTo("title", diaryItem.title)
                                    .whereEqualTo("date", diaryItem.date)
                                    .get()
                                    .addOnSuccessListener { result ->
                                        for (document in result) {
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(userId)
                                                .collection("diaries")
                                                .document(document.id)
                                                .delete()
                                        }
                                        Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                        loadDiaries() // 삭제 후 다시 불러오기
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .setNegativeButton("취소", null)
                            .show()
                    }
                )

                // RecyclerView에 어댑터 연결
                diaryRecyclerView.adapter = diaryAdapter
            }
            .addOnFailureListener {
                // 일기 로딩 실패 시 토스트 메시지 출력
                Toast.makeText(context, "일기 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
