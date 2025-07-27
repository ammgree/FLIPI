package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class StoryDetailFragment : Fragment() {

    private lateinit var songTitleTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var albumImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 연결
        songTitleTextView = view.findViewById(R.id.songTitleTextView)
        artistTextView = view.findViewById(R.id.artistTextView)
        albumImageView = view.findViewById(R.id.albumImageView)

        // HomeFragment에서 전달된 StoryItem 불러오기
        val story = arguments?.getParcelable<StoryItem>("story")
        story?.let {
            songTitleTextView.text = it.title
            artistTextView.text = it.artist
            Glide.with(requireContext()).load(it.albumArtUrl).into(albumImageView)
        }

        // StoryAddFragment에서 전달된 데이터 수신
        parentFragmentManager.setFragmentResultListener("storySongSelected", viewLifecycleOwner) { _, bundle ->
            val title = bundle.getString("songTitle")
            val artist = bundle.getString("artistName")
            val imageUrl = bundle.getString("albumArtUrl")

            songTitleTextView.text = title
            artistTextView.text = artist
            Glide.with(requireContext()).load(imageUrl).into(albumImageView)
        }
    }
}
