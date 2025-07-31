package com.example.itunesapi

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth //firebase 사용 권한

    lateinit var regButton : TextView //회원가입 버튼=글자버튼
    lateinit var startButton : FloatingActionButton //로그인 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance() //firebase에서 인스턴스 가져오기

        //회원가입
        regButton = findViewById(R.id.logReg)
        regButton.setOnClickListener{
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        //로그인
        startButton = findViewById(R.id.logNext)
        startButton.setOnClickListener{
            loginUser()
        }

        //insertSongs()//데베에 노래 넣는 함수입니당 맨밑에 함수구현 있는데 이젠 활성화 X
    }

    private fun loginUser(){
        val email = findViewById<EditText>(R.id.logEmail).text.toString()
        val password = findViewById<EditText>(R.id.logPw).text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    val user = auth.currentUser
                    val uid = user?.uid

                    //firestore서 username 가져오기
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("users").document(uid!!)

                    docRef.get().addOnSuccessListener { document->
                        if(document != null && document.exists()){
                            val username = document.getString("username")
                            val mood = document.getString("mood")
                            Toast.makeText(this, "로그인 성공: $username", Toast.LENGTH_SHORT).show()
                            //다음 화면, 기분 묻는 화면으로 전환
                            val intent = Intent(this, Mood::class.java)
                            intent.putExtra("username", username)
                            intent.putExtra("mood", mood)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            Toast.makeText(this, "사용자 정보 없음", Toast.LENGTH_SHORT).show()
                        }
                    }
                        .addOnFailureListener{ e->
                            Toast.makeText(this, "Firestore 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                else{
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /*private fun insertSongs() { //이건 노래추천 데이터베이스에 음악 넣는 거라 비활성화함
        val songs = listOf(
            mapOf(
                "title" to "발자국",
                "artist" to "나상현씨밴드",
                "mood" to "차분한",
                "weather" to "흐림",
                "likeCount" to 0
            ),mapOf(
                "title" to "stranger's theme",
                "artist" to "빈지노",
                "mood" to "차분한",
                "weather" to "맑음",
                "likeCount" to 0
            ),mapOf(
                "title" to "아까워",
                "artist" to "재지팩트",
                "mood" to "차분한",
                "weather" to "흐림",
                "likeCount" to 0
            ),mapOf(
                "title" to "young knight",
                "artist" to "재지팩트",
                "mood" to "기분이 그저그런",
                "weather" to "맑음",
                "likeCount" to 0
            ),mapOf(
                "title" to "25",
                "artist" to "나상현씨밴드",
                "mood" to "기분이 그저그런",
                "weather" to "흐림",
                "likeCount" to 0
            ),mapOf(
                "title" to "always awake",
                "artist" to "빈지노",
                "mood" to "기분이 좋은",
                "weather" to "맑음",
                "likeCount" to 0
            ),mapOf(
                "title" to "love love love",
                "artist" to "나상현씨밴드",
                "mood" to "기분이 좋은",
                "weather" to "맑음",
                "likeCount" to 0
            ),mapOf(
                "title" to "train",
                "artist" to "빈지노",
                "mood" to "행복한",
                "weather" to "맑음",
                "likeCount" to 0
            )
        )

        val db = FirebaseFirestore.getInstance()
        for (song in songs) {
            db.collection("songs").add(song)
        }
    }*/
}