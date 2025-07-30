package com.example.itunesapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StoryAdapter(
    private val stories: List<StoryItem>,
    private val onItemClick: (StoryItem) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleTextView)
        val artistText: TextView = itemView.findViewById(R.id.artistTextView)
        val albumImage: ImageView = itemView.findViewById(R.id.albumImageView)

        init {
            itemView.setOnClickListener {
                onItemClick(stories[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        holder.titleText.text = story.title
        holder.artistText.text = story.artist

        Glide.with(holder.itemView.context)
            .load(story.albumArtUrl)   // 이미지 URL 넣기
            .circleCrop()             // 원형 크롭 효과
            .into(holder.albumImage)

        holder.titleText.text = story.title
        holder.artistText.text = story.artist

    }

    override fun getItemCount(): Int = stories.size
}
