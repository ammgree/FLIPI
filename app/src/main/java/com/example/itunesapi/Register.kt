package com.example.itunesapi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class Register : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth //firebase사용권한
    private lateinit var firestore: FirebaseFirestore

    lateinit var completeButton: Button //회원가입 버튼
    private lateinit var profileImage: ImageView
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var storageRef: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()//firebase에서 인스턴스 가져오기
        firestore = FirebaseFirestore.getInstance()

        completeButton = findViewById(R.id.regButton)
        completeButton.setOnClickListener {
            registerUser()
        }

        profileImage = findViewById(R.id.profileImage)
        storageRef = FirebaseStorage.getInstance().reference

        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImage.setImageURI(imageUri)
        }
    }


    private fun registerUser(){
        val userName = findViewById<EditText>(R.id.regName).text.toString()
        val userEmail = findViewById<EditText>(R.id.regEmail).text.toString()
        val userPw= findViewById<EditText>(R.id.regPw).text.toString()

        auth.createUserWithEmailAndPassword(userEmail, userPw)
            .addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    val uid = auth.currentUser?.uid
                    if (imageUri != null && uid != null) {
                        val imageRef = storageRef.child("profileImages/$uid.jpg")
                        imageRef.putFile(imageUri!!)
                            .addOnSuccessListener {
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    onRegisterSuccess(userName, userEmail, userPw, uri.toString())
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        onRegisterSuccess(userName, userEmail, userPw, null)
                    }

                }
            }
            .addOnFailureListener { e->
                Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveUserData(username: String, userEmail: String, userPw: String, imageUrl: String?) {
        val user = hashMapOf(
            "username" to username,
            "email" to userEmail,
            "password" to userPw,
            "profileImageUrl" to imageUrl
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


    private fun onRegisterSuccess(userName: String, userEmail: String, userPw: String, imageUrl: String?) {
        saveUserData(userName, userEmail, userPw, imageUrl)
        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent (this, Login::class.java) //로그인=메인화면으로 돌아가는 intent
        startActivity(intent)
        finish() //현 액티비티 종료, 뒤로가기버튼으로 다시 돌아오지 못함
    }
}