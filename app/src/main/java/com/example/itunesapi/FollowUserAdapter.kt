package com.example.itunesapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FollowUserAdapter(
    private val userList: List<UserItem>,
    private val currentUsername: String?,  // 현재 로그인한 사용자 이름
    private val onNavigateToProfile: () -> Unit,  // 본인 프로필로 이동
    private val onNavigateToOtherUser: (String) -> Unit  // 타인 프로필로 이동
) : RecyclerView.Adapter<FollowUserAdapter.FollowUserViewHolder>() {

    inner class FollowUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameText: TextView = itemView.findViewById(R.id.usernameText)
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImage)

        fun bind(user: UserItem) {
            usernameText.text = user.username
            if (!user.profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.circle_background)
            }

            itemView.setOnClickListener {
                if (user.username == currentUsername) {
                    onNavigateToProfile()
                } else {
                    onNavigateToOtherUser(user.username)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follow_user, parent, false)
        return FollowUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowUserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size
}

