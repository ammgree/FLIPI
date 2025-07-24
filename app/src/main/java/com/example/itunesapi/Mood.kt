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
    lateinit var moodName : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        val bad : ImageButton = findViewById(R.id.moodBad)
        bad.setOnClickListener{
            openHome("기분이 좋지않은 ")
        }
        val sad : ImageButton = findViewById(R.id.moodSad)
        sad.setOnClickListener{
            openHome("슬픈 ")
        }
        val mad : ImageButton = findViewById(R.id.moodMad)
        mad.setOnClickListener{
            openHome("화난")
        }
        val soso : ImageButton = findViewById(R.id.moodSoso)
        soso.setOnClickListener{
            openHome("기분이 그저그런 ")
        }
        val good : ImageButton = findViewById(R.id.moodGood)
        good.setOnClickListener{
            openHome("기분이 좋은 ")
        }
        val happy : ImageButton = findViewById(R.id.moodHappy)
        happy.setOnClickListener{
            openHome("행복한 ")
        }
        val chill : ImageButton = findViewById(R.id.moodChill)
        chill.setOnClickListener{
            openHome("차분한 ")
        }

        val username = intent.getStringExtra("username")
        moodName = findViewById(R.id.moodName)
        moodName.text = "$username 님."
    }

    private fun openHome(mood : String){
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra("mood", mood)
        startActivity(intent)
        finish() //뒤로가기 방지
    }
}