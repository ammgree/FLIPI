package com.example.itunesapi

// Firestore에 저장될 일기(Diary) 데이터를 담는 데이터 클래스
data class DiaryItem(
    var title: String = "",                // 일기 제목
    var content: String = "",              // 일기 내용
    var date: String = "",                 // 일기 작성 날짜 (형식: yyyy-MM-dd)
    var isPublic: Boolean = false,         // 공개 여부 (true: 공개, false: 비공개)
    var musicTitle: String? = null,        // 연동된 노래 제목 (nullable)
    var musicArtist: String? = null,       // 노래 아티스트 이름 (nullable)
    var musicImageUrl: String? = null,     // 노래 앨범 이미지 URL (nullable)
    var musicUrl: String? = null           // 노래 미리듣기 URL (nullable)
)
