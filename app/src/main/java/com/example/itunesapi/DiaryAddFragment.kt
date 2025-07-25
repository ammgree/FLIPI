package com.example.itunesapi

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import com.example.itunesapi.DiaryItem
import java.util.*



class DiaryAddFragment : Fragment() {

    private lateinit var musicTitleTextView: TextView
    private lateinit var musicImageView: ImageView
    private var selectedMusicUrl: String? = null
    private var selectedMusicArtist: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_add, container, false)

        val btnMusic = view.findViewById<ImageButton>(R.id.btnMusic)
        val bottomNav = activity?.findViewById<View>(R.id.navigationBar)
        bottomNav?.visibility = View.GONE

        btnMusic.setOnClickListener {
            val fragment = SearchFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("fromDiaryAdd", true)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        musicTitleTextView = view.findViewById(R.id.musicTitleTextView)
        musicImageView = view.findViewById(R.id.musicImageView)
        val editTitle = view.findViewById<EditText>(R.id.editTitle)
        val editContent = view.findViewById<EditText>(R.id.editContent)
        val btnSave = view.findViewById<ImageButton>(R.id.btnSave)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val switch = view.findViewById<Switch>(R.id.switchVisibility)

        switch.setOnCheckedChangeListener { _, isChecked ->
            switch.text = if (isChecked) "공개" else "비공개"
        }

        btnSave.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val content = editContent.text.toString().trim()
            val isPublic = switch.isChecked

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(context, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val diary = DiaryItem(
                title = title,
                content = content,
                date = date,
                isPublic = isPublic,
                musicTitle = musicTitleTextView.text.toString().split(" | ").firstOrNull() ?: "",
                musicArtist = selectedMusicArtist,
                musicImageUrl = musicImageView.tag as? String ?: "",
                musicUrl = selectedMusicUrl ?: ""
            )

            FirebaseFirestore.getInstance().collection("diaries")
                .add(diary)
                .addOnSuccessListener {
                    Toast.makeText(context, "일기 저장 완료!", Toast.LENGTH_SHORT).show()
                    MusicPlayerManager.stop() // ✅ 저장 성공 후 음악 정지
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnBack.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("알림")
                .setMessage("일기 작성 화면에서 나가시겠습니까?")
                .setPositiveButton("네") { _, _ ->
                    MusicPlayerManager.stop() // ✅ 뒤로가기 시 음악 정지
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("아니요", null)
                .show()
        }

        parentFragmentManager.setFragmentResultListener("songSelected", viewLifecycleOwner) { _, bundle ->
            val songTitle = bundle.getString("songTitle") ?: ""
            val songUrl = bundle.getString("songUrl")
            val songArtist = bundle.getString("songArtist") ?: ""
            val albumImage = bundle.getString("albumImage")

            musicTitleTextView.text = "$songTitle | $songArtist"
            musicTitleTextView.visibility = View.VISIBLE

            musicImageView.tag = albumImage
            musicImageView.visibility = View.GONE
            selectedMusicUrl = songUrl
            selectedMusicArtist = songArtist
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MusicPlayerManager.stop() // ✅ 프래그먼트 종료 시 음악 정지
        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.VISIBLE
    }
}
