package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itunesapi.DiaryAdapter
import com.example.itunesapi.DiaryItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject






class DiaryFragment : Fragment() {

    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var diaryList: List<DiaryItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)

        diaryRecyclerView = view.findViewById(R.id.diaryRecyclerView)
        diaryRecyclerView.layoutManager = LinearLayoutManager(context)

        // 🔹 Firestore에서 데이터 가져오기
        FirebaseFirestore.getInstance().collection("diaries")
            .get()
            .addOnSuccessListener { result ->
                val diaryList = mutableListOf<DiaryItem>()

                for (document in result) {
                    val diary = document.toObject(DiaryItem::class.java)

                    if (diary.title != "추가하기") {
                        diaryList.add(diary)
                    }
                }


                diaryAdapter = DiaryAdapter(diaryList) { diaryItem ->
                    val fragment = DiaryDetailFragment(diaryItem)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                diaryRecyclerView.adapter = diaryAdapter
            }
            .addOnFailureListener {
                Toast.makeText(context, "일기 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        return view
    }
}
