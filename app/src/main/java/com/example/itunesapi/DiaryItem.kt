
package com.example.itunesapi

data class DiaryItem(
    var title: String = "",
    var content: String = "",
    var date: String = "",
    var isPublic: Boolean = false,
    var musicTitle: String? = null,
    var musicArtist: String? = null,
    var musicImageUrl: String? = null,
    var musicUrl: String? = null
)
