package com.example.itunesapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DiaryAdapter(
    private val diaryList: List<DiaryItem>,
    private val onItemClick: (DiaryItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_ADD = 1
    }

    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val diaryImage: ImageView = itemView.findViewById(R.id.diaryImage)
        private val diaryTitle: TextView = itemView.findViewById(R.id.diaryTitle)
        private val diaryDate: TextView = itemView.findViewById(R.id.diaryDate)
        private val diaryPublic: TextView = itemView.findViewById(R.id.diaryPublic)

        fun bind(item: DiaryItem) {
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(diaryImage)

            diaryTitle.text = item.title
            diaryDate.text = item.date
            diaryPublic.text = if (item.isPublic) "공개" else "비공개"

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    inner class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addText: TextView = itemView.findViewById(R.id.addText)

        init {
            itemView.setOnClickListener {

                onItemClick(DiaryItem("", "추가하기", "", false))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == diaryList.size) VIEW_TYPE_ADD else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
            DiaryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_diary, parent, false)
            AddViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DiaryViewHolder) {
            holder.bind(diaryList[position])
        }
    }

    override fun getItemCount(): Int = diaryList.size + 1
}
