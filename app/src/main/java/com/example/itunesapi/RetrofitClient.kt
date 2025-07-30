package com.example.itunesapi.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import com.example.itunesapi.network.YoutubeApiService




object RetrofitClient {
    private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

    val youtubeService: YoutubeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YoutubeApiService::class.java)
    }
}
