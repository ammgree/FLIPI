package com.example.itunesapi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth //firebase사용권한
    private lateinit var firestore: FirebaseFirestore

    lateinit var completeButton: Button //회원가입 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()//firebase에서 인스턴스 가져오기
        firestore = FirebaseFirestore.getInstance()

        completeButton = findViewById(R.id.regButton)
        completeButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){
        val userName = findViewById<EditText>(R.id.regName).text.toString()
        val userEmail = findViewById<EditText>(R.id.regEmail).text.toString()
        val userPw= findViewById<EditText>(R.id.regPw).text.toString()

        auth.createUserWithEmailAndPassword(userEmail, userPw) //firebase 권한으로 em/pw 만듦
            .addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    val user=auth.currentUser
                    saveUserData(userName, userEmail, userPw) //firestore에 사용자 세부정보 저장
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
            }
            .addOnFailureListener { e-> //이미 계정이 있거나 등..
                Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData(username: String, userEmail: String, userPw: String) {
        //해시맵으로 username과 email, pw 필드에 저장 근데 유아이디는 어따로..?
        val user = hashMapOf( "username" to username, "email" to userEmail, "password" to userPw)
        val uid = auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid!!).set(user)

        //생성된 id로 새 문서 추가
        firestore.collection("users") //컬렉션 이름과 같게
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("register", "DocumentSnapshot added with UID : ${documentReference.id}")
            }
            .addOnFailureListener{e->
                Log.e("register", "문서 추가 오류", e)
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent (this, Login::class.java) //로그인=메인화면으로 돌아가는 intent
        startActivity(intent)
        finish() //현 액티비티 종료, 뒤로가기버튼으로 다시 돌아오지 못함
    }
}