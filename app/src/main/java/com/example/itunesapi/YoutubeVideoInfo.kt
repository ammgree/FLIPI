package com.example.itunesapi.model

// 유튜브 영상의 기본 정보를 담는 데이터 클래스
data class YoutubeVideoInfo(
    val title: String,         // 영상 제목
    val videoId: String,       // 유튜브 영상 ID
    val channelTitle: String,  // 영상이 업로드된 채널 이름
    val thumbnailUrl: String   // 영상 썸네일 이미지 URL
)
