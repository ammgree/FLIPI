package com.example.itunesapi

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Mood : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        val username = intent.getStringExtra("username")
        val moodName = findViewById<TextView>(R.id.moodName)
        moodName.text = "$username 님."

        val bad: ImageButton = findViewById(R.id.moodBad)
        bad.setOnClickListener {
            openHome("기분이 좋지않은", username?:"")
        }
        val sad: ImageButton = findViewById(R.id.moodSad)
        sad.setOnClickListener {
            openHome("슬픈", username?:"")
        }
        val mad: ImageButton = findViewById(R.id.moodMad)
        mad.setOnClickListener {
            openHome("화난", username?:"")
        }
        val soso: ImageButton = findViewById(R.id.moodSoso)
        soso.setOnClickListener {
            openHome("기분이 그저그런", username?:"")
        }
        val good: ImageButton = findViewById(R.id.moodGood)
        good.setOnClickListener {
            openHome("기분이 좋은", username?:"")
        }
        val happy: ImageButton = findViewById(R.id.moodHappy)
        happy.setOnClickListener {
            openHome("행복한", username?:"")
        }
        val chill: ImageButton = findViewById(R.id.moodChill)
        chill.setOnClickListener {
            openHome("차분한", username?:"")
        }
    }

    private fun openHome(mood : String, username : String){
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra("mood", mood)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}