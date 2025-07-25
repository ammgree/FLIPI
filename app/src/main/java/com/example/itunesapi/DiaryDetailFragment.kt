package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class DiaryDetailFragment(private val diaryItem: DiaryItem) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_detail, container, false)

        val titleText = view.findViewById<TextView>(R.id.textTitle)
        val contentText = view.findViewById<TextView>(R.id.textContent)
        val dateText = view.findViewById<TextView>(R.id.textDate)
        val visibilityText = view.findViewById<TextView>(R.id.textVisibility)
        val backButton = view.findViewById<ImageButton>(R.id.btnBack)


        titleText.text = diaryItem.title
        contentText.text = diaryItem.content
        dateText.text = diaryItem.date
        visibilityText.text = if (diaryItem.isPublic) "공개" else "비공개"


        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
