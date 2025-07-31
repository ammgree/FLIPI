package com.example.itunesapi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Register : AppCompatActivity() {
    // Firebase 인증 객체
    private lateinit var auth : FirebaseAuth

    // Firebase Firestore 객체
    private lateinit var firestore: FirebaseFirestore

    // 회원가입 완료 버튼
    lateinit var completeButton: Button

    // 프로필 이미지 뷰
    private lateinit var profileImage: ImageView

    // 선택된 이미지 Uri 저장 변수
    private var imageUri: Uri? = null

    // 이미지 선택 요청 코드 상수
    private val PICK_IMAGE_REQUEST = 1

    // Firebase Storage 참조 객체
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase 인스턴스 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 버튼 및 이미지 뷰와 연결
        completeButton = findViewById(R.id.regButton)
        profileImage = findViewById(R.id.profileImage)

        // Firebase Storage 참조 초기화
        storageRef = FirebaseStorage.getInstance().reference

        // 회원가입 버튼 클릭 시 registerUser 호출
        completeButton.setOnClickListener {
            registerUser()
        }

        // 프로필 이미지 클릭 시 이미지 선택 인텐트 실행
        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    // 이미지 선택 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImage.setImageURI(imageUri) // 선택한 이미지 표시
        }
    }

    // 사용자 회원가입 처리 함수
    private fun registerUser(){
        val userName = findViewById<EditText>(R.id.regName).text.toString()
        val userEmail = findViewById<EditText>(R.id.regEmail).text.toString()
        val userPw= findViewById<EditText>(R.id.regPw).text.toString()

        // 이메일과 비밀번호로 Firebase 사용자 생성
        auth.createUserWithEmailAndPassword(userEmail, userPw)
            .addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    val uid = auth.currentUser?.uid

                    // 이미지가 선택된 경우 Firebase Storage에 업로드
                    if (imageUri != null && uid != null) {
                        val imageRef = storageRef.child("profileImages/$uid.jpg")
                        imageRef.putFile(imageUri!!)
                            .addOnSuccessListener {
                                // 업로드 성공 시 이미지 다운로드 URL 가져오기
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    onRegisterSuccess(userName, userEmail, userPw, uri.toString())
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // 이미지가 선택되지 않은 경우
                        onRegisterSuccess(userName, userEmail, userPw, null)
                    }
                }
            }
            .addOnFailureListener { e->
                // 회원가입 실패 시 메시지 출력
                Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 사용자 정보 Firestore에 저장
    private fun saveUserData(username: String, userEmail: String, userPw: String, imageUrl: String?) {
        val user = hashMapOf(
            "username" to username,
            "email" to userEmail,
            "password" to userPw,
            "profileImageUrl" to imageUrl,
            "mood" to "" // 기본값은 빈 문자열
        )
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).set(user)
                .addOnSuccessListener {
                    Log.d("register", "유저 정보 저장 완료")
                }
                .addOnFailureListener { e ->
                    Log.e("register", "유저 정보 저장 실패", e)
                }
        }
    }

    // 회원가입 성공 후 실행 함수
    private fun onRegisterSuccess(userName: String, userEmail: String, userPw: String, imageUrl: String?) {
        saveUserData(userName, userEmail, userPw, imageUrl)
        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
        navigateToMainActivity()
    }

    // 로그인 화면으로 이동
    private fun navigateToMainActivity() {
        val intent = Intent (this, Login::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }
}
