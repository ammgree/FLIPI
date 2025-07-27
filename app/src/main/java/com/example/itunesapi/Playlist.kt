package com.example.itunesapi

data class Playlist(
    var title: String,
    var picture: String? = null,
    val songs: MutableList<Album> = mutableListOf()
)
