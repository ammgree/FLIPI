package com.example.itunesapi

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast


class DiaryAddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_add, container, false)

        val editTitle = view.findViewById<EditText>(R.id.editTitle)
        val editContent = view.findViewById<EditText>(R.id.editContent)
        val btnSave = view.findViewById<ImageButton>(R.id.btnSave)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)



        val bottomNav = activity?.findViewById<View>(R.id.navigationBar)
        bottomNav?.visibility = View.GONE

        btnSave.setOnClickListener {
            val title = editTitle.text.toString()
            val content = editContent.text.toString()


            Toast.makeText(context, "저장 완료: $title", Toast.LENGTH_SHORT).show()

            // 뒤로 가기
            parentFragmentManager.popBackStack()
        }

        btnBack.setOnClickListener {
            // 다이얼로그로 확인 받고 이동
            AlertDialog.Builder(requireContext())
                .setTitle("알림")
                .setMessage("일기 작성 화면에서 나가시겠습니까?")
                .setPositiveButton("네") { _, _ ->
                    // 이전 프래그먼트로 이동
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("아니요", null)
                .show()
        }

        val switch = view.findViewById<Switch>(R.id.switchVisibility)

        switch.setOnCheckedChangeListener { _, isChecked ->
            switch.text = if (isChecked) "공개" else "비공개"
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 하단 바 다시 보이기
        val navBar = activity?.findViewById<View>(R.id.navigationBar)
        navBar?.visibility = View.VISIBLE
    }


}
