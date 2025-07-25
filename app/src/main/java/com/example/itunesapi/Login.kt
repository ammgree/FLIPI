package com.example.itunesapi

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        auth = FirebaseAuth.getInstance()//firebase에서 인스턴스 가져오기

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
                            Toast.makeText(this, "로그인 성공: $username", Toast.LENGTH_SHORT).show()
                            //다음 화면, 기분 묻는 화면으로 전환
                            val intent = Intent(this, Mood::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            Toast.makeText(this, "사용자 정보 없음", Toast.LENGTH_SHORT).show()
                        }
                    }
                        .addOnFailureListener{e->
                            Toast.makeText(this, "Firestore 오류: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
                else{
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}