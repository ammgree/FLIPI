package com.example.itunesapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class StoreFragment : Fragment() {

    private lateinit var storeRecyclerView: RecyclerView
    private lateinit var storeAdapter: PlaylistAdapter
    private lateinit var storeList : List<Playlist>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_store, container, false)

        storeRecyclerView = view.findViewById(R.id.storeView)
        storeRecyclerView.layoutManager = LinearLayoutManager(context)

        // 일단 예시
        storeList = listOf(
            Playlist(
                title = "플리제목",
                picture = "https://example.com/image1.jpg"
            )
        )
        storeAdapter = PlaylistAdapter(storeList) {
            Toast.makeText(requireContext(), "${storeList[0].title} 클릭됨", Toast.LENGTH_SHORT).show()
        }
        storeRecyclerView.adapter = storeAdapter
        return view
    }
}