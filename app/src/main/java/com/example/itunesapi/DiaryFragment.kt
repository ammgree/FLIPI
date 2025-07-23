package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itunesapi.DiaryAdapter
import com.example.itunesapi.DiaryItem


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

        // 일단 예시
        diaryList = listOf(
            DiaryItem(
                imageUrl = "https://example.com/image1.jpg",
                title = "GURU2 언제 끝나?",
                date = "2025-07-19",
                isPublic = true
            ),
            DiaryItem(
                imageUrl = "https://example.com/image2.jpg",
                title = "진짜 개더운날 🤯",
                date = "2025-07-19",
                isPublic = true
            ),
            DiaryItem(
                imageUrl = "https://example.com/image3.jpg",
                title = "이건 첫번째 레슨",
                date = "2025-07-19",
                isPublic = true
            )
        )

        diaryAdapter = DiaryAdapter(diaryList) { diaryItem ->
            if (diaryItem.title == "추가하기") {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DiaryAddFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                // 기존 일기 보기 로직
            }
        }





        diaryRecyclerView.adapter = diaryAdapter

        return view
    }
}
