package com.example.itunesapi.model

// 유튜브 API 응답 전체 구조를 나타내는 데이터 클래스
data class YoutubeApiResponse(
    val items: List<Item> // 유튜브 영상 아이템들의 리스트
)

// 각 영상 아이템을 나타내는 클래스
data class Item(
    val snippet: Snippet // 영상의 기본 정보(snippet)를 포함
)

// 영상의 세부 정보(제목, 채널명, 썸네일 등)를 담는 클래스
data class Snippet(
    val title: String, // 영상 제목
    val channelTitle: String, // 채널 이름
    val thumbnails: Thumbnails // 썸네일 이미지 정보
)

// 다양한 해상도의 썸네일 중 'high' 해상도 정보를 담는 클래스
data class Thumbnails(
    val high: Thumbnail // 고해상도 썸네일 객체
)

// 썸네일 하나의 정보를 담는 클래스 (URL)
data class Thumbnail(
    val url: String // 썸네일 이미지의 URL
)
