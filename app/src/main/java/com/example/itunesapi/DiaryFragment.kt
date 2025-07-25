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

    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)

        diaryRecyclerView = view.findViewById(R.id.diaryRecyclerView)
        diaryRecyclerView.layoutManager = LinearLayoutManager(context)

        loadDiaries()

        return view
    }

    private fun loadDiaries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("diaries")
            .get()
            .addOnSuccessListener { result ->
                val diaryList = mutableListOf<DiaryItem>()
                for (document in result) {
                    val diary = document.toObject<DiaryItem>()
                    if (diary.title != "추가하기") {
                        diaryList.add(diary)
                    }
                }

                diaryAdapter = DiaryAdapter(
                    diaryList,
                    onItemClick = { diaryItem ->
                        val fragment = DiaryDetailFragment(diaryItem)
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    onItemLongClick = { diaryItem ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("일기 삭제")
                            .setMessage("「${diaryItem.title}」을(를) 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { _, _ ->
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
                                        loadDiaries()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .setNegativeButton("취소", null)
                            .show()
                    }
                )

                diaryRecyclerView.adapter = diaryAdapter
            }
            .addOnFailureListener {
                Toast.makeText(context, "일기 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
