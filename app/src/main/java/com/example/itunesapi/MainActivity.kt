package com.example.itunesapi

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // <도연>main에서 fragment_container로 바꿨습니다. (이렇게 해야 다른 프로그먼트로 이동할떄, 하단바가 안 사라져요)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SearchFragment())
            .commit()

        // <도연>일기버튼 연결하겠습니다
        val dailyButton = findViewById<ImageButton>(R.id.nav_daily)

        dailyButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryFragment())
                .commit()
        }

        // <규리>검색버튼 연결하겠습니다
        val searchButton = findViewById<ImageButton>(R.id.nav_search)

        searchButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchFragment())
                .commit()
        }

        // 보관함 버튼
        val storeButton = findViewById<ImageButton>(R.id.nav_store)

        storeButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StoreFragment())
                .commit()
        }
    }
}