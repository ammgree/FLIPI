package com.example.itunesapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.util.ArrayList

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
// 에러나서 주석햇움
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        recyclerView = view.findViewById<RecyclerView>(R.id.friend)
//        val textView = view.findViewById<TextView>(R.id.textPlaylist)
//        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//        recyclerView.adapter = adapter
//
//        fun bind(playlist: ArrayList<>(), onClick : ())
//    }

    private fun recoPlaylist() {
        val imageView = ImageView(requireContext())
        view?.findViewById<LinearLayout>(R.id.songPlaylist)?.addView(imageView)
    }
}