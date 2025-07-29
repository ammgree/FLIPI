package com.example.itunesapi

data class Playlist(
    var title: String,
    var picture: String? = null,
    val songs: MutableList<Album> = mutableListOf()
)

// firebase 넣기 위해
data class PlaylistDTO(
    val title: String = "",
    val picture: String? = null,
    val songs: List<Map<String, Any>> = emptyList()
)

fun Album.toMap(): Map<String, Any> {
    return mapOf(
        "title" to title,
        "artist" to artist,
        "album" to album,
        "imageUrl" to imageUrl,
        "songUrl" to songUrl
    )
}
