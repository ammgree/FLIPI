package com.example.itunesapi

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class DiaryAddFragment : Fragment() {

    // 선택한 음악의 제목과 이미지를 표시할 뷰
    private lateinit var musicTitleTextView: TextView
    private lateinit var musicImageView: ImageView

    // 음악의 URL과 아티스트 이름 저장용 변수
    private var selectedMusicUrl: String? = null
    private var selectedMusicArtist: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트 레이아웃을 inflate
        val view = inflater.inflate(R.layout.fragment_diary_add, container, false)

        val btnMusic = view.findViewById<ImageButton>(R.id.btnMusic)

        // 하단 네비게이션 바 숨기기
        val bottomNav = activity?.findViewById<View>(R.id.navigationBar)
        bottomNav?.visibility = View.GONE

        // 음악 추가 버튼 클릭 시 SearchFragment로 이동, fromDiaryAdd 플래그 전달
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

        // 뷰 초기화
        musicTitleTextView = view.findViewById(R.id.musicTitleTextView)
        musicImageView = view.findViewById(R.id.musicImageView)
        val editTitle = view.findViewById<EditText>(R.id.editTitle)
        val editContent = view.findViewById<EditText>(R.id.editContent)
        val btnSave = view.findViewById<ImageButton>(R.id.btnSave)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val switch = view.findViewById<Switch>(R.id.switchVisibility)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 공개/비공개 스위치 텍스트 변경
        switch.setOnCheckedChangeListener { _, isChecked ->
            switch.text = if (isChecked) "공개" else "비공개"
        }

        // 저장 버튼 클릭 시 실행
        btnSave.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val content = editContent.text.toString().trim()
            val isPublic = switch.isChecked

            // 제목과 내용이 비어 있으면 경고
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(context, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 날짜 설정
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // DiaryItem 객체 생성
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

            // Firestore에 일기 저장
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("diaries")
                .add(diary)
                .addOnSuccessListener {
                    Toast.makeText(context, "일기 저장 완료!", Toast.LENGTH_SHORT).show()
                    MusicPlayerManager.stop() // 저장 성공 시 음악 정지
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 뒤로가기 버튼 클릭 시 알림창 표시 후 뒤로 이동
        btnBack.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("알림")
                .setMessage("일기 작성 화면에서 나가시겠습니까?")
                .setPositiveButton("네") { _, _ ->
                    MusicPlayerManager.stop() // 뒤로가기 시 음악 정지
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("아니요", null)
                .show()
        }

        // SearchFragment에서 선택한 노래 정보를 수신
        parentFragmentManager.setFragmentResultListener("songSelected", viewLifecycleOwner) { _, bundle ->
            val songTitle = bundle.getString("songTitle") ?: ""
            val songUrl = bundle.getString("songUrl")
            val songArtist = bundle.getString("songArtist") ?: ""
            val albumImage = bundle.getString("albumImage")

            // 선택된 노래 정보 UI에 반영
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
        // 프래그먼트 종료 시 음악 정지 및 하단 바 다시 표시
        MusicPlayerManager.stop()
        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.VISIBLE
    }
}
