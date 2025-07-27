package com.example.itunesapi

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DiaryAdapter(
    private val diaryList: List<DiaryItem>,
    private val onItemClick: (DiaryItem) -> Unit,
    private val onItemLongClick: (DiaryItem) -> Unit,
    private val isProfile: Boolean = false  // ← 추가
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
                .load(item.musicImageUrl)
                .into(diaryImage)

            diaryTitle.text = item.title
            diaryDate.text = if (item.date.isNotBlank()) item.date else "날짜 없음"
            diaryPublic.text = if (item.isPublic) "공개" else "비공개"

            itemView.setOnClickListener {
                onItemClick(item)
            }
            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }


    }

    inner class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                // "추가하기" 레이아웃 클릭 시 다이어리 작성 화면으로 이동
                val context = itemView.context
                if (context is MainActivity) {  // MainActivity는 너 프로젝트에 맞게 바꿔줘!
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, DiaryAddFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
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
        if (holder is DiaryViewHolder && position < diaryList.size) {
            val item = diaryList[position]
            Log.d("DiaryAdapter", "title=${item.title}, isPublic=${item.isPublic}")
            holder.bind(item)  // 모든 처리는 bind()에서!
        }
    }



    override fun getItemCount(): Int {
        return if (isProfile) {
            diaryList.size  // 추가하기 버튼 안 넣음
        } else {
            diaryList.size + 1  // 마지막에 추가하기 버튼 포함
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (!isProfile && position == diaryList.size) VIEW_TYPE_ADD else VIEW_TYPE_ITEM
    }
}
