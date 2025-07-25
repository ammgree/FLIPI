
package com.example.itunesapi

data class DiaryItem(
    val title: String = "",
    val content: String = "",
    val date: String = "",
    var isPublic: Boolean = true,
    val imageUrl: String? = null  // 일단 null 허용
)
