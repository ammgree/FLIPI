package com.example.itunesapi.network

import com.example.itunesapi.model.YoutubeApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// 유튜브 영상 정보를 요청하기 위한 Retrofit 인터페이스
interface YoutubeApiService {

    // 특정 videoId에 해당하는 유튜브 영상의 정보를 가져옴
    @GET("videos")
    fun getVideoInfo(
        @Query("part") part: String = "snippet", // 응답에 포함할 데이터 항목 (기본값은 snippet)
        @Query("id") videoId: String, // 조회할 유튜브 영상의 ID
        @Query("key") apiKey: String // 유튜브 API 키
    ): Call<YoutubeApiResponse> // YoutubeApiResponse 데이터 형태로 응답을 받음
}
