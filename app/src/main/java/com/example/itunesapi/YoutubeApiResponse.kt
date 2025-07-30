package com.example.itunesapi.model

// YoutubeApiResponse.kt
data class YoutubeApiResponse(
    val items: List<Item>
)

data class Item(
    val snippet: Snippet
)

data class Snippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val high: Thumbnail
)

data class Thumbnail(
    val url: String
)
