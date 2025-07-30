package com.example.itunesapi

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

object KeyManager {

    private var keys: JSONObject? = null

    // 초기화 - 최초 1번만 실행
    fun init(context: Context) {
        if (keys == null) {
            val inputStream: InputStream = context.assets.open("secret_keys.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            keys = JSONObject(jsonString)
        }
    }

    // 키 가져오기
    fun get(key: String): String {
        return keys?.getString(key) ?: ""
    }
}
