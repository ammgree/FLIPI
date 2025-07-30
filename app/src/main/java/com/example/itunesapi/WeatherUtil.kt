package com.example.itunesapi

object WeatherUtil {
    fun classifyWeather(main : String) : String {
        return when(main.lowercase()){
            "clear" -> "맑음"
            "clouds" -> "흐림"
            "rain", "drizzle", "thunderstorm", "snow" -> "비/눈"
            else -> "알 수 없음"
        }
    }
}