package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class StoryAddFragment : DialogFragment() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var selectedSongText: TextView
    private lateinit var addStoryButton: Button

    // 선택된 노래 정보 저장용 변수
    private var selectedTitle: String = ""
    private var selectedArtist: String = ""
    private var selectedAlbumArtUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchButton)
        selectedSongText = view.findViewById(R.id.selectedSongText)
        addStoryButton = view.findViewById(R.id.addStoryButton)

        // 1. 검색 버튼 클릭 시
        searchButton.setOnClickListener {
            val query = searchInput.text.toString()
            if (query.isNotBlank()) {
                searchSong(query)
            } else {
                Toast.makeText(requireContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. 추가 버튼 클릭 시 Firestore 저장
        addStoryButton.setOnClickListener {
            if (selectedTitle.isNotBlank()) {
                val story = StoryItem(selectedTitle, selectedArtist, selectedAlbumArtUrl)
                val db = FirebaseFirestore.getInstance()
                db.collection("stories")
                    .add(story)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "스토리 추가 완료", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "스토리 추가 실패", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "노래를 먼저 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // iTunes API 검색 예시
    private fun searchSong(query: String) {
        val url = "https://itunes.apple.com/search?term=${URLEncoder.encode(query, "UTF-8")}&entity=song&limit=1"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }

                val json = JSONObject(response)
                val results = json.getJSONArray("results")

                if (results.length() > 0) {
                    val item = results.getJSONObject(0)
                    selectedTitle = item.getString("trackName")
                    selectedArtist = item.getString("artistName")
                    selectedAlbumArtUrl = item.getString("artworkUrl100")

                    // UI 업데이트는 메인스레드에서
                    requireActivity().runOnUiThread {
                        selectedSongText.text = "$selectedTitle - $selectedArtist"
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "결과 없음", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "검색 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}

