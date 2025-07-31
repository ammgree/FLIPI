package com.example.itunesapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// 팔로우/팔로잉 목록을 표시하는 RecyclerView 어댑터
class FollowUserAdapter(
    private val userList: List<UserItem>, // 표시할 사용자 목록
    private val currentUsername: String?, // 현재 로그인한 사용자 이름
    private val onNavigateToProfile: () -> Unit, // 본인 프로필로 이동 콜백
    private val onNavigateToOtherUser: (String) -> Unit // 타인 프로필로 이동 콜백
) : RecyclerView.Adapter<FollowUserAdapter.FollowUserViewHolder>() {

    // ViewHolder 정의
    inner class FollowUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameText: TextView = itemView.findViewById(R.id.usernameText) // 유저 이름 텍스트
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImage) // 프로필 이미지

        // 데이터 바인딩
        fun bind(user: UserItem) {
            usernameText.text = user.username
            if (!user.profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .into(profileImage) // 이미지가 있으면 로드
            } else {
                profileImage.setImageResource(R.drawable.circle_background) // 기본 이미지
            }

            // 클릭 시 본인인지 타인인지 구분해서 이동
            itemView.setOnClickListener {
                if (user.username == currentUsername) {
                    onNavigateToProfile() // 본인 프로필로 이동
                } else {
                    onNavigateToOtherUser(user.username) // 타인 프로필로 이동
                }
            }
        }
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follow_user, parent, false)
        return FollowUserViewHolder(view)
    }

    // 데이터 바인딩 호출
    override fun onBindViewHolder(holder: FollowUserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    // 전체 아이템 개수 반환
    override fun getItemCount(): Int = userList.size
}
