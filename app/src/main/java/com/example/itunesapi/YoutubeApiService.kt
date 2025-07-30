package com.example.itunesapi.network


import com.example.itunesapi.model.YoutubeApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApiService {
    @GET("videos")
    fun getVideoInfo(
        @Query("part") part: String = "snippet",
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): Call<YoutubeApiResponse>
}
