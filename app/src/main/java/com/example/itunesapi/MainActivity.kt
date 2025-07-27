package com.example.itunesapi

import android.os.Bundle
import android.widget.ImageButton

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    val playLists = mutableListOf<Playlist>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        // 타이머 버튼
        val navTimer = findViewById<ImageButton>(R.id.nav_timer)

        navTimer.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FocusModeFragment())
                .addToBackStack(null)
                .commit()
        }

        // 홈 버튼
        val navHome = findViewById<ImageButton>(R.id.nav_home)

        navHome.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
}