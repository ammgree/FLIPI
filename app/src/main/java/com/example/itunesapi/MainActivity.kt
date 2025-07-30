package com.example.itunesapi

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

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

        val mood = intent.getStringExtra("mood")
        val username = intent.getStringExtra("username")

        if(savedInstanceState == null && mood != null && username != null) {
            val homeFragment = HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("mood", mood)
                    putString("username", username)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit()
        }


        val currentSong = findViewById<ConstraintLayout>(R.id.currentSong)
        val songAlbum = findViewById<ImageView>(R.id.songAlbum)
        val songTitle = findViewById<TextView>(R.id.songTitle)

        val playButton = findViewById<ImageButton>(R.id.play)
        val pauseButton = findViewById<ImageButton>(R.id.pause)

        MusicPlayerManager.addOnPlayPauseChangeListener{ isPlaying ->
            playButton.visibility = if (isPlaying) GONE else VISIBLE
            pauseButton.visibility = if (isPlaying) VISIBLE else GONE
        }

        MusicPlayerManager.setOnStartListener {
            runOnUiThread {
                currentSong.visibility = VISIBLE
                Glide.with(this)
                    .load(MusicPlayerManager.song.imageUrl)
                    .into(songAlbum)
                songTitle.text = MusicPlayerManager.song.title
            }
        }

        if (MusicPlayerManager.isPlaying()) {
            currentSong.visibility = VISIBLE
            Glide.with(this)
                .load(MusicPlayerManager.song.imageUrl)
                .into(songAlbum)
            songTitle.text = MusicPlayerManager.song.title
        } else {
            currentSong.visibility = GONE
        }

        playButton.setOnClickListener {
            MusicPlayerManager.resume()
            playButton.visibility = GONE
            pauseButton.visibility = VISIBLE
        }

        pauseButton.setOnClickListener {
            MusicPlayerManager.pause()
            playButton.visibility = VISIBLE
            pauseButton.visibility = GONE
        }


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
            val mood = intent.getStringExtra("mood")
            val username = intent.getStringExtra("username")

            val fragment = HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("mood", mood)
                    putString("username", username)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }



}