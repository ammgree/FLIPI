package com.example.itunesapi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongItem(
    val url: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String
) : Parcelable